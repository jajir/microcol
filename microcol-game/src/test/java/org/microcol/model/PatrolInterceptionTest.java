package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventPatrolInterception;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

/**
 * Patrol checks on legal ship steps: the three draw bands, eligibility and
 * selection of the patrol, and the exactly-once effects of one step.
 */
class PatrolInterceptionTest {
    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    private static final int MOVER_ID = 3;
    private static final int FIRST_PATROL_ID = 100;
    private static final int MOVER_FRENCH_ID = 102;
    private static final int PASSENGER_ID = 103;
    /** The French colony Quebec, an owned port once sea lies beside it. */
    private static final Location PORT = Location.of(17, 10);
    /** The sea square east of the port, the harbour approach. */
    private static final Location APPROACH = Location.of(18, 10);
    /** Open sea one square beyond the approach. */
    private static final Location OPEN_SEA = Location.of(19, 10);
    /** Beside the approach but two squares from the port. */
    private static final Location APPROACH_PATROL = Location.of(19, 11);
    /** Beside both the approach and the port itself. */
    private static final Location PORT_PATROL = Location.of(18, 9);
    /** Open ocean east of the harbour where a three-step route starts. */
    private static final Location ROUTE_START = Location.of(22, 10);
    /** Three open-ocean steps; only the first lies beside the patrol. */
    private static final List<Location> ROUTE = List.of(Location.of(23, 10),
            Location.of(24, 10), Location.of(25, 10));
    /** Beside the first route step, two squares from the later ones. */
    private static final Location ROUTE_PATROL = Location.of(22, 9);
    private static final String MAP = "/maps/free-play.microcol";

    /**
     * Open-water squares around the mover, found by the shared terrain rules.
     */
    private static final class Ground {
        private final Model terrain = Model.make(
                new GameModelPoDao().loadFromClassPath(MAP).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        private final Location start = terrain.getUnitById(MOVER_ID)
                .getLocation();
        /** Open water whose other neighbors hold no unit to intercept. */
        private final Location step = start.getNeighbors().stream().filter(
                at -> openWater(terrain, at) && quiet(terrain, start, at))
                .findFirst().orElseThrow();
        /** Open water beside the step, the mover's own square excluded. */
        private final List<Location> beside = step.getNeighbors().stream()
                .filter(at -> !at.equals(start) && openWater(terrain, at))
                .collect(Collectors.toList());
        /** Open water two squares from the step, not beside it. */
        private final Location far = step.getNeighbors().stream()
                .flatMap(at -> at.getNeighbors().stream())
                .filter(at -> !at.equals(step) && !at.equals(start)
                        && !at.isNeighbor(step) && openWater(terrain, at))
                .findFirst().orElseThrow();

        /** Every neighbor except the mover's own square is free of units. */
        private static boolean quiet(final Model terrain, final Location start,
                final Location at) {
            return at.getNeighbors().stream()
                    .filter(neighbor -> !neighbor.equals(start)).allMatch(
                            neighbor -> terrain.getUnitsAt(neighbor).isEmpty());
        }

        private static boolean openWater(final Model terrain,
                final Location at) {
            return terrain.getMap().isValid(at)
                    && UnitType.FRIGATE.canMoveAtTerrain(
                            terrain.getMap().getTerrainTypeAt(at))
                    && terrain.getUnitsAt(at).isEmpty();
        }
    }

