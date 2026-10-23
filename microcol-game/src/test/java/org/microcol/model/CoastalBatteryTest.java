package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.CoastalBatteryShotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventCoastalBattery;
import org.microcol.model.turnevent.TurnEventShipRepair;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

/**
 * Coastal battery shots after legal ship steps (R-0177): strength, the exact
 * probability boundary, the eligibility matrix, the once-per-round ledger with
 * reload and the owner's next round, repair and cargo after a hit, and the
 * order of batteries before the patrol check.
 */
class CoastalBatteryTest {
    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    private static final int MOVER_ID = 120;
    private static final int PATROL_ID = 121;
    private static final int ARTILLERY_ID = 122;
    /** The French colony Quebec, the battery in the harbour fixture. */
    private static final Location PORT = Location.of(17, 10);
    /** Sea beside the port, the first square the frigate enters beside it. */
    private static final Location APPROACH = Location.of(18, 10);
    /** Open sea two squares from the port, where the frigate starts. */
    private static final Location OPEN_SEA = Location.of(19, 10);
    /** Sea beside the port again, one step after the approach. */
    private static final Location PORT_PATROL = Location.of(18, 9);
    /** Sea beside the approach but two squares from the port. */
    private static final Location APPROACH_PATROL = Location.of(19, 11);
    /** Land beside the approach, free of colonies in the bundled map. */
    private static final Location SECOND_PORT = Location.of(17, 11);
    private static final String MAP = "/maps/free-play.microcol";

    /** A frigate's defense 16 against a fort's 4: a shot hits below 1/5. */
    private static final double FORT_HIT_CHANCE = 4.0 / 20.0;

    /** The bundled map with sea made explicit around Quebec. */
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

