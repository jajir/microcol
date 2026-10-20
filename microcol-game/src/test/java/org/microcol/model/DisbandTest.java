package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * Player disbanding: the model offer, the equipment that is kept or lost, the
 * refusals before any change and the single removal of a confirmed unit.
 */
class DisbandTest {

    /** One square from New Amsterdam (id 0), on grass without trees. */
    private static final Location TARGET = Location.of(24, 14);

    /** Open sea two squares east of New Amsterdam, next to its colony. */
    private static final Location SEA = Location.of(26, 14);

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private final GameModelPoDao dao = new GameModelPoDao();

    private final Gson gson = new Gson();

    private Model fixture() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        types.put(TARGET, TerrainType.GRASSLAND);
        trees.remove(TARGET);
        data.getModel().getMap().setTerrainType(types);
        data.getModel().getMap().setTrees(trees);
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private UnitFreeColonist colonist(final Model model, final Location at) {
        final UnitFreeColonist colonist = (UnitFreeColonist) model
                .addUnitOutSideColony(model.getColonyById(0).orElseThrow());
        colonist.placeToLocation(at);
        colonist.setActionPoints(colonist.getSpeed());
        return colonist;
    }

    /** A colonist standing on New Amsterdam's own square, outside slots. */
    private UnitFreeColonist onColonySquare(final Model model) {
        return (UnitFreeColonist) model
                .addUnitOutSideColony(model.getColonyById(0).orElseThrow());
    }

    private String snapshot(final Model model) {
        return gson.toJson(model.save());
    }

    private int stock(final Model model, final GoodsType type) {
        return model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(type).getAmount();
    }

    private int capacity(final Model model, final GoodsType type) {
        return model.getColonyById(0).orElseThrow().getWarehouse()
                .getStorageCapacity(type).getAmount();
    }

    @Test
    void ordinaryColonistOutsideColonyLosesAllEquipment() {
        final Model model = fixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        colonist.setTools(40);
        colonist.setMounted(true);
        colonist.setHoldingGuns(true);
        final int horsesBefore = stock(model, GoodsType.HORSE);
        final int toolsBefore = stock(model, GoodsType.TOOLS);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        assertTrue(offer.getReceivingColony().isEmpty());
        assertEquals(50, offer.getCarried(GoodsType.HORSE));
        assertEquals(50, offer.getCarried(GoodsType.MUSKET));
        assertEquals(40, offer.getCarried(GoodsType.TOOLS));
        assertEquals(50, offer.getLost(GoodsType.HORSE));
        assertEquals(40, offer.getLost(GoodsType.TOOLS));
        assertEquals(0, offer.getRetained(GoodsType.MUSKET));
        assertFalse(offer.hasExcessLoss());
        model.disband(colonist, offer, false);
        assertFalse(model.getAllUnits().contains(colonist));
        assertEquals(horsesBefore, stock(model, GoodsType.HORSE));
        assertEquals(toolsBefore, stock(model, GoodsType.TOOLS));
    }