    /** Same unit-building pattern as the privateer naval tests. */
    private static UnitPo patrol(final int id, final UnitType type,
            final String owner, final Location at) {
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
        for (int i = 0; i < type.getCargoCapacity(); i++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        unit.setCargo(cargo);
        return unit;
    }

    /**
     * A live Dutch turn with the given patrols beside the map, and the saved
     * random state chosen after the opening turns so no earlier draw shifts it.
     */
    private static Model game(final List<UnitPo> patrols, final long state)
            throws ReflectiveOperationException {
        return game(patrols, state, null);
    }

    /**
     * As {@link #game(List, long)}, with the Dutch and French relation set
     * before the model is built when a state is given. The bundled units all
     * stay: every reference in the saved graph remains valid.
     */
    private static Model game(final List<UnitPo> patrols, final long state,
            final EuropeanRelationState relation)
            throws ReflectiveOperationException {
        final ModelPo data = new GameModelPoDao().loadFromClassPath(MAP)
                .getModel();
        if (relation != null) {
            data.getEuropeanRelations().stream()
                    .filter(row -> Set
                            .of(row.getFirstPlayer(), row.getSecondPlayer())
                            .equals(Set.of("Dutch", "France")))
                    .findFirst().orElseThrow().setState(relation);
        }
        patrols.forEach(data.getUnits()::add);
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        final Field field = Model.class.getDeclaredField("combatRandomState");
        field.setAccessible(true);
        field.setLong(model, state);
        return model;
    }

    /**
     * The bundled free-play map with sea made explicit around the French colony
     * Quebec, which has only land around it. The ocean squares are the approach
     * east of the port, open sea beyond it and the two patrol squares; the
     * worked field west of the port stays land, and any tree or plowed field
     * marker on a converted square is dropped with its land.
     */
    private static ModelPo harbourData() {
        final ModelPo data = new GameModelPoDao().loadFromClassPath(MAP)
                .getModel();
        final List<Location> sea = List.of(APPROACH, OPEN_SEA, APPROACH_PATROL,
                PORT_PATROL);
        final var terrain = data.getMap().getTerrainMap();
        sea.forEach(at -> terrain.put(at, TerrainType.OCEAN));
        data.getMap().setTerrainType(terrain);
        final Set<Location> fields = data.getMap().getFieldSet();
        if (fields.removeAll(sea)) {
            data.getMap().setFields(fields);
        }
        final Set<Location> trees = data.getMap().getTreeSet();
        if (trees.removeAll(sea)) {
            data.getMap().setTrees(trees);
        }
        return data;
    }

    /**
     * A French frigate that carries a French colonist in its first hold and
     * seven ore in its second, so cargo and landing can be checked with the
     * saved graph valid: the passenger names its hold and the hold names the
     * passenger.
     */
    private static UnitPo frenchFrigate(final Location from) {
        final UnitPo ship = patrol(MOVER_FRENCH_ID, UnitType.FRIGATE, "France",
                from);
        ship.getCargo().getSlots().get(0).setUnitId(PASSENGER_ID);
        ship.getCargo().getSlots().get(1).setGoodsType(GoodsType.ORE);
        ship.getCargo().getSlots().get(1).setAmount(7);
        return ship;
    }

    private static UnitPo frenchPassenger() {
        final UnitPo colonist = new UnitPo();
        colonist.setId(PASSENGER_ID);
        colonist.setType(UnitType.COLONIST);
        colonist.setOwnerId("France");
        colonist.setWorkUpdates(0);
        colonist.setMissionary(false);
        colonist.setAvailableMoves(UnitType.COLONIST.getSpeed());
        colonist.setAction(new UnitActionNoAction().save());
        colonist.setCargo(new CargoPo());
        colonist.setPlaceCargoSlot(new PlaceCargoSlotPo());
        return colonist;
    }

    /**
     * The harbour fixture: a French frigate starts at the given square, and a
     * Dutch privateer at peace with France waits at the patrol square. The
     * first turn belongs to France, so the frigate's own step is legal.
     */
    private static Model harbourGame(final Location from,
            final Location patrolAt, final long state)
            throws ReflectiveOperationException {
        final ModelPo data = harbourData();
        data.getUnits().add(frenchPassenger());
        data.getUnits().add(frenchFrigate(from));
        data.getUnits().add(
                patrol(FIRST_PATROL_ID, UnitType.PRIVATEER, "Dutch", patrolAt));
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("France"); i++) {
            model.endTurn();
        }
        assertEquals("France", model.getCurrentPlayer().getName());
        final Field field = Model.class.getDeclaredField("combatRandomState");
        field.setAccessible(true);
        field.setLong(model, state);
        return model;
    }

    /** The first saved state whose coming draw falls in the given band. */
    private static long stateFor(final ShipPatrolRules.Outcome outcome) {
        for (long state = 0;; state++) {
            if (ShipPatrolRules
                    .draw(Model.combatRandomAfter(state)) == outcome) {
                return state;
            }
        }
    }

