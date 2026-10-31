package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Crown dispatch from the persisted roster: real royal passengers, one shipment
 * per reserve member, and losses recorded once (R-0186).
 */
class RoyalDispatchTest {

    private static final String KING = "Dutch's King";

    /** Starts the Crown's own turn of a Dutch campaign with a fresh force. */
    private static Model royalTurn(final boolean declared) {
        return royalTurn(declared, Difficulty.CONQUISTADOR);
    }

    private static Model royalTurn(final boolean declared,
            final Difficulty difficulty) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().setDifficulty(difficulty);
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() != null) {
                player.setRoyalRoster(RoyalRoster.initial(difficulty));
            } else if (player.getName().equals("Dutch")) {
                RebellionFixtures.set(data.getModel(), player, declared);
            }
        }
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        model.endTurn();
        assertEquals(KING, model.getCurrentPlayer().getName());
        return model;
    }

    private static RoyalRoster crown(final Model model) {
        return model.getRoyalRosterOf(model.getPlayerByName(KING));
    }

    private static int shipped(final RoyalRoster crown, final Family family) {
        return crown.getCount(family, State.SHIPPED);
    }

    private static int lost(final RoyalRoster crown, final Family family) {
        return crown.getCount(family, State.DESTROYED);
    }

    private static int reserve(final RoyalRoster crown, final Family family) {
        return crown.getCount(family, State.RESERVE);
    }

    private static int firstReserveShip(final RoyalRoster crown) {
        return crown.getMembers().stream()
                .filter(member -> member.getFamily() == Family.MAN_OF_WAR
                        && member.getState() == State.RESERVE)
                .findFirst().orElseThrow().getId();
    }

    private static List<CargoSlot> loadedSlots(final UnitWithCargo ship) {
        return ship.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).collect(Collectors.toList());
    }

    @Test
    void dispatchLoadsRealRoyalPassengersWithinTheHold() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final int member = firstReserveShip(crown);
        assertTrue(model.canDispatchRoyalShip(king, member));

        final UnitWithCargo voyage = model.dispatchRoyalShip(king, member);

        assertEquals(UnitType.MAN_OF_WAR, voyage.getType());
        assertSame(king, voyage.getOwner());
        assertTrue(voyage.isAtHighSea());
        final List<CargoSlot> loaded = loadedSlots(voyage);
        assertEquals(6, loaded.size());
        // Infantry, cavalry, artillery in turn: two of each family sail.
        assertEquals(2, shipped(crown, Family.INFANTRY));
        assertEquals(2, shipped(crown, Family.CAVALRY));
        assertEquals(2, shipped(crown, Family.ARTILLERY));
        assertEquals(State.SHIPPED,
                crown.findByUnitId(voyage.getId()).orElseThrow().getState());
        int mounted = 0;
        for (final CargoSlot slot : loaded) {
            final Unit soldier = slot.getUnit().orElseThrow();
            assertSame(king, soldier.getOwner());
            assertTrue(soldier.isAtCargoSlot());
            assertTrue(crown.findByUnitId(soldier.getId()).isPresent());
            if (soldier instanceof UnitArtillery) {
                assertEquals(UnitType.ARTILLERY, soldier.getType());
            } else {
                final UnitFreeColonist royal = (UnitFreeColonist) soldier;
                assertEquals(MilitaryRole.ROYAL, royal.getMilitaryRole());
                assertTrue(royal.isHoldingGuns());
                assertTrue(royal.canAttack());
                if (royal.isMounted()) {
                    mounted++;
                }
            }
        }
        assertEquals(2, mounted);
        // Reserve shrinks by exactly the shipped members; nothing is recreated.
        assertEquals(4, reserve(crown, Family.INFANTRY));
        assertEquals(2, reserve(crown, Family.CAVALRY));
        assertEquals(0, reserve(crown, Family.ARTILLERY));
        assertEquals(1, reserve(crown, Family.MAN_OF_WAR));
    }

    @Test
    void eachReserveShipSailsOnceAndTheForceNeverDuplicates() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final int first = firstReserveShip(crown);
        model.dispatchRoyalShip(king, first);
        assertFalse(model.canDispatchRoyalShip(king, first));
        assertThrows(IllegalStateException.class,
                () -> model.dispatchRoyalShip(king, first));

        final int second = firstReserveShip(crown);
        model.dispatchRoyalShip(king, second);
        // Twelve ground units sailed with the two ships; nothing is left.
        assertEquals(0,
                reserve(crown, Family.INFANTRY) + reserve(crown, Family.CAVALRY)
                        + reserve(crown, Family.ARTILLERY));
        assertEquals(0, reserve(crown, Family.MAN_OF_WAR));
        assertEquals(12,
                shipped(crown, Family.INFANTRY) + shipped(crown, Family.CAVALRY)
                        + shipped(crown, Family.ARTILLERY));
        assertEquals(2, shipped(crown, Family.MAN_OF_WAR));
        assertEquals(2,
                model.getAllUnits().stream()
                        .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR
                                && unit.getOwner() == king)
                        .count());
    }

    @Test
    void undeclaredCrownKeepsItsForceInReserve() {
        final Model model = royalTurn(false);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final int member = firstReserveShip(crown);
        assertFalse(model.canDispatchRoyalShip(king, member));
        assertThrows(IllegalStateException.class,
                () -> model.dispatchRoyalShip(king, member));
        assertEquals(2, reserve(crown, Family.MAN_OF_WAR));
        assertEquals(0, shipped(crown, Family.INFANTRY));
    }

    @Test
    void lostShipRecordsItsPassengersOnceAndKeepsReserveIntact() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final UnitWithCargo voyage = model.dispatchRoyalShip(king,
                firstReserveShip(crown));

        model.destroyUnit(voyage);

        assertEquals(1, lost(crown, Family.MAN_OF_WAR));
        assertEquals(2, lost(crown, Family.INFANTRY));
        assertEquals(2, lost(crown, Family.CAVALRY));
        assertEquals(2, lost(crown, Family.ARTILLERY));
        assertEquals(4, reserve(crown, Family.INFANTRY));
        assertEquals(2, reserve(crown, Family.CAVALRY));
        assertEquals(1, reserve(crown, Family.MAN_OF_WAR));
        assertEquals(0, shipped(crown, Family.INFANTRY));
    }

    @Test
    void lostPassengerLeavesItsShipAndTheOthersShipped() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final UnitWithCargo voyage = model.dispatchRoyalShip(king,
                firstReserveShip(crown));
        final Unit lost = loadedSlots(voyage).get(0).getUnit().orElseThrow();

        model.destroyUnit(lost);

        assertEquals(1, lost(crown, Family.INFANTRY));
        assertEquals(State.SHIPPED,
                crown.findByUnitId(voyage.getId()).orElseThrow().getState());
        assertEquals(5, loadedSlots(voyage).size());
        assertEquals(1, shipped(crown, Family.INFANTRY));
    }

    @Test
    void savedRoyalUnitOrderSurvivesCarrierFirstLoading() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        model.dispatchRoyalShip(king, firstReserveShip(crown(model)));
        model.dispatchRoyalShip(king, firstReserveShip(crown(model)));
        final List<Integer> expectedOrder = model.getAllUnits().stream()
                .map(Unit::getId).collect(Collectors.toList());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());

        assertEquals(expectedOrder, restored.getAllUnits().stream()
                .map(Unit::getId).collect(Collectors.toList()));
    }

    @Test
    void savedBindingsReloadAndAnUnknownUnitIsRejected() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        model.dispatchRoyalShip(king, firstReserveShip(crown));

        final ModelPo saved = model.save();
        final Model restored = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(new com.google.gson.Gson().toJson(crown.save()),
                new com.google.gson.Gson().toJson(crown(restored).save()));

        final ModelPo broken = model.save();
        broken.getGameManager().getPlayers().stream()
                .filter(player -> KING.equals(player.getName())).findFirst()
                .orElseThrow().getRoyalRoster().getMembers().stream()
                .filter(member -> member.getState() == State.SHIPPED)
                .findFirst().orElseThrow().setUnitId(9999);
        assertThrows(IllegalArgumentException.class, () -> Model.make(broken,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void startingForceLargerThanTheTwoHoldsKeepsItsSurplusInReserve() {
        // Governor starts with fourteen ground units; two ships carry twelve.
        final Model model = royalTurn(true, Difficulty.GOVERNOR);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        model.dispatchRoyalShip(king, firstReserveShip(crown));
        model.dispatchRoyalShip(king, firstReserveShip(crown));

        assertEquals(12,
                shipped(crown, Family.INFANTRY) + shipped(crown, Family.CAVALRY)
                        + shipped(crown, Family.ARTILLERY));
        assertEquals(2,
                reserve(crown, Family.INFANTRY) + reserve(crown, Family.CAVALRY)
                        + reserve(crown, Family.ARTILLERY));
        assertEquals(0, reserve(crown, Family.MAN_OF_WAR));
        // No third ship is created for the surplus, and no member is copied.
        final int shipMember = crown.getMembers().stream()
                .filter(member -> member.getFamily() == Family.MAN_OF_WAR)
                .findFirst().orElseThrow().getId();
        assertFalse(model.canDispatchRoyalShip(king, shipMember));
        assertEquals(16, crown.getMembers().size());
        assertEquals(2,
                model.getAllUnits().stream()
                        .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR
                                && unit.getOwner() == king)
                        .count());
    }

    @Test
    void transportPlanNeedsAShipOnTheMap() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final UnitWithCargo voyage = model.dispatchRoyalShip(king,
                firstReserveShip(crown));
        assertThrows(IllegalStateException.class,
                () -> model.setRoyalTransportPlan(king, voyage,
                        java.util.Optional.empty(), 0));
    }

    private static final int[] RESERVE = { 7, 12 };

    /** The Crown's own turn of a prepared war, played without a robot. */
    private static Model kingTurnOf(final GameModelPo data) {
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        model.endTurn();
        assertEquals(KING, model.getCurrentPlayer().getName());
        return model;
    }

    private static String ledger(final Model model) {
        return new com.google.gson.Gson().toJson(crown(model).save());
    }

    @Test
    void returnedCarrierTakesTheRemainingReserveAndSailsAgain() {
        // The Crown holds Delft, so two living ground units do not win the
        // rebellion during this Crown turn (R-0191).
        final GameModelPo data = RoyalScenarioFixtures.midWar(null, true,
                RESERVE, new int[0]);
        RebellionFixtures.transfer(data.getModel(), KING, 1);
        final Model model = kingTurnOf(data);
        final Player king = model.getPlayerByName(KING);
        final UnitWithCargo carrier = (UnitWithCargo) model
                .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
        final int units = model.getAllUnits().size();
        assertTrue(carrier.isAtEuropePort());
        assertTrue(model.canReloadRoyalShip(king, carrier));

        model.reloadRoyalShip(king, carrier);

        // The same ship is on the Atlantic again, outward bound, holding the
        // two reserve members as real royal units.
        assertSame(carrier,
                model.getUnitById(RoyalScenarioFixtures.CARRIER_ID));
        assertTrue(carrier.isAtHighSea());
        assertFalse(((PlaceHighSea) carrier.getPlace()).isTravelToEurope());
        assertEquals(units + 2, model.getAllUnits().size());
        final RoyalRoster crown = crown(model);
        assertEquals(0, reserve(crown, Family.INFANTRY)
                + reserve(crown, Family.CAVALRY));
        final List<Unit> aboard = loadedSlots(carrier).stream()
                .map(slot -> slot.getUnit().orElseThrow())
                .collect(Collectors.toList());
        assertEquals(2, aboard.size());
        for (final Unit soldier : aboard) {
            final UnitFreeColonist royal = (UnitFreeColonist) soldier;
            assertEquals(MilitaryRole.ROYAL, royal.getMilitaryRole());
            assertTrue(royal.isHoldingGuns());
            final RoyalRoster.Member member = crown
                    .findByUnitId(soldier.getId()).orElseThrow();
            assertEquals(State.SHIPPED, member.getState());
            assertEquals(member.getFamily() == Family.CAVALRY,
                    royal.isMounted());
        }
        assertEquals(12, lost(crown, Family.INFANTRY)
                + lost(crown, Family.CAVALRY) + lost(crown, Family.ARTILLERY));

        // Once only: the reserve is spent and the ship is on its way.
        assertFalse(model.canReloadRoyalShip(king, carrier));
        assertThrows(IllegalStateException.class,
                () -> model.reloadRoyalShip(king, carrier));

        // Continue keeps the ledger and the units.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(ledger(model), ledger(restored));
    }

    @Test
    void reloadIsRefusedBeforeAnyChangeUnlessAnEmptyCarrierIsInEurope() {
        // On the map, not in Europe.
        assertReloadRefused(RoyalScenarioFixtures.midWar(Location.of(30, 14),
                true, RESERVE, new int[0]));
        // In Europe, but its hold is not empty.
        assertReloadRefused(RoyalScenarioFixtures.midWar(null, true, RESERVE,
                new int[] { 1 }));
        // In Europe and empty, but no ground unit waits in reserve.
        assertReloadRefused(RoyalScenarioFixtures.midWar(null, true, new int[0],
                new int[0]));
    }

    private static void assertReloadRefused(final GameModelPo data) {
        final Model model = kingTurnOf(data);
        final Player king = model.getPlayerByName(KING);
        final UnitWithCargo carrier = (UnitWithCargo) model
                .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
        final String before = ledger(model);
        final int units = model.getAllUnits().size();
        final Place place = carrier.getPlace();

        assertFalse(model.canReloadRoyalShip(king, carrier));
        assertThrows(IllegalStateException.class,
                () -> model.reloadRoyalShip(king, carrier));

        assertEquals(before, ledger(model));
        assertEquals(units, model.getAllUnits().size());
        assertSame(place, carrier.getPlace());
    }

    @Test
    void reloadIsRefusedOutsideTheCrownsOwnTurn() {
        final Model model = Model.make(
                RoyalScenarioFixtures.midWar(null, true, RESERVE, new int[0])
                        .getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        final Player king = model.getPlayerByName(KING);
        final UnitWithCargo carrier = (UnitWithCargo) model
                .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
        assertFalse(model.canReloadRoyalShip(king, carrier));
        assertThrows(IllegalStateException.class,
                () -> model.reloadRoyalShip(king, carrier));
    }

    @Test
    void aCrownUnitThatChangesOwnerLeavesTheRosterOnce() {
        final Model model = royalTurn(true);
        final Player king = model.getPlayerByName(KING);
        final RoyalRoster crown = crown(model);
        final UnitWithCargo voyage = model.dispatchRoyalShip(king,
                firstReserveShip(crown));
        final Unit soldier = loadedSlots(voyage).get(0).getUnit().orElseThrow();
        final int id = soldier.getId();

        soldier.takeOver(model.getPlayerByName("Dutch"));

        // The unit lives on under its new owner: captured, not destroyed.
        assertEquals(1, crown.getCount(Family.INFANTRY, State.CAPTURED));
        assertEquals(0, lost(crown, Family.INFANTRY));
        assertTrue(crown.findByUnitId(id).isEmpty());
        // Taking the unit again, or destroying it, changes nothing more.
        soldier.takeOver(model.getPlayerByName("Dutch"));
        model.destroyUnit(soldier);
        assertEquals(1, crown.getCount(Family.INFANTRY, State.CAPTURED));
        assertEquals(0, lost(crown, Family.INFANTRY));
        assertEquals(State.SHIPPED,
                crown.findByUnitId(voyage.getId()).orElseThrow().getState());

        // Continue keeps the captured state and accepts the unbound unit.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(1,
                crown(restored).getCount(Family.INFANTRY, State.CAPTURED));
    }

    @Test
    void savedCapturedMemberWithAUnitIsRejected() {
        final ModelPo saved = royalTurn(true).save();
        final org.microcol.model.store.RoyalMemberPo member = saved
                .getGameManager().getPlayers().stream()
                .filter(player -> KING.equals(player.getName())).findFirst()
                .orElseThrow().getRoyalRoster().getMembers().get(0);
        member.setState(State.CAPTURED);
        member.setUnitId(900);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
