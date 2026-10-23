package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.InterventionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventInterventionArrival;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * R-0190 port rules on the authored Dutch campaign: New Amsterdam (colony 0,
 * 25,14) is the lowest-ID port and touches the sea only at (26,13) and (26,14);
 * Delft (colony 1, 22,12) opens to the northern ocean. The lowest-ID owned
 * coastal colony that the high seas reach is chosen; its approach must be open
 * to the high seas; delivery waits without a fallback to a higher ID; retries
 * do not duplicate; and a rejected call changes no unit, ID, flag or notice.
 */
class InterventionDeliveryPortTest {

    private static final int TARGET = 500;

    /** New Amsterdam, the lowest-ID colony of the campaign. */
    private static final int LOWEST_COLONY = 0;

    /** Delft, the next colony by ID, which opens to the northern ocean. */
    private static final int NEXT_COLONY = 1;

    private static final Location NORTH_APPROACH = Location.of(26, 13);

    private static final Location SOUTH_APPROACH = Location.of(26, 14);

    /** Four squares from New Amsterdam, inside the corridor of the strait. */
    private static final Location CHOKEPOINT = Location.of(29, 13);

    private static final int BLOCKER_NORTH = 200;

    private static final int BLOCKER_SOUTH = 201;

    private static final int EIGHT = 8;

    /**
     * A declared Dutch nation, the Crown at war with it, and no intervention.
     */
    private static GameModelPo declared() {
        return RoyalScenarioFixtures.crown(Difficulty.CONQUISTADOR, true);
    }

    /** The saved Dutch target, earned and waiting for its delivery. */
    private static InterventionPo earnedPending() {
        final InterventionPo po = new InterventionPo();
        po.setTarget(TARGET);
        po.setBells((long) TARGET);
        po.setEarned(true);
        po.setDelivered(false);
        return po;
    }

    /** A declared nation whose intervention is earned and not yet delivered. */
    private static GameModelPo pending() {
        final GameModelPo data = declared();
        dutchPo(data).setIntervention(earnedPending());
        return data;
    }

    private static PlayerPo dutchPo(final GameModelPo data) {
        return data.getModel().getGameManager().getPlayers().stream().filter(
                player -> RoyalScenarioFixtures.REBELS.equals(player.getName()))
                .findFirst().orElseThrow();
    }

    private static Model load(final ModelPo po) {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(RoyalScenarioFixtures.REBELS);
    }