    private static List<Optional<Goods>> goodsOf(final Ship ship) {
        return IntStream.range(0, ship.getType().getCargoCapacity()).mapToObj(
                index -> ship.getCargo().getSlotByIndex(index).getGoods())
                .collect(Collectors.toList());
    }

    private static List<Optional<Unit>> unitsOf(final Ship ship) {
        return IntStream.range(0, ship.getType().getCargoCapacity()).mapToObj(
                index -> ship.getCargo().getSlotByIndex(index).getUnit())
                .collect(Collectors.toList());
    }

    /** The patrol interceptions recorded for the mover's owner. */
    private static List<TurnEventPatrolInterception> interceptionsOf(
            final Model model, final Ship mover) {
        return model.getTurnEventStore().getForPlayer(mover.getOwner()).stream()
                .filter(TurnEventPatrolInterception.class::isInstance)
                .map(TurnEventPatrolInterception.class::cast)
                .collect(Collectors.toList());
    }

    private static long savedState(final Model model) {
        return model.save().getCombatRandomState().longValue();
    }

    @Test
    void drawBandsSplitAtOneHalfAndThreeQuarters() {
        assertEquals(ShipPatrolRules.Outcome.UNDETECTED,
                ShipPatrolRules.draw(0.0));
        assertEquals(ShipPatrolRules.Outcome.UNDETECTED,
                ShipPatrolRules.draw(0.4999));
        assertEquals(ShipPatrolRules.Outcome.SLOWED, ShipPatrolRules.draw(0.5));
        assertEquals(ShipPatrolRules.Outcome.SLOWED,
                ShipPatrolRules.draw(0.7499));
        assertEquals(ShipPatrolRules.Outcome.STOPPED,
                ShipPatrolRules.draw(0.75));
        assertEquals(ShipPatrolRules.Outcome.STOPPED,
                ShipPatrolRules.draw(0.9999));
        assertThrows(IllegalArgumentException.class,
                () -> ShipPatrolRules.draw(1.0));
        assertThrows(IllegalArgumentException.class,
                () -> ShipPatrolRules.draw(-0.1));
    }

    @Test
    void slowdownRemovesOnePointAndStopRemovesTheRest() {
        assertEquals(6, ShipPatrolRules
                .remainingAfter(ShipPatrolRules.Outcome.UNDETECTED, 6));
        assertEquals(3, ShipPatrolRules
                .remainingAfter(ShipPatrolRules.Outcome.SLOWED, 6));
        assertEquals(0, ShipPatrolRules
                .remainingAfter(ShipPatrolRules.Outcome.SLOWED, 2));
        assertEquals(0, ShipPatrolRules
                .remainingAfter(ShipPatrolRules.Outcome.SLOWED, 0));
        assertEquals(0, ShipPatrolRules
                .remainingAfter(ShipPatrolRules.Outcome.STOPPED, 6));
        assertThrows(IllegalArgumentException.class, () -> ShipPatrolRules
                .remainingAfter(ShipPatrolRules.Outcome.SLOWED, -1));
    }

