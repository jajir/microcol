package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitMasterBlacksmith;
import org.microcol.model.unit.UnitSpecialist;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitActionType;

import com.google.gson.Gson;

class EuropeanSpecialistTest {

    @TempDir
    Path directory;

    private static Stream<UnitType> roster() {
        return UnitType.EUROPEAN_SPECIALISTS.stream();
    }

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @ParameterizedTest
    @MethodSource("roster")
    void everyEligibleSpecialistCanBeBoughtEmbarkedAndReloaded(
            final UnitType type) {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        final int originalCount = buyer.getAllUnits().size();
        final int expectedId = model.getAllUnits().stream()
                .mapToInt(Unit::getId).max().orElseThrow() + 1;
        assertSame(type, UnitType.valueOf(type.name()));
        buyer.setGold(type.getEuropePrice());
        assertTrue(buyer.canBuyUnit(type));
        buyer.buy(type);
        assertEquals(0, buyer.getGold());
        assertEquals(originalCount + 1, buyer.getAllUnits().size());
        final Unit expert = model.getUnitById(expectedId);
        assertSame(type, expert.getType());
        assertTrue(expert.isAtEuropePier());
        assertSame(buyer, expert.getOwner());
        assertEquals(type == UnitType.MASTER_BLACKSMITH,
                expert instanceof UnitMasterBlacksmith);
        assertEquals(type != UnitType.MASTER_BLACKSMITH,
                expert instanceof UnitSpecialist);

        ((UnitFreeColonist) expert).setTools(80);
        final Ship ship = (Ship) model.getUnitById(3);
        ship.placeToEuropePort(model.getEurope().getPort());
        final CargoSlot hold = ship.getCargo().getSlots().stream()
                .filter(CargoSlot::isEmpty).findFirst().orElseThrow();
        hold.store(expert);
        assertSame(expert, hold.getUnit().orElseThrow());
        data.setModel(model.save());
        final Path file = directory.resolve(type.name() + ".microcol");
        final GameModelPoDao dao = new GameModelPoDao();
        dao.saveToFile(file.toString(), data);
        final Model restored = load(dao.loadFromFile(file.toFile()));
        final Unit reloaded = restored.getUnitById(expectedId);
        assertSame(type, reloaded.getType());
        assertEquals(80, ((UnitFreeColonist) reloaded).getTools());
        assertEquals("Dutch", reloaded.getOwner().getName());
        assertTrue(reloaded.isAtCargoSlot());
        assertEquals(3, reloaded.getPlaceCargoSlot().getOwnerUnit().getId());
        assertSame(reloaded, reloaded.getPlaceCargoSlot().getCargoSlot()
                .getUnit().orElseThrow());
        assertEquals(0, restored.getPlayerByName("Dutch").getGold());
    }