    @Test
    void colonistOnOwnedColonySquareReturnsActualEquipment() {
        final Model model = fixture();
        final UnitFreeColonist colonist = onColonySquare(model);
        colonist.setTools(35);
        colonist.setMounted(true);
        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.HORSE, 0));
        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.TOOLS, 0));
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        assertEquals(0, offer.getReceivingColony().orElseThrow().getId());
        assertEquals(50, offer.getRetained(GoodsType.HORSE));
        assertEquals(35, offer.getRetained(GoodsType.TOOLS));
        assertEquals(0, offer.getLost(GoodsType.TOOLS));
        assertFalse(offer.hasExcessLoss());
        model.disband(colonist, offer, false);
        assertEquals(50, stock(model, GoodsType.HORSE));
        assertEquals(35, stock(model, GoodsType.TOOLS));
        assertFalse(model.getAllUnits().contains(colonist));
    }

    @Test
    void excessAboveWarehouseLimitNeedsAcknowledgementAndExactFitDoesNot() {
        final Model model = fixture();
        final int capacity = capacity(model, GoodsType.HORSE);
        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.HORSE, capacity - 30));
        final UnitFreeColonist colonist = onColonySquare(model);
        colonist.setMounted(true);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        assertEquals(30, offer.getRetained(GoodsType.HORSE));
        assertEquals(20, offer.getLost(GoodsType.HORSE));
        assertTrue(offer.hasExcessLoss());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, offer, false));
        assertEquals(before, snapshot(model));
        model.disband(colonist, offer, true);
        assertEquals(capacity, stock(model, GoodsType.HORSE));
        assertFalse(model.getAllUnits().contains(colonist));

        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.HORSE, capacity - 50));
        final UnitFreeColonist exact = onColonySquare(model);
        exact.setMounted(true);
        final DisbandOffer exactOffer = model.getDisbandOffer(exact)
                .orElseThrow();
        assertEquals(0, exactOffer.getLost(GoodsType.HORSE));
        assertFalse(exactOffer.hasExcessLoss());
        model.disband(exact, exactOffer, false);
        assertEquals(capacity, stock(model, GoodsType.HORSE));
    }

    @Test
    void staleWarehouseSpaceRejectsConfirmationWithoutMutation() {
        final Model model = fixture();
        final int capacity = capacity(model, GoodsType.HORSE);
        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.HORSE, capacity - 30));
        final UnitFreeColonist colonist = onColonySquare(model);
        colonist.setMounted(true);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.HORSE, capacity - 10));
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, offer, true));
        assertEquals(before, snapshot(model));
        assertTrue(model.getAllUnits().contains(colonist));
    }

    @Test
    void emptyShipAndWagonCanBeDisbanded() {
        final Model model = fixture();
        final Player dutch = model.getPlayerByName("Dutch");
        final Unit ship = model.addUnitInEurope(UnitType.GALLEON, dutch);
        ship.placeToLocation(SEA);
        final Unit wagon = model.addUnitInEurope(UnitType.WAGON, dutch);
        wagon.placeToLocation(TARGET);
        for (final Unit carrier : List.of(ship, wagon)) {
            assertTrue(model.getDisbandProblem(carrier).isEmpty());
            final DisbandOffer offer = model.getDisbandOffer(carrier)
                    .orElseThrow();
            assertTrue(offer.getReceivingColony().isEmpty());
            model.disband(carrier, offer, false);
            assertFalse(model.getAllUnits().contains(carrier));
        }
    }

    @Test
    void loadedShipRefusesWithoutChangeUntilTheCargoIsGone() {
        final Model model = fixture();
        final Player dutch = model.getPlayerByName("Dutch");
        final Unit ship = model.addUnitInEurope(UnitType.GALLEON, dutch);
        ship.placeToLocation(SEA);
        final DisbandOffer emptyOffer = model.getDisbandOffer(ship)
                .orElseThrow();
        final UnitFreeColonist passenger = onColonySquare(model);
        passenger.setActionPoints(passenger.getSpeed());
        passenger.embarkFromLocation(((UnitWithCargo) ship).getCargo()
                .getEmptyCargoSlot().orElseThrow());
        assertEquals(DisbandProblem.LOADED_CARRIER,
                model.getDisbandProblem(ship).orElseThrow());
        assertTrue(model.getDisbandOffer(ship).isEmpty());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(ship, emptyOffer, false));
        assertEquals(before, snapshot(model));
        assertTrue(model.getAllUnits().contains(ship));
        assertTrue(model.getAllUnits().contains(passenger));
    }

    @Test
    void foreignUnitIsRefusedAndOffersDoNotCrossUnits() {
        final Model model = fixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        final UnitFreeColonist foreign = colonist(model, TARGET);
        foreign.takeOver(model.getPlayerByName("Dutch's King"));
        assertEquals(DisbandProblem.NOT_OWNER,
                model.getDisbandProblem(foreign).orElseThrow());
        assertTrue(model.getDisbandOffer(foreign).isEmpty());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(foreign, offer, false));
        assertEquals(before, snapshot(model));
    }

    @Test
    void staleUnitIsRefusedWithoutChange() {
        final Model model = fixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        model.destroyUnit(colonist);
        assertEquals(DisbandProblem.UNAVAILABLE,
                model.getDisbandProblem(colonist).orElseThrow());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, offer, false));
        assertEquals(before, snapshot(model));
    }

    @Test
    void repeatedConfirmationRemovesTheUnitOnlyOnce() {
        final Model model = fixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        final UnitFreeColonist other = colonist(model, TARGET);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        model.disband(colonist, offer, false);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, offer, false));
        assertEquals(before, snapshot(model));
        assertTrue(model.getAllUnits().contains(other));
    }

    @Test
    void stoppedGameRefusesWithWrongPhaseAndKeepsTheSnapshot() {
        final Model model = fixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        model.stop();
        assertEquals(DisbandProblem.WRONG_PHASE,
                model.getDisbandProblem(colonist).orElseThrow());
        assertTrue(model.getDisbandOffer(colonist).isEmpty());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, offer, false));
        assertEquals(before, snapshot(model));
    }

    @Test
    void continuingOrderEndsWithTheUnitWithoutAdvancingTheTurn() {
        final Model model = fixture();
        final UnitFreeColonist sentry = colonist(model, TARGET);
        model.setSentry(sentry);
        final Player turn = model.getCurrentPlayer();
        final int gold = turn.getGold();
        final DisbandOffer offer = model.getDisbandOffer(sentry).orElseThrow();
        model.disband(sentry, offer, false);
        assertSame(turn, model.getCurrentPlayer());
        assertEquals(gold, turn.getGold());
        assertFalse(model.getAllUnits().contains(sentry));
        assertFalse(model.getUnitsAt(TARGET).contains(sentry));
    }

    @Test
    void fieldResidentReturnsEquipmentToItsColonyWhileAnotherResidentStays() {
        final Model model = fixture();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist resident = (UnitFreeColonist) model
                .getUnitById(0);
        assertTrue(resident.isAtPlaceColonyField());
        final UnitFreeColonist helper = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        final ConstructionSlot slot = colony
                .getConstructionByType(ConstructionType.TOWN_HALL)
                .getConstructionSlots().stream()
                .filter(ConstructionSlot::isEmpty).findFirst().orElseThrow();
        helper.placeToColonyStructureSlot(slot);
        final int residents = colony.getUnitsInsideColony().size();
        assertTrue(resident.canLeaveColony());
        resident.setTools(15);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 0));
        assertTrue(model.getDisbandProblem(resident).isEmpty());
        final DisbandOffer offer = model.getDisbandOffer(resident)
                .orElseThrow();
        assertSame(colony, offer.getReceivingColony().orElseThrow());
        assertEquals(15, offer.getRetained(GoodsType.TOOLS));
        assertFalse(offer.hasExcessLoss());
        model.disband(resident, offer, false);
        assertEquals(15,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertFalse(model.getAllUnits().contains(resident));
        assertTrue(colony.isValid());
        assertEquals(residents - 1, colony.getUnitsInsideColony().size());
        assertTrue(model.getAllUnits().contains(helper));
    }

    @Test
    void lastColonyResidentIsRefusedBecauseLeavingWouldDestroyTheColony() {
        final Model model = fixture();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit last = model.getUnitById(0);
        // Remove every other resident the way the game does, one by one.
        for (final Unit other : colony.getUnitsInsideColony()) {
            if (other != last) {
                other.getPlace().destroy();
                model.destroyUnit(other);
            }
        }
        assertEquals(List.of(last), colony.getUnitsInsideColony());
        assertEquals(DisbandProblem.LAST_COLONY_RESIDENT,
                model.getDisbandProblem(last).orElseThrow());
        assertTrue(model.getDisbandOffer(last).isEmpty());
        assertTrue(colony.isValid());
    }

    @Test
    void pendingImmigrationChoiceRefusesDisbandingWithoutChange() {
        final Model model = pendingImmigrationFixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        final DisbandOffer offer = model.getDisbandOffer(colonist)
                .orElseThrow();
        model.endTurn();
        model.endTurn();
        assertTrue(model.isAwaitingImmigrationChoice());
        assertEquals(DisbandProblem.WRONG_PHASE,
                model.getDisbandProblem(colonist).orElseThrow());
        assertTrue(model.getDisbandOffer(colonist).isEmpty());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, offer, false));
        assertEquals(before, snapshot(model));
        assertTrue(model.getAllUnits().contains(colonist));
    }

    /** Mission Dutch with Brewster and enough crosses for an earned choice. */
    private Model pendingImmigrationFixture() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        dutch.getCongress().setMembers(List.of(Founder.WILLIAM_BREWSTER));
        dutch.getCongress().setSelected(null);
        dutch.getRecruitment().setCrosses(230);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void offerGoesStaleWhenCarriedEquipmentOrPlaceChanges() {
        final Model model = fixture();
        final UnitFreeColonist colonist = colonist(model, TARGET);
        colonist.setTools(40);
        final DisbandOffer equipmentOffer = model.getDisbandOffer(colonist)
                .orElseThrow();
        colonist.setTools(39);
        final String beforeEquipmentChange = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, equipmentOffer, false));
        assertEquals(beforeEquipmentChange, snapshot(model));
        // Standing again on the same square is a new place: the offer is stale.
        colonist.setTools(40);
        final DisbandOffer placeOffer = model.getDisbandOffer(colonist)
                .orElseThrow();
        colonist.placeToLocation(TARGET);
        final String beforePlaceChange = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.disband(colonist, placeOffer, false));
        assertEquals(beforePlaceChange, snapshot(model));
    }
}