    @Test
    void undetectedStepPaysOnceAndConsumesOneDrawWithoutNotice()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final long state = stateFor(ShipPatrolRules.Outcome.UNDETECTED);
        final Model model = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "France", ground.beside.get(0))), state);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        final int before = mover.getMovementThirds();
        mover.moveOneStep(ground.step);
        assertEquals(before - MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
        assertEquals(state + GAMMA, savedState(model));
        assertTrue(model.getTurnEventStore().getForPlayer(mover.getOwner())
                .stream()
                .noneMatch(TurnEventPatrolInterception.class::isInstance));
    }

    @Test
    void slowedStepRemovesOneRemainingPointAndNotesIt()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model model = game(
                List.of(patrol(FIRST_PATROL_ID, UnitType.PRIVATEER, "France",
                        ground.beside.get(0))),
                stateFor(ShipPatrolRules.Outcome.SLOWED));
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        final int before = mover.getMovementThirds();
        mover.moveOneStep(ground.step);
        assertEquals(before - 2 * MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
        assertTrue(model.getTurnEventStore().getForPlayer(mover.getOwner())
                .stream()
                .anyMatch(event -> event instanceof TurnEventPatrolInterception
                        && !((TurnEventPatrolInterception) event).isStopped()));
    }

    @Test
    void slowdownFloorsAtZeroWhenTheStepLeavesLessThanAPoint()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model model = game(
                List.of(patrol(FIRST_PATROL_ID, UnitType.PRIVATEER, "France",
                        ground.beside.get(0))),
                stateFor(ShipPatrolRules.Outcome.SLOWED));
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        mover.setMovementThirds(4);
        mover.moveOneStep(ground.step);
        assertEquals(0, mover.getMovementThirds());
    }

    @Test
    void stoppedStepRemovesAllRemainingMovement()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "France", ground.beside.get(0))), state);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        mover.moveOneStep(ground.step);
        assertEquals(0, mover.getMovementThirds());
        assertEquals(state + GAMMA, savedState(model));
        assertTrue(model.getTurnEventStore().getForPlayer(mover.getOwner())
                .stream()
                .anyMatch(event -> event instanceof TurnEventPatrolInterception
                        && ((TurnEventPatrolInterception) event).isStopped()));
    }

    @Test
    void strongestEligiblePatrolIsChosenThenLowestId()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        assertTrue(ground.beside.size() >= 2, "Two open squares beside step");
        final Model model = game(List.of(
                patrol(FIRST_PATROL_ID, UnitType.FRIGATE, "France",
                        ground.beside.get(0)),
                patrol(FIRST_PATROL_ID + 1, UnitType.MAN_OF_WAR, "France",
                        ground.beside.get(1))),
                0L);
        model.declareHostilities(model.getUnitById(MOVER_ID).getOwner(),
                model.getUnitById(FIRST_PATROL_ID).getOwner());
        assertEquals(FIRST_PATROL_ID + 1, model
                .findPatrol((Ship) model.getUnitById(MOVER_ID), ground.step)
                .orElseThrow().getId());

        final Model tied = game(List.of(
                patrol(FIRST_PATROL_ID + 1, UnitType.FRIGATE, "France",
                        ground.beside.get(0)),
                patrol(FIRST_PATROL_ID, UnitType.FRIGATE, "France",
                        ground.beside.get(1))),
                0L);
        tied.declareHostilities(tied.getUnitById(MOVER_ID).getOwner(),
                tied.getUnitById(FIRST_PATROL_ID).getOwner());
        assertEquals(FIRST_PATROL_ID,
                tied.findPatrol((Ship) tied.getUnitById(MOVER_ID), ground.step)
                        .orElseThrow().getId());
    }

    @Test
    void privateerInterceptsAtPeaceButOrdinaryArmedShipDoesNot()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model privateer = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "France", ground.beside.get(0))), 0L);
        assertTrue(privateer
                .findPatrol((Ship) privateer.getUnitById(MOVER_ID), ground.step)
                .isPresent());

        final Model ordinary = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.FRIGATE, "France", ground.beside.get(0))), 0L);
        final Ship mover = (Ship) ordinary.getUnitById(MOVER_ID);
        final long state = savedState(ordinary);
        final int before = mover.getMovementThirds();
        assertTrue(ordinary.findPatrol(mover, ground.step).isEmpty());
        mover.moveOneStep(ground.step);
        assertEquals(state, savedState(ordinary));
        assertEquals(before - MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
    }

    @Test
    void ordinaryArmedShipInterceptsOnlyADeclaredWartimeEnemy()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model model = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.FRIGATE, "France", ground.beside.get(0))), 0L);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        assertTrue(model.findPatrol(mover, ground.step).isEmpty());
        model.declareHostilities(mover.getOwner(),
                model.getUnitById(FIRST_PATROL_ID).getOwner());
        assertTrue(model.findPatrol(mover, ground.step).isPresent());
    }

    @Test
    void friendlyUnarmedAndNonadjacentShipsNeverIntercept()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model friendly = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "Dutch", ground.beside.get(0))), 0L);
        assertTrue(friendly
                .findPatrol((Ship) friendly.getUnitById(MOVER_ID), ground.step)
                .isEmpty());

        final Model unarmed = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.CARAVEL, "France", ground.beside.get(0))), 0L);
        unarmed.declareHostilities(unarmed.getUnitById(MOVER_ID).getOwner(),
                unarmed.getUnitById(FIRST_PATROL_ID).getOwner());
        assertTrue(unarmed
                .findPatrol((Ship) unarmed.getUnitById(MOVER_ID), ground.step)
                .isEmpty());

        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model nonadjacent = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "France", ground.far)), state);
        final Ship mover = (Ship) nonadjacent.getUnitById(MOVER_ID);
        mover.moveOneStep(ground.step);
        assertTrue(mover.getMovementThirds() > 0);
        assertEquals(state, savedState(nonadjacent));
    }

    @Test
    void invalidStepChangesNeitherMovementNorRandomStream()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "France", ground.beside.get(0))), state);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        final int before = mover.getMovementThirds();
        assertThrows(IllegalArgumentException.class,
                () -> mover.moveOneStep(Location.of(0, 0)));
        assertEquals(before, mover.getMovementThirds());
        assertEquals(state, savedState(model));
    }

    @Test
    void stoppedPathWalksOnlyTheFirstStep()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        assertTrue(ground.beside.size() >= 2, "Two open squares beside step");
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = game(List.of(patrol(FIRST_PATROL_ID,
                UnitType.PRIVATEER, "France", ground.beside.get(0))), state);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        model.moveUnit(mover,
                Path.of(List.of(ground.step, ground.beside.get(1))));
        assertEquals(ground.step, mover.getLocation());
        assertEquals(0, mover.getMovementThirds());
        assertEquals(state + GAMMA, savedState(model));
    }

    @Test
    void stopSurvivesSaveAndContinueWithoutRedrawing()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model model = game(
                List.of(patrol(FIRST_PATROL_ID, UnitType.PRIVATEER, "France",
                        ground.beside.get(0))),
                stateFor(ShipPatrolRules.Outcome.STOPPED));
        ((Ship) model.getUnitById(MOVER_ID)).moveOneStep(ground.step);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(0, restored.getUnitById(MOVER_ID).getMovementThirds());
        assertEquals(model.save().getCombatRandomState(),
                restored.save().getCombatRandomState());
    }

    @Test
    void interceptionLeavesCargoUntouched()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final Model model = game(
                List.of(patrol(FIRST_PATROL_ID, UnitType.PRIVATEER, "France",
                        ground.beside.get(0))),
                stateFor(ShipPatrolRules.Outcome.STOPPED));
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        final List<Optional<Goods>> goods = goodsOf(mover);
        final List<Optional<Unit>> passengers = unitsOf(mover);
        mover.moveOneStep(ground.step);
        assertEquals(goods, goodsOf(mover));
        assertEquals(passengers, unitsOf(mover));
    }

    @Test
    void stopRecordsTheSelectedStrongestPatrolAndDrawsOnce()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = game(List.of(
                patrol(FIRST_PATROL_ID, UnitType.FRIGATE, "France",
                        ground.beside.get(0)),
                patrol(FIRST_PATROL_ID + 1, UnitType.MAN_OF_WAR, "France",
                        ground.beside.get(1))),
                state);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        model.declareHostilities(mover.getOwner(),
                model.getUnitById(FIRST_PATROL_ID).getOwner());
        mover.moveOneStep(ground.step);
        final List<TurnEventPatrolInterception> notices = interceptionsOf(model,
                mover);
        assertEquals(1, notices.size());
        assertEquals(FIRST_PATROL_ID + 1, notices.get(0).getPatrolId());
        assertTrue(notices.get(0).isStopped());
        assertEquals(state + GAMMA, savedState(model));
    }

    @Test
    void alliedPatrolNeverInterceptsAndDrawsNothing()
            throws ReflectiveOperationException {
        final Ground ground = new Ground();
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = game(
                List.of(patrol(FIRST_PATROL_ID, UnitType.PRIVATEER, "France",
                        ground.beside.get(0))),
                state, EuropeanRelationState.ALLIANCE);
        final Ship mover = (Ship) model.getUnitById(MOVER_ID);
        assertTrue(model.areAllied(mover.getOwner(),
                model.getUnitById(FIRST_PATROL_ID).getOwner()));
        assertTrue(model.findPatrol(mover, ground.step).isEmpty());
        final int before = mover.getMovementThirds();
        mover.moveOneStep(ground.step);
        assertEquals(before - MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
        assertEquals(state, savedState(model));
        assertTrue(interceptionsOf(model, mover).isEmpty());
    }

    @Test
    void harbourFixtureMakesQuebecAnOwnedPortWithOpenSeaBesideIt() {
        final Model model = Model.make(harbourData(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = model.getColonyAt(PORT).orElseThrow().getOwner();
        assertEquals("France", france.getName());
        assertTrue(model.isOwnedCoastalPort(france, PORT));
        for (final Location sea : List.of(APPROACH, OPEN_SEA, APPROACH_PATROL,
                PORT_PATROL)) {
            assertTrue(Ground.openWater(model, sea), sea + " must be open sea");
        }
        assertTrue(APPROACH.isNeighbor(PORT));
        assertTrue(PORT_PATROL.isNeighbor(PORT));
        assertTrue(APPROACH_PATROL.isNeighbor(APPROACH));
        assertFalse(APPROACH_PATROL.isNeighbor(PORT));
    }

    /**
     * The harbour approach is an ordinary legal step: the patrol beside the
     * approach, two squares from the port, stops the frigate there. Nothing is
     * landed because the frigate has not entered the port.
     */
    @Test
    void harbourApproachStepIsCheckedBesideAPatrol()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = harbourGame(OPEN_SEA, APPROACH_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        final List<Optional<Goods>> goods = goodsOf(mover);
        final List<Optional<Unit>> passengers = unitsOf(mover);
        mover.moveOneStep(APPROACH);
        assertEquals(APPROACH, mover.getLocation());
        assertEquals(0, mover.getMovementThirds());
        final List<TurnEventPatrolInterception> notices = interceptionsOf(model,
                mover);
        assertEquals(1, notices.size());
        assertEquals(FIRST_PATROL_ID, notices.get(0).getPatrolId());
        assertTrue(notices.get(0).isStopped());
        assertEquals(state + GAMMA, savedState(model));
        assertEquals(goods, goodsOf(mover));
        assertEquals(passengers, unitsOf(mover));
    }

    /**
     * The entry itself is checked, not only the approach: stepping onto the
     * owned port beside a patrol still lands the passenger and keeps the goods,
     * then the stop removes the rest of the movement. One draw is consumed and
     * Continue neither redraws nor repeats the notice.
     */
    @Test
    void ownedPortEntryIsCheckedBesideAPatrolAndStillUnloads()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = harbourGame(APPROACH, PORT_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        assertTrue(model.isOwnedCoastalPort(mover.getOwner(), PORT));
        final List<Optional<Goods>> goods = goodsOf(mover);
        assertTrue(unitsOf(mover).get(0).isPresent());
        mover.moveOneStep(PORT);
        assertEquals(PORT, mover.getLocation());
        assertEquals(0, mover.getMovementThirds());
        final Unit passenger = model.getUnitById(PASSENGER_ID);
        assertTrue(passenger.isAtPlaceLocation());
        assertEquals(PORT, passenger.getLocation());
        assertTrue(unitsOf(mover).stream().allMatch(Optional::isEmpty));
        assertEquals(goods, goodsOf(mover));
        final List<TurnEventPatrolInterception> notices = interceptionsOf(model,
                mover);
        assertEquals(1, notices.size());
        assertEquals(FIRST_PATROL_ID, notices.get(0).getPatrolId());
        assertTrue(notices.get(0).isStopped());
        assertEquals(state + GAMMA, savedState(model));

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Ship again = (Ship) restored.getUnitById(MOVER_FRENCH_ID);
        assertEquals(PORT, again.getLocation());
        assertEquals(0, again.getMovementThirds());
        assertEquals(PORT, restored.getUnitById(PASSENGER_ID).getLocation());
        assertEquals(goods, goodsOf(again));
        assertEquals(state + GAMMA, savedState(restored));
        final List<TurnEventPatrolInterception> kept = interceptionsOf(restored,
                again);
        assertEquals(1, kept.size());
        assertEquals(FIRST_PATROL_ID, kept.get(0).getPatrolId());
    }

    @Test
    void undetectedPortEntryStillDrawsOnceAndKeepsMovementAndLanding()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.UNDETECTED);
        final Model model = harbourGame(APPROACH, PORT_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        final int before = mover.getMovementThirds();
        final List<Optional<Goods>> goods = goodsOf(mover);
        mover.moveOneStep(PORT);
        assertEquals(before - MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
        assertEquals(PORT, model.getUnitById(PASSENGER_ID).getLocation());
        assertEquals(goods, goodsOf(mover));
        assertEquals(state + GAMMA, savedState(model));
        assertTrue(interceptionsOf(model, mover).isEmpty());
    }

    @Test
    void patrolBesideOnlyTheApproachDoesNotCheckThePortEntry()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.STOPPED);
        final Model model = harbourGame(APPROACH, APPROACH_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        final int before = mover.getMovementThirds();
        mover.moveOneStep(PORT);
        assertEquals(before - MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
        assertEquals(PORT, model.getUnitById(PASSENGER_ID).getLocation());
        assertEquals(state, savedState(model));
        assertTrue(interceptionsOf(model, mover).isEmpty());
    }

    /**
     * A slowdown on the first step of a route, a safe middle step, then a last
     * step the slowdown left unpaid: the walk ends cleanly on the middle
     * square. The route was affordable when it was given; only the patrol made
     * the last step unaffordable.
     */
    @Test
    void routeSlowedEarlyStopsBeforeTheStepItCanNoLongerPay()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.SLOWED);
        final Model model = harbourGame(ROUTE_START, ROUTE_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        mover.setMovementThirds(3 * MovementPoints.THIRDS_PER_POINT);
        final List<Optional<Goods>> goods = goodsOf(mover);
        model.moveUnit(mover, Path.of(ROUTE));
        assertEquals(ROUTE.get(1), mover.getLocation());
        assertEquals(0, mover.getMovementThirds());
        assertEquals(state + GAMMA, savedState(model));
        final List<TurnEventPatrolInterception> notices = interceptionsOf(model,
                mover);
        assertEquals(1, notices.size());
        assertFalse(notices.get(0).isStopped());
        assertEquals(FIRST_PATROL_ID, notices.get(0).getPatrolId());
        assertEquals(goods, goodsOf(mover));
    }

    /** A slowdown that leaves enough movement lets the route finish. */
    @Test
    void routeSlowedEarlyWithMovementToSpareStillFinishes()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.SLOWED);
        final Model model = harbourGame(ROUTE_START, ROUTE_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        mover.setMovementThirds(4 * MovementPoints.THIRDS_PER_POINT);
        model.moveUnit(mover, Path.of(ROUTE));
        assertEquals(ROUTE.get(2), mover.getLocation());
        assertEquals(0, mover.getMovementThirds());
        assertEquals(state + GAMMA, savedState(model));
        assertEquals(1, interceptionsOf(model, mover).size());
    }

    /**
     * A route the ship cannot pay for when it is given is still refused before
     * any step, draw or notice, whether or not a patrol waits beside it.
     */
    @Test
    void routeUnaffordableFromTheStartIsRefusedBeforeAnyStep()
            throws ReflectiveOperationException {
        final long state = stateFor(ShipPatrolRules.Outcome.SLOWED);
        final Model model = harbourGame(ROUTE_START, ROUTE_PATROL, state);
        final Ship mover = (Ship) model.getUnitById(MOVER_FRENCH_ID);
        mover.setMovementThirds(2 * MovementPoints.THIRDS_PER_POINT);
        assertThrows(IllegalStateException.class,
                () -> model.moveUnit(mover, Path.of(ROUTE)));
        assertEquals(ROUTE_START, mover.getLocation());
        assertEquals(2 * MovementPoints.THIRDS_PER_POINT,
                mover.getMovementThirds());
        assertEquals(state, savedState(model));
        assertTrue(interceptionsOf(model, mover).isEmpty());
    }
}