    /** Change squares of the authored map in saved data, before loading. */
    private static void setTerrain(final GameModelPo data,
            final TerrainType type, final Location... squares) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (final Location at : squares) {
            terrain.put(at, type);
        }
        data.getModel().getMap().setTerrainType(terrain);
    }

    /**
     * The two sea squares beside New Amsterdam become a fresh-water lake that
     * land closes on every side: the colony still touches water, but no ship
     * reaches it from the high seas.
     */
    private static void closeLakeBesideLowestColony(final GameModelPo data) {
        setTerrain(data, TerrainType.LAKE, NORTH_APPROACH, SOUTH_APPROACH);
        setTerrain(data, TerrainType.PRAIRIE, Location.of(26, 12),
                Location.of(27, 12), Location.of(27, 13), Location.of(27, 14),
                Location.of(27, 15));
    }

    /**
     * The same two squares stay ocean, and a corridor one square wide runs east
     * along y=13 from x=27 to x=30 between walls of land; it opens to the sea
     * at x=31. Whoever stands in the corridor closes the only way in.
     */
    private static void narrowStraitBesideLowestColony(final GameModelPo data) {
        setTerrain(data, TerrainType.PRAIRIE, Location.of(26, 12),
                Location.of(27, 12), Location.of(28, 12), Location.of(29, 12),
                Location.of(30, 12), Location.of(27, 14), Location.of(28, 14),
                Location.of(29, 14), Location.of(30, 14), Location.of(27, 15));
    }

    /** A saved Crown ship with empty holds, which stays where it is put. */
    private static UnitPo crownShip(final int id, final Location at) {
        return ship(id, RoyalScenarioFixtures.KING, UnitType.CARAVEL, at);
    }

    private static UnitPo ship(final int id, final String owner,
            final UnitType type, final Location at) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(owner);
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        unit.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int slot = 0; slot < type.getCargoCapacity(); slot++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        unit.setCargo(cargo);
        return unit;
    }

    private static Colony colony(final Model model, final int id) {
        return model.getColonyById(id).orElseThrow();
    }

    private static long menOfWarAt(final Model model, final Colony port) {
        return model.getUnitsAt(port.getLocation()).stream()
                .filter(unit -> unit.getOwner() == dutch(model))
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR).count();
    }

    private static List<UnitFreeColonist> deliveredSoldiers(
            final Player owner) {
        return owner.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitFreeColonist)
                .map(unit -> (UnitFreeColonist) unit)
                .filter(UnitFreeColonist::isInterventionDelivered)
                .collect(Collectors.toList());
    }

    private static Set<Integer> unitIds(final Model model) {
        return model.getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    /** Squares the Dutch have explored, as their saved state records them. */
    private static Set<Location> visibleTo(final Model model) {
        return dutch(model).save().getVisible().getVisibilitySet();
    }

    private static List<TurnEventInterventionArrival> arrivals(
            final Model model) {
        return model.getTurnEventsLocalizedMessages(dutch(model)).stream()
                .filter(TurnEventInterventionArrival.class::isInstance)
                .map(TurnEventInterventionArrival.class::cast)
                .collect(Collectors.toList());
    }

    /**
     * Nothing was delivered: no provenance soldier, no man-of-war, no notice.
     */
    private static void assertNothingDelivered(final Model model) {
        final Player dutch = dutch(model);
        assertFalse(dutch.getIntervention().orElseThrow().isDelivered());
        assertEquals(0, deliveredSoldiers(dutch).size());
        assertEquals(0L, model.getAllUnits().stream()
                .filter(unit -> unit.getOwner() == dutch)
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR).count());
        assertEquals(0, arrivals(model).size());
    }

    /** All eight units stand at the colony, with exact types and roles. */
    private static void assertDeliveredAt(final Model model,
            final Colony port) {
        final Player dutch = dutch(model);
        assertTrue(dutch.getIntervention().orElseThrow().isDelivered());
        assertEquals(2L, menOfWarAt(model, port));
        final List<UnitFreeColonist> soldiers = deliveredSoldiers(dutch);
        assertEquals(6, soldiers.size());
        assertEquals(4L, soldiers.stream()
                .filter(soldier -> !soldier.isMounted()).count());
        assertEquals(2L,
                soldiers.stream().filter(UnitFreeColonist::isMounted).count());
        for (final UnitFreeColonist soldier : soldiers) {
            assertSame(dutch, soldier.getOwner());
            assertEquals(MilitaryRole.CONTINENTAL, soldier.getMilitaryRole());
            assertTrue(soldier.isHoldingGuns());
            assertEquals(port.getLocation(), soldier.getLocation());
        }
        assertEquals(1, arrivals(model).size());
        assertEquals(port.getId(), arrivals(model).get(0).getColonyId());
    }

    @Test
    void lowestPortIsChosenAndEightDistinctUnitsHaveExactIdentities() {
        final Model model = load(pending().getModel());
        final Player dutch = dutch(model);
        final Set<Integer> before = unitIds(model);
        final int highest = before.stream().mapToInt(Integer::intValue).max()
                .orElseThrow();
        final Optional<Colony> port = model.deliverInterventionForces(dutch);
        assertTrue(port.isPresent());
        assertEquals(LOWEST_COLONY, port.get().getId());
        final Set<Integer> created = unitIds(model);
        created.removeAll(before);
        // Eight new, distinct, consecutive identities; nothing else changed.
        assertEquals(EIGHT, created.size());
        assertEquals(highest + 1, created.stream().mapToInt(Integer::intValue)
                .min().orElseThrow());
        assertEquals(highest + EIGHT, created.stream()
                .mapToInt(Integer::intValue).max().orElseThrow());
        assertDeliveredAt(model, colony(model, LOWEST_COLONY));
    }

    @Test
    void blockedLowestPortWaitsAndNoHigherPortTakesTheForces() {
        final GameModelPo data = pending();
        data.getModel().getUnits()
                .add(crownShip(BLOCKER_NORTH, NORTH_APPROACH));
        data.getModel().getUnits()
                .add(crownShip(BLOCKER_SOUTH, SOUTH_APPROACH));
        final Model model = load(data.getModel());
        final Player dutch = dutch(model);
        final Set<Integer> before = unitIds(model);
        final Set<Location> seen = visibleTo(model);
        assertTrue(model.deliverInterventionForces(dutch).isEmpty());
        assertEquals(before, unitIds(model));
        assertEquals(seen, visibleTo(model));
        assertNothingDelivered(model);
        assertTrue(dutch.startTurn());
        // The owner update tried again and still waits; the next port by ID
        // received nothing, and no identity was spent.
        assertNothingDelivered(model);
        assertEquals(0L, menOfWarAt(model, colony(model, NEXT_COLONY)));
        assertEquals(0L, menOfWarAt(model, colony(model, LOWEST_COLONY)));
        assertTrue(unitIds(model).containsAll(before));
        assertEquals(before.size(), unitIds(model).size());
    }

    @Test
    void oneFreeApproachIsEnoughToDeliver() {
        final GameModelPo data = pending();
        data.getModel().getUnits()
                .add(crownShip(BLOCKER_NORTH, NORTH_APPROACH));
        final Model model = load(data.getModel());
        assertTrue(model.deliverInterventionForces(dutch(model)).isPresent());
        assertDeliveredAt(model, colony(model, LOWEST_COLONY));
    }

    @Test
    void retryAfterTheBlockadeIsGoneDeliversOnceAtTheSamePort() {
        final GameModelPo data = pending();
        data.getModel().getUnits()
                .add(crownShip(BLOCKER_NORTH, NORTH_APPROACH));
        data.getModel().getUnits()
                .add(crownShip(BLOCKER_SOUTH, SOUTH_APPROACH));
        final Model blocked = load(data.getModel());
        assertTrue(dutch(blocked).startTurn());
        assertNothingDelivered(blocked);
        // The pending state survives Save and Continue unchanged.
        final ModelPo saved = blocked.save();
        final int unitsWhileBlocked = saved.getUnits().size();
        assertTrue(saved.getUnits()
                .removeIf(unit -> unit.getId() == BLOCKER_SOUTH));
        final Model open = load(saved);
        final Player dutch = dutch(open);
        assertFalse(dutch.getIntervention().orElseThrow().isDelivered());
        assertTrue(dutch.startTurn());
        assertEquals(unitsWhileBlocked - 1 + EIGHT, open.getAllUnits().size());
        assertDeliveredAt(open, colony(open, LOWEST_COLONY));
        // A later owner update, and a reload of the delivered state, add
        // nothing.
        assertTrue(dutch.startTurn());
        assertEquals(unitsWhileBlocked - 1 + EIGHT, open.getAllUnits().size());
        final Model restored = load(open.save());
        assertTrue(dutch(restored).startTurn());
        assertEquals(unitsWhileBlocked - 1 + EIGHT,
                restored.getAllUnits().size());
        assertEquals(6, deliveredSoldiers(dutch(restored)).size());
    }

    @Test
    void enemyFarUpTheOnlyApproachBlocksTheChosenPortWithoutFallback() {
        final GameModelPo data = pending();
        narrowStraitBesideLowestColony(data);
        data.getModel().getUnits().add(crownShip(BLOCKER_NORTH, CHOKEPOINT));
        final Model model = load(data.getModel());
        final Player dutch = dutch(model);
        // Neither approach square of the port holds an enemy; the corridor
        // does.
        assertTrue(model.getUnitsAt(NORTH_APPROACH).isEmpty());
        assertTrue(model.getUnitsAt(SOUTH_APPROACH).isEmpty());
        assertTrue(model.isOwnedCoastalPort(dutch,
                colony(model, LOWEST_COLONY).getLocation()));
        assertTrue(model.deliverInterventionForces(dutch).isEmpty());
        assertTrue(dutch.startTurn());
        assertNothingDelivered(model);
        // Delft opens to the open sea but is not the geographic choice.
        assertEquals(0L, menOfWarAt(model, colony(model, NEXT_COLONY)));
        // Once the corridor is clear the same port receives the forces.
        final ModelPo saved = model.save();
        assertTrue(saved.getUnits()
                .removeIf(unit -> unit.getId() == BLOCKER_NORTH));
        final Model open = load(saved);
        assertTrue(dutch(open).startTurn());
        assertDeliveredAt(open, colony(open, LOWEST_COLONY));
        assertEquals(0L, menOfWarAt(open, colony(open, NEXT_COLONY)));
    }

    @Test
    void enemyBesideTheStraitDoesNotBlockWhenTheCorridorStaysOpen() {
        final GameModelPo data = pending();
        narrowStraitBesideLowestColony(data);
        // Open water just outside the mouth of the corridor.
        data.getModel().getUnits()
                .add(crownShip(BLOCKER_NORTH, Location.of(32, 13)));
        final Model model = load(data.getModel());
        assertTrue(model.deliverInterventionForces(dutch(model)).isPresent());
        assertDeliveredAt(model, colony(model, LOWEST_COLONY));
    }

    @Test
    void inlandLakeColonyIsExcludedAndTheNextReachablePortIsUsed() {
        final GameModelPo data = pending();
        closeLakeBesideLowestColony(data);
        final Model model = load(data.getModel());
        final Player dutch = dutch(model);
        final Colony lakeside = colony(model, LOWEST_COLONY);
        // The lake counts as water for the colony, but the high seas do not
        // reach it, so nothing is placed there.
        assertTrue(model.isOwnedCoastalPort(dutch, lakeside.getLocation()));
        assertTrue(dutch.startTurn());
        assertEquals(0L, menOfWarAt(model, lakeside));
        assertDeliveredAt(model, colony(model, NEXT_COLONY));
        assertTrue(model.getUnitsAt(NORTH_APPROACH).isEmpty());
        assertTrue(model.getUnitsAt(SOUTH_APPROACH).isEmpty());
    }

    @Test
    void portCapturedByTheCrownMovesTheDeliveryToTheNextOwnedPort() {
        final Model model = load(pending().getModel());
        final Player dutch = dutch(model);
        final Player crown = model.getPlayerByName(RoyalScenarioFixtures.KING);
        final Colony lowest = colony(model, LOWEST_COLONY);
        // The Crown takes the lowest-ID port by a real defense battle, which
        // moves the colony and its residents together.
        final Unit invader = model.addUnitOutSideColony(lowest);
        invader.takeOver(crown);
        ColonyBattles.capture(model, lowest, invader);
        assertSame(crown, lowest.getOwner());
        assertTrue(dutch.startTurn());
        assertEquals(0L, menOfWarAt(model, lowest));
        assertDeliveredAt(model, colony(model, NEXT_COLONY));
    }

    /**
     * The next identity is one above the highest saved one, and
     * {@code Integer.MAX_VALUE} itself is never handed out. A highest saved
     * identity of {@code MAX - 8} leaves seven free identities, {@code MAX - 9}
     * leaves eight.
     */
    @Test
    void sevenFreeIdentitiesCreateNothingAndEightFreeDeliverAllEight() {
        // A saved Dutch ship in open water holds the highest identity.
        final GameModelPo tooFew = pending();
        tooFew.getModel().getUnits()
                .add(ship(Integer.MAX_VALUE - EIGHT,
                        RoyalScenarioFixtures.REBELS, UnitType.CARAVEL,
                        Location.of(35, 10)));
        final Model few = load(tooFew.getModel());
        final Set<Integer> before = unitIds(few);
        final Set<Location> seen = visibleTo(few);
        assertTrue(few.deliverInterventionForces(dutch(few)).isEmpty());
        assertEquals(before, unitIds(few));
        assertEquals(seen, visibleTo(few));
        assertTrue(dutch(few).startTurn());
        assertNothingDelivered(few);
        assertEquals(before, unitIds(few));
        // No identity was spent: the next unit still takes the first free one.
        assertEquals(Integer.MAX_VALUE - EIGHT + 1,
                few.addUnitOutSideColony(colony(few, NEXT_COLONY)).getId());

        final GameModelPo exact = pending();
        exact.getModel().getUnits()
                .add(ship(Integer.MAX_VALUE - EIGHT - 1,
                        RoyalScenarioFixtures.REBELS, UnitType.CARAVEL,
                        Location.of(35, 10)));
        final Model enough = load(exact.getModel());
        final Set<Integer> known = unitIds(enough);
        assertTrue(enough.deliverInterventionForces(dutch(enough)).isPresent());
        final Set<Integer> created = unitIds(enough);
        created.removeAll(known);
        assertEquals(EIGHT, created.size());
        assertEquals(Integer.MAX_VALUE - EIGHT, created.stream()
                .mapToInt(Integer::intValue).min().orElseThrow());
        assertEquals(Integer.MAX_VALUE - 1, created.stream()
                .mapToInt(Integer::intValue).max().orElseThrow());
        assertDeliveredAt(enough, colony(enough, LOWEST_COLONY));
    }

    @Test
    void foreignOrCrownOwnerIsRejectedBeforeAnyChange() {
        final Model model = load(pending().getModel());
        final Model other = load(pending().getModel());
        final Set<Integer> before = unitIds(model);
        assertThrows(IllegalArgumentException.class,
                () -> model.deliverInterventionForces(dutch(other)));
        assertThrows(IllegalArgumentException.class,
                () -> model.deliverInterventionForces(
                        model.getPlayerByName(RoyalScenarioFixtures.KING)));
        assertEquals(before, unitIds(model));
        assertNothingDelivered(model);
        assertNothingDelivered(other);
    }

    @Test
    void missingUnearnedAndDuplicateCallsKeepIdsCountAndFlag() {
        final Model model = load(declared().getModel());
        final Player dutch = dutch(model);
        final Set<Integer> start = unitIds(model);
        // No target exists before the first owner update.
        assertThrows(IllegalStateException.class,
                () -> model.deliverInterventionForces(dutch));
        assertTrue(dutch.startTurn());
        final InterventionTarget state = dutch.getIntervention().orElseThrow();
        assertFalse(state.isEarned());
        final Set<Integer> started = unitIds(model);
        assertTrue(started.containsAll(start));
        // A target that is not earned delivers nothing.
        assertThrows(IllegalStateException.class,
                () -> model.deliverInterventionForces(dutch));
        assertEquals(started, unitIds(model));
        assertFalse(state.isDelivered());

        dutch.creditProducedBells(
                Math.toIntExact(state.getTarget() - state.getBells()));
        // An ordinary armed colonist exists beside the delivery.
        final UnitFreeColonist ordinary = (UnitFreeColonist) model
                .addUnitOutSideColony(colony(model, NEXT_COLONY));
        ordinary.setHoldingGuns(true);
        final Set<Integer> beforeDelivery = unitIds(model);
        assertTrue(model.deliverInterventionForces(dutch).isPresent());
        final Set<Integer> delivered = unitIds(model);
        assertEquals(beforeDelivery.size() + EIGHT, delivered.size());
        // A second call, and the update after it, create nothing and change
        // neither flag nor notice.
        assertThrows(IllegalStateException.class,
                () -> model.deliverInterventionForces(dutch));
        assertTrue(dutch.startTurn());
        assertEquals(delivered, unitIds(model));
        assertTrue(state.isDelivered());
        assertEquals(1, arrivals(model).size());
        assertEquals(6, deliveredSoldiers(dutch).size());
        // The ordinary soldier gained no intervention origin or role.
        assertFalse(ordinary.isInterventionDelivered());
        assertEquals(MilitaryRole.COLONIAL, ordinary.getMilitaryRole());
    }
}