    private static UnitPo unit(final int id, final UnitType type,
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

    /** A Dutch frigate with ore in its second hold, starting at open sea. */
    private static UnitPo dutchFrigate() {
        final UnitPo ship = unit(MOVER_ID, UnitType.FRIGATE, "Dutch", OPEN_SEA);
        ship.getCargo().getSlots().get(1).setGoodsType(GoodsType.ORE);
        ship.getCargo().getSlots().get(1).setAmount(7);
        return ship;
    }

    private static ConstructionPo facility(final ConstructionType type) {
        final ConstructionPo facility = new ConstructionPo();
        facility.setType(type);
        return facility;
    }

    private static ColonyPo colonyAt(final ModelPo data, final Location at) {
        return data.getColonies().stream()
                .filter(colony -> colony.getLocation().equals(at)).findFirst()
                .orElseThrow();
    }

    /**
     * The harbour with the chosen facility on Quebec (if any), the extra units,
     * and a Dutch frigate that is the current player. The saved random state is
     * set after the opening turns; war with France is declared unless asked
     * otherwise.
     */
    private static Model build(final ModelPo data, final long state,
            final boolean war) throws ReflectiveOperationException {
        data.getUnits().add(dutchFrigate());
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        if (war) {
            model.declareHostilities(ship(model).getOwner(),
                    model.getColonyAt(PORT).orElseThrow().getOwner());
        }
        final Field field = Model.class.getDeclaredField("combatRandomState");
        field.setAccessible(true);
        field.setLong(model, state);
        return model;
    }

    private static Model game(final ConstructionType facility,
            final List<UnitPo> extras, final long state)
            throws ReflectiveOperationException {
        final ModelPo data = harbourData();
        if (facility != null) {
            colonyAt(data, PORT).getConstructions().add(facility(facility));
        }
        extras.forEach(data.getUnits()::add);
        return build(data, state, true);
    }

    /** The first saved state whose coming draw is a hit or a miss. */
    private static long stateFor(final boolean hit) {
        for (long state = 0;; state++) {
            if ((Model.combatRandomAfter(state) < FORT_HIT_CHANCE) == hit) {
                return state;
            }
        }
    }

    private static long savedState(final Model model) {
        return model.save().getCombatRandomState().longValue();
    }

    private static Ship ship(final Model model) {
        return (Ship) model.getUnitById(MOVER_ID);
    }

    private static List<TurnEventCoastalBattery> shotsOf(final Model model,
            final Ship ship) {
        return model.getTurnEventStore().getForPlayer(ship.getOwner()).stream()
                .filter(TurnEventCoastalBattery.class::isInstance)
                .map(TurnEventCoastalBattery.class::cast)
                .collect(Collectors.toList());
    }

    private static List<Optional<Goods>> goodsOf(final Ship ship) {
        return IntStream.range(0, ship.getType().getCargoCapacity()).mapToObj(
                index -> ship.getCargo().getSlotByIndex(index).getGoods())
                .collect(Collectors.toList());
    }

    @Test
    void fortAndFortressStrengthAddsArtilleryOfTheColonyOwnerOnly()
            throws ReflectiveOperationException {
        final Model fort = game(ConstructionType.FORT, List.of(), 0L);
        final CombatStrengths fortShot = CombatStrengths.forCoastalBattery(fort,
                fort.getColonyAt(PORT).orElseThrow(), ship(fort));
        assertEquals(4.0, fortShot.getAttacker().getTotal());
        assertEquals(16.0, fortShot.getDefender().getTotal());

        final Model fortress = game(ConstructionType.FORTRESS, List.of(), 0L);
        assertEquals(6.0,
                CombatStrengths.forCoastalBattery(fortress,
                        fortress.getColonyAt(PORT).orElseThrow(),
                        ship(fortress)).getAttacker().getTotal());

        final UnitPo full = unit(ARTILLERY_ID, UnitType.ARTILLERY, "France",
                PORT);
        full.setArtilleryDamaged(false);
        final UnitPo damaged = unit(ARTILLERY_ID + 1, UnitType.ARTILLERY,
                "France", PORT);
        damaged.setArtilleryDamaged(true);
        // Own artillery on the colony square counts; the same owner's
        // artillery on a neighbouring square does not, as it is not stationed
        // on the colony tile.
        final UnitPo beside = unit(ARTILLERY_ID + 2, UnitType.ARTILLERY,
                "France", SECOND_PORT);
        beside.setArtilleryDamaged(false);
        final Model garrisoned = game(ConstructionType.FORT,
                List.of(full, damaged, beside), 0L);
        assertEquals(4.0 + 2.0 + 1.0,
                CombatStrengths.forCoastalBattery(garrisoned,
                        garrisoned.getColonyAt(PORT).orElseThrow(),
                        ship(garrisoned)).getAttacker().getTotal());
    }

    @Test
    void exactProbabilityBoundaryMissesAndJustBelowHits()
            throws ReflectiveOperationException {
        final Model model = game(ConstructionType.FORT, List.of(), 0L);
        final CombatStrengths strengths = CombatStrengths.forCoastalBattery(
                model, model.getColonyAt(PORT).orElseThrow(), ship(model));
        assertEquals(FORT_HIT_CHANCE, strengths.getAttackerWinProbability(),
                0.0);
        assertFalse(strengths.attackerWins(FORT_HIT_CHANCE));
        assertTrue(strengths.attackerWins(Math.nextDown(FORT_HIT_CHANCE)));
    }

    @Test
    void stockadeFriendlyAndNonadjacentStepsNeverFire()
            throws ReflectiveOperationException {
        final long state = stateFor(true);

        final Model stockade = game(ConstructionType.STOCKADE, List.of(),
                state);
        ship(stockade).moveOneStep(APPROACH);
        assertTrue(shotsOf(stockade, ship(stockade)).isEmpty());
        assertEquals(state, savedState(stockade));

        final ModelPo peaceData = harbourData();
        colonyAt(peaceData, PORT).getConstructions()
                .add(facility(ConstructionType.FORT));
        final Model atPeace = build(peaceData, state, false);
        ship(atPeace).moveOneStep(APPROACH);
        assertTrue(shotsOf(atPeace, ship(atPeace)).isEmpty());
        assertEquals(state, savedState(atPeace));

        final Model nonadjacent = game(ConstructionType.FORT, List.of(), state);
        final Location away = OPEN_SEA.getNeighbors().stream()
                .filter(at -> !at.isNeighbor(PORT)
                        && nonadjacent.getMap().getTerrainTypeAt(at).isSea())
                .findFirst().orElseThrow();
        ship(nonadjacent).moveOneStep(away);
        assertTrue(shotsOf(nonadjacent, ship(nonadjacent)).isEmpty());
        assertEquals(state, savedState(nonadjacent));
    }

    @Test
    void twoAdjacentStepsFireOnceAtTheSameShip()
            throws ReflectiveOperationException {
        final Model model = game(ConstructionType.FORT, List.of(),
                stateFor(false));
        final Ship ship = ship(model);
        ship.moveOneStep(APPROACH);
        ship.moveOneStep(PORT_PATROL);
        assertEquals(PORT_PATROL, ship.getLocation());
        final List<TurnEventCoastalBattery> shots = shotsOf(model, ship);
        assertEquals(1, shots.size());
        assertFalse(shots.get(0).isHit());
    }

    @Test
    void ledgerSurvivesSaveAndContinueAndRenewsOnlyForTheColonyOwner()
            throws ReflectiveOperationException {
        final Model model = game(ConstructionType.FORT, List.of(),
                stateFor(false));
        ship(model).moveOneStep(APPROACH);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Ship saved = ship(restored);
        final long state = savedState(restored);
        restored.resolveCoastalBatteries(saved, PORT_PATROL);
        assertEquals(state, savedState(restored));
        assertEquals(1, shotsOf(restored, saved).size());

        // The ship owner's own turn does not renew the French battery.
        restored.renewCoastalBatteryShots(restored.getPlayerByName("Dutch"));
        restored.resolveCoastalBatteries(saved, PORT_PATROL);
        assertEquals(state, savedState(restored));
        assertEquals(1, shotsOf(restored, saved).size());

        restored.renewCoastalBatteryShots(restored.getPlayerByName("France"));
        restored.resolveCoastalBatteries(saved, PORT_PATROL);
        assertEquals(2, shotsOf(restored, saved).size());
    }

    /**
     * The real turn order renews the battery. The Dutch frigate draws the first
     * shot in the Dutch turn; Continue and every other player's turn keep the
     * record; only France's own turn start expires it. Ending the Dutch turn
     * clears the Dutch turn report, so the old notice is gone by then and the
     * second shot is proved by the stream advancing exactly one draw right
     * around it, by its new notice and by the ledger entry keyed to the colony
     * owner. No pending choice is skipped on the way.
     */
    @Test
    void colonyOwnerTurnStartRenewsTheShotInTheRealTurnOrder()
            throws ReflectiveOperationException {
        final Model model = game(ConstructionType.FORT, List.of(),
                stateFor(false));
        ship(model).moveOneStep(APPROACH);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Ship saved = ship(restored);
        final int quebecId = restored.getColonyAt(PORT).orElseThrow().getId();
        final long state = savedState(restored);
        restored.resolveCoastalBatteries(saved, PORT_PATROL);
        assertEquals(state, savedState(restored));
        assertEquals(1, shotsOf(restored, saved).size());

        int turns = 0;
        while (!restored.getCurrentPlayer().getName().equals("France")) {
            assertTrue(turns++ < 12, "France's turn never started");
            // Another player's turn leaves the French record in place.
            assertEquals(1, restored.save().getCoastalBatteryShots().size());
            assertFalse(restored.isAwaitingImmigrationChoice());
            assertFalse(restored.isAwaitingTaxDecision());
            restored.endTurn();
        }
        assertFalse(restored.isAwaitingImmigrationChoice());
        assertFalse(restored.isAwaitingTaxDecision());
        // France's own turn start expired its record; ending the Dutch turn
        // had already cleared the Dutch notice of the first shot.
        assertTrue(restored.save().getCoastalBatteryShots().isEmpty());
        assertTrue(shotsOf(restored, saved).isEmpty());

        final long beforeSecondShot = savedState(restored);
        restored.resolveCoastalBatteries(saved, PORT_PATROL);
        assertEquals(beforeSecondShot + GAMMA, savedState(restored));
        final List<TurnEventCoastalBattery> shots = shotsOf(restored, saved);
        assertEquals(1, shots.size());
        assertEquals(quebecId, shots.get(0).getColonyId());
        final List<CoastalBatteryShotPo> ledger = restored.save()
                .getCoastalBatteryShots();
        assertEquals(1, ledger.size());
        assertEquals(quebecId, ledger.get(0).getColonyId().intValue());
        assertEquals(MOVER_ID, ledger.get(0).getShipId().intValue());
        assertEquals("France", ledger.get(0).getOwnerName());
    }

    @Test
    void hitSendsShipToRepairWithItsCargoAndDrawsNoPatrol()
            throws ReflectiveOperationException {
        final Model model = game(
                ConstructionType.FORT, List.of(unit(PATROL_ID,
                        UnitType.PRIVATEER, "France", APPROACH_PATROL)),
                stateFor(true));
        final Ship ship = ship(model);
        final List<Optional<Goods>> goods = goodsOf(ship);
        final long state = savedState(model);
        ship.moveOneStep(APPROACH);
        assertTrue(ship.isDamaged());
        assertFalse(ship.isAtPlaceLocation());
        assertTrue(ship.getRepair().orElseThrow().isEuropeDestination());
        assertEquals(goods, goodsOf(ship));
        assertEquals(state + GAMMA, savedState(model));
        final List<TurnEventCoastalBattery> shots = shotsOf(model, ship);
        assertEquals(1, shots.size());
        assertTrue(shots.get(0).isHit());
    }

    @Test
    void missLeavesTheShipOnItsCourseAfterThePatrolDraw()
            throws ReflectiveOperationException {
        final Model model = game(
                ConstructionType.FORT, List.of(unit(PATROL_ID,
                        UnitType.PRIVATEER, "France", APPROACH_PATROL)),
                stateFor(false));
        final Ship ship = ship(model);
        final long state = savedState(model);
        ship.moveOneStep(APPROACH);
        assertTrue(ship.isAtPlaceLocation());
        assertFalse(shotsOf(model, ship).get(0).isHit());
        // One battery draw, then the patrol draw on the same step.
        assertEquals(state + 2 * GAMMA, savedState(model));
    }

    @Test
    void twoFortsFireInStableIdOrderAndAHitStopsTheSecond()
            throws ReflectiveOperationException {
        final ModelPo data = harbourData();
        colonyAt(data, PORT).getConstructions()
                .add(facility(ConstructionType.FORT));
        final ColonyPo second = secondFort(data);
        // Listed before Quebec, so only the colony ID decides who fires first.
        data.getColonies().add(0, second);
        final Model model = build(data, stateFor(true), true);
        final Ship ship = ship(model);
        final int quebecId = model.getColonyAt(PORT).orElseThrow().getId();
        ship.moveOneStep(APPROACH);
        final List<TurnEventCoastalBattery> shots = shotsOf(model, ship);
        assertEquals(1, shots.size());
        assertEquals(quebecId, shots.get(0).getColonyId());
        assertTrue(shots.get(0).isHit());
        assertTrue(ship.isDamaged());
    }

    /**
     * Quebec misses first, the second fort in ID order then hits, and the hit
     * repairs the ship. Both shots are recorded, in ID order, one draw each.
     */
    @Test
    void missFirstFortThenSecondFortHitsInIdOrder()
            throws ReflectiveOperationException {
        final ModelPo data = harbourData();
        colonyAt(data, PORT).getConstructions()
                .add(facility(ConstructionType.FORT));
        data.getColonies().add(0, secondFort(data));
        final long state = missThenHitState();
        final Model model = build(data, state, true);
        final Ship ship = ship(model);
        final int quebecId = model.getColonyAt(PORT).orElseThrow().getId();
        ship.moveOneStep(APPROACH);
        final List<TurnEventCoastalBattery> shots = shotsOf(model, ship);
        assertEquals(2, shots.size());
        assertEquals(quebecId, shots.get(0).getColonyId());
        assertFalse(shots.get(0).isHit());
        assertNotEquals(quebecId, shots.get(1).getColonyId());
        assertTrue(shots.get(1).isHit());
        assertTrue(ship.isDamaged());
        assertEquals(state + 2 * GAMMA, savedState(model));
    }

    /**
     * A colony with a fort beside the port: its ID is the next one the save
     * allocates, which the save then counts as taken.
     */
    private static ColonyPo secondFort(final ModelPo data) {
        // Every mandatory saved field comes from Quebec, which the bundled
        // map already loads as a valid French colony. Only the identity and
        // the square differ; no worker, field output or construction slot is
        // copied, so no unit is named twice.
        final ColonyPo quebec = colonyAt(data, PORT);
        final ColonyPo second = new ColonyPo();
        final int id = data.getNextColonyId();
        second.setId(id);
        data.setNextColonyId(id + 1);
        second.setName("Second fort");
        second.setOwnerName(quebec.getOwnerName());
        second.setLiberty(quebec.getLiberty());
        second.setLastColonialAffiliation(quebec.getLastColonialAffiliation());
        second.setTownCommonsGoods(quebec.getTownCommonsGoods());
        second.setLocation(SECOND_PORT);
        second.setColonyWarehouse(new HashMap<>(quebec.getColonyWarehouse()));
        second.setExportGoods(quebec.getExportGoods() == null ? null
                : new ArrayList<>(quebec.getExportGoods()));
        second.setEducationCourses(quebec.getEducationCourses() == null ? null
                : new ArrayList<>(quebec.getEducationCourses()));
        second.setConstructions(new ArrayList<>());
        // Quebec's fortification is not copied; the second colony gets its own.
        quebec.getConstructions().stream().filter(
                construction -> construction.getType() != ConstructionType.FORT)
                .map(construction -> facility(construction.getType()))
                .forEach(second.getConstructions()::add);
        second.getConstructions().add(facility(ConstructionType.FORT));
        return second;
    }

    /** A state that misses first and whose next draw hits. */
    private static long missThenHitState() {
        for (long state = 0;; state++) {
            if (Model.combatRandomAfter(state) >= FORT_HIT_CHANCE && Model
                    .combatRandomAfter(state + GAMMA) < FORT_HIT_CHANCE) {
                return state;
            }
        }
    }

    @Test
    void rejectedMoveChangesNeitherShipNorRandomStream()
            throws ReflectiveOperationException {
        final long state = stateFor(true);
        final Model model = game(ConstructionType.FORT, List.of(), state);
        final Ship ship = ship(model);
        assertThrows(IllegalArgumentException.class,
                () -> ship.moveOneStep(Location.of(0, 0)));
        assertEquals(OPEN_SEA, ship.getLocation());
        assertTrue(shotsOf(model, ship).isEmpty());
        assertEquals(state, savedState(model));
    }

    /**
     * A hit with no reachable repair destination follows the agreed R-0098
     * fallback: the damaged ship sinks and its unplundered cargo is lost with
     * it. The battery notice itself only reports the hit.
     */
    @Test
    void hitWithNoRepairDestinationSinksTheShipAndReportsItsLostCargo()
            throws ReflectiveOperationException {
        final ModelPo data = harbourData();
        colonyAt(data, PORT).getConstructions()
                .add(facility(ConstructionType.FORT));
        RebellionFixtures.holdFreePlayMontreal(data);
        // A declared-independence owner has no home market to repair in.
        RebellionFixtures.declare(data,
                data.getGameManager().getPlayers().stream()
                        .filter(player -> player.getName().equals("Dutch"))
                        .findFirst().orElseThrow());
        final Model model = build(data, stateFor(true), true);
        final Ship ship = ship(model);
        ship.moveOneStep(APPROACH);
        assertFalse(model.isUnitInGame(ship));
        final List<TurnEventCoastalBattery> shots = shotsOf(model, ship);
        assertEquals(1, shots.size());
        assertTrue(shots.get(0).isHit());
        final List<TurnEventShipRepair> sunk = model.getTurnEventStore()
                .getForPlayer(model.getPlayerByName("Dutch")).stream()
                .filter(TurnEventShipRepair.class::isInstance)
                .map(TurnEventShipRepair.class::cast)
                .filter(event -> event
                        .getKind() == TurnEventShipRepair.Kind.SUNK)
                .collect(Collectors.toList());
        assertEquals(1, sunk.size());
        assertEquals(List.of(Goods.of(GoodsType.ORE, 7)),
                sunk.get(0).getLostGoods());
    }

    /**
     * Loading refuses a shot record with an unallocated colony, an unknown
     * owner, a negative ship ID or a duplicate, and accepts the saved one.
     */
    @Test
    void persistedShotsAreValidatedOnLoad()
            throws ReflectiveOperationException {
        final Model model = game(ConstructionType.FORT, List.of(),
                stateFor(false));
        ship(model).moveOneStep(APPROACH);
        final CoastalBatteryShotPo shot = model.save().getCoastalBatteryShots()
                .get(0);
        assertNotNull(Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators()));

        assertRejected(model,
                shotPo(shot.getColonyId(), shot.getShipId(), "Nobody"));
        assertRejected(model, shotPo(model.save().getNextColonyId(),
                shot.getShipId(), shot.getOwnerName()));
        assertRejected(model,
                shotPo(shot.getColonyId(), -1, shot.getOwnerName()));
        assertRejected(model, shot, shot);
    }

    private static CoastalBatteryShotPo shotPo(final int colonyId,
            final int shipId, final String ownerName) {
        final CoastalBatteryShotPo po = new CoastalBatteryShotPo();
        po.setColonyId(colonyId);
        po.setShipId(shipId);
        po.setOwnerName(ownerName);
        return po;
    }

    /** Loads a fresh save of the model with the given shot records instead. */
    private static void assertRejected(final Model model,
            final CoastalBatteryShotPo... shots) {
        final ModelPo bad = model.save();
        bad.setCoastalBatteryShots(List.of(shots));
        assertThrows(IllegalArgumentException.class, () -> Model.make(bad,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