    @ParameterizedTest
    @MethodSource("roster")
    void oneGoldShortCannotCreateOrChargeSpecialist(final UnitType type) {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(type.getEuropePrice() - 1);
        final String before = new Gson().toJson(model.save());
        assertFalse(buyer.canBuyUnit(type));
        assertThrows(NotEnoughtGoldException.class, () -> buyer.buy(type));
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void armedOreMinerRetainsSpecialtyAndCanAttackLikeAnEquippedColonist() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.EXPERT_ORE_MINER.getEuropePrice());
        buyer.buy(UnitType.EXPERT_ORE_MINER);
        final UnitFreeColonist miner = (UnitFreeColonist) buyer.getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.EXPERT_ORE_MINER)
                .findFirst().orElseThrow();
        assertFalse(miner.canAttack());
        miner.setHoldingGuns(true);
        assertTrue(miner.canAttack());
        assertSame(UnitType.EXPERT_ORE_MINER, miner.getType());
        assertTrue(miner.getType().getAttackableUnitType()
                .contains(UnitType.COLONIST));
        assertEquals(2.0F,
                miner.getType().getProductionModifier(GoodsType.ORE));
    }

    @Test
    void seasonedScoutHasTwoTileSightWithoutChangingItsIdentity() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.SEASONED_SCOUT.getEuropePrice());
        buyer.buy(UnitType.SEASONED_SCOUT);
        final UnitFreeColonist scout = (UnitFreeColonist) buyer.getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.SEASONED_SCOUT)
                .findFirst().orElseThrow();
        assertEquals(2, scout.getSightRadius());
        scout.setHoldingGuns(true);
        assertEquals(2, scout.getSightRadius());
        assertSame(UnitType.SEASONED_SCOUT, scout.getType());
    }

    @Test
    void hardyPioneerFinishesPlowAfterTwoOwnerUpdatesAcrossDiskReload() {
        final GameModelPo data = scenario();
        final Location field = Location.of(24, 14);
        final var fields = data.getModel().getMap().getFieldSet();
        fields.remove(field);
        data.getModel().getMap().setFields(fields);
        Model model = Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.HARDY_PIONEER.getEuropePrice());
        buyer.buy(UnitType.HARDY_PIONEER);
        final UnitFreeColonist hardy = (UnitFreeColonist) buyer.getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.HARDY_PIONEER)
                .findFirst().orElseThrow();
        final int id = hardy.getId();
        hardy.placeToLocation(field);
        hardy.setTools(100);
        hardy.setActionPoints(hardy.getSpeed());
        hardy.setActionType(UnitActionType.plowField);
        assertEquals(2,
                ((UnitActionPlowField) hardy.getAction()).getRemainingTurns());
        model.endTurn();
        model.endTurn();
        assertEquals(1,
                ((UnitActionPlowField) hardy.getAction()).getRemainingTurns());
        assertFalse(model.getMap().getTerrainAt(field).isHasField());
        data.setModel(model.save());
        final Path file = directory.resolve("hardy.microcol");
        final GameModelPoDao dao = new GameModelPoDao();
        dao.saveToFile(file.toString(), data);
        model = Model.make(dao.loadFromFile(file.toFile()).getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        final Unit reloaded = model.getUnitById(id);
        assertSame(UnitType.HARDY_PIONEER, reloaded.getType());
        assertEquals(100, ((UnitFreeColonist) reloaded).getTools());
        assertEquals(1, ((UnitActionPlowField) reloaded.getAction())
                .getRemainingTurns());
        model.endTurn();
        model.endTurn();
        assertTrue(model.getMap().getTerrainAt(field).isHasField());
        assertEquals(80, ((UnitFreeColonist) reloaded).getTools());
        assertEquals(UnitActionType.noAction, reloaded.getAction().getType());
    }

    @Test
    void purchasedFarmerCanUseRealColonyJobChoiceAndRestoreAssignment() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.EXPERT_FARMER.getEuropePrice());
        buyer.buy(UnitType.EXPERT_FARMER);
        final Unit expert = buyer.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.EXPERT_FARMER)
                .findFirst().orElseThrow();
        final int id = expert.getId();
        final Colony colony = model.getColonyById(0).orElseThrow();
        expert.placeToLocation(colony.getLocation());
        final ColonyJobChoice offer = colony.getJobChoices(expert).stream()
                .filter(choice -> choice
                        .getKind() == ColonyJobChoice.Kind.BEST_FIELD
                        && choice.getGoodsType() == GoodsType.CORN
                        && choice.getUnavailableReason().isEmpty())
                .findFirst().orElseThrow();
        assertTrue(offer.isSpecialty());
        final int quoted = offer.getPotential().orElseThrow().getAmount();
        assertTrue(quoted > 0);
        offer.execute();
        assertTrue(expert.isAtPlaceColonyField());
        final ColonyField field = colony.getColonyFields().stream()
                .filter(candidate -> candidate.getUnit().orElse(null) == expert)
                .findFirst().orElseThrow();
        assertEquals(quoted, field.getProduction().orElseThrow().getAmount());
        data.setModel(model.save());
        final Path file = directory.resolve("assigned-farmer.microcol");
        final GameModelPoDao dao = new GameModelPoDao();
        dao.saveToFile(file.toString(), data);
        final Model restored = load(dao.loadFromFile(file.toFile()));
        assertSame(UnitType.EXPERT_FARMER, restored.getUnitById(id).getType());
        assertTrue(restored.getUnitById(id).isAtPlaceColonyField());
        assertEquals(quoted, restored.getColonyById(0).orElseThrow()
                .getColonyFields().stream()
                .filter(candidate -> candidate.getUnit()
                        .orElse(null) == restored.getUnitById(id))
                .findFirst().orElseThrow().getProduction().orElseThrow()
                .getAmount());
    }
}
