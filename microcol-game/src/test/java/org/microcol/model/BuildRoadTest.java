package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.UnitActionBuildRoadPo;
import org.microcol.model.unit.UnitActionBuildRoad;
import org.microcol.model.unit.UnitActionConverter;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Road building: admission on every square type, the exact tool boundary,
 * ordinary and hardy timing, the single completion transaction, cancellation,
 * stale completion and persistence.
 */
class BuildRoadTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    /** Squares used by the tests lie this far from every colony. */
    private static final int COLONY_CLEARANCE = 4;

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private final Gson gson = new Gson();

    private int saves;

    /** Land square used by the pioneer, chosen away from colony workers. */
    private Location target;

    /** Second far square, used for a second pioneer or a second road. */
    private Location other;

    /**
     * Pick three land squares in a row, each far from every colony, so that no
     * resident colony worker stands on a square this test changes.
     */
    private static List<Location> remoteRun(final GameModelPo data) {
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final List<Location> colonies = data.getModel().getColonies().stream()
                .map(ColonyPo::getLocation).collect(Collectors.toList());
        final int maxX = data.getModel().getMap().getMaxX();
        final int maxY = data.getModel().getMap().getMaxY();
        for (int y = 1; y <= maxY; y++) {
            for (int x = 1; x <= maxX - 2; x++) {
                final List<Location> run = List.of(Location.of(x, y),
                        Location.of(x + 1, y), Location.of(x + 2, y));
                final boolean usable = run.stream().allMatch(at -> types
                        .containsKey(at) && types.get(at).isLand()
                        && colonies.stream().allMatch(colony -> colony
                                .getDistanceChebyshev(at) >= COLONY_CLEARANCE));
                if (usable) {
                    return run;
                }
            }
        }
        throw new IllegalStateException("No remote land squares in the map");
    }

    private GameModelPo data(final TerrainType terrain) {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final List<Location> run = remoteRun(data);
        target = run.get(0);
        other = run.get(2);
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final Set<Location> trees = new HashSet<>(
                data.getModel().getMap().getTreeSet());
        final Set<Location> fields = new HashSet<>(
                data.getModel().getMap().getFieldSet());
        for (final Location at : List.of(target, other)) {
            types.put(at, terrain);
            trees.remove(at);
            fields.remove(at);
        }
        data.getModel().getMap().setTerrainType(types);
        data.getModel().getMap().setTrees(trees);
        data.getModel().getMap().setFields(fields);
        return data;
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private Model fixture() {
        return make(data(TerrainType.GRASSLAND));
    }

    /** Save through the real file format and load a fresh model. */
    private Model reload(final GameModelPo data, final Model model) {
        data.setModel(model.save());
        final Path file = directory.resolve("road-" + saves++ + ".microcol");
        dao.saveToFile(file.toString(), data);
        return make(dao.loadFromFile(file.toFile()));
    }

    private UnitFreeColonist pioneer(final Model model, final int tools,
            final Location at) {
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist pioneer = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        pioneer.placeToLocation(at);
        pioneer.setTools(tools);
        pioneer.setActionPoints(pioneer.getSpeed());
        return pioneer;
    }

    private UnitFreeColonist hardy(final Model model, final int tools,
            final Location at) {
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.HARDY_PIONEER.getEuropePrice());
        buyer.buy(UnitType.HARDY_PIONEER);
        final UnitFreeColonist hardy = (UnitFreeColonist) buyer.getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.HARDY_PIONEER)
                .findFirst().orElseThrow();
        hardy.placeToLocation(at);
        hardy.setTools(tools);
        hardy.setActionPoints(hardy.getSpeed());
        return hardy;
    }

    private void nextOwnerTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private UnitActionBuildRoad action(final Unit unit) {
        return (UnitActionBuildRoad) unit.getAction();
    }

    /** Run the last owner update of this order without any other update. */
    private void completeNow(final Model model,
            final UnitFreeColonist pioneer) {
        final UnitActionBuildRoad action = action(pioneer);
        action.setRemainingTurns(1);
        assertTrue(action.startTurn(model, pioneer));
    }

    private boolean road(final Model model, final Location at) {
        return model.getMap().getTerrainAt(at).isHasRoad();
    }

    // ---- Admission
    // -----------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = { "GRASSLAND", "PRAIRIE", "SAVANNAH", "PLAINS",
            "MARSH", "SWAMP", "DESERT", "TUNDRA", "ARCTIC", "HILL",
            "MOUNTAIN" })
    void everyNonSeaTerrainAcceptsARoad(final String name) {
        final Model model = make(data(TerrainType.valueOf(name)));
        final UnitFreeColonist pioneer = pioneer(model, 20, target);
        assertTrue(model.canBuildRoad(pioneer), name);
        model.buildRoad(pioneer);
        assertEquals(UnitActionType.buildRoad, pioneer.getAction().getType());
    }

    @ParameterizedTest
    @ValueSource(strings = { "OCEAN", "LAKE", "HIGH_SEA" })
    void waterSquaresRejectRoadWithoutMutation(final String name) {
        final Model model = make(data(TerrainType.valueOf(name)));
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        final String before = gson.toJson(model.save());
        assertEquals(BuildRoadProblem.SEA,
                model.getBuildRoadProblem(pioneer).orElseThrow());
        assertFalse(model.canBuildRoad(pioneer));
        assertThrows(IllegalStateException.class,
                () -> model.buildRoad(pioneer));
        assertEquals(before, gson.toJson(model.save()));
    }

    @Test
    void forestFieldAndColonyTilesAreLegalRoadTargets() {
        final GameModelPo data = data(TerrainType.GRASSLAND);
        final Set<Location> trees = new HashSet<>(
                data.getModel().getMap().getTreeSet());
        trees.add(target);
        data.getModel().getMap().setTrees(trees);
        final Set<Location> fields = new HashSet<>(
                data.getModel().getMap().getFieldSet());
        fields.add(other);
        data.getModel().getMap().setFields(fields);
        final Model model = make(data);
        final UnitFreeColonist inForest = pioneer(model, 100, target);
        assertTrue(model.getBuildRoadProblem(inForest).isEmpty());
        final UnitFreeColonist onField = pioneer(model, 100, other);
        assertTrue(model.getBuildRoadProblem(onField).isEmpty());
        final Location colonyTile = model.getColonyById(0).orElseThrow()
                .getLocation();
        final UnitFreeColonist inColony = pioneer(model, 100, colonyTile);
        assertTrue(model.getBuildRoadProblem(inColony).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 19 })
    void tooFewToolsRejectWithoutMutation(final int tools) {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, tools, target);
        final String before = gson.toJson(model.save());
        assertEquals(BuildRoadProblem.NOT_ENOUGH_TOOLS,
                model.getBuildRoadProblem(pioneer).orElseThrow());
        assertThrows(IllegalStateException.class,
                () -> model.buildRoad(pioneer));
        assertEquals(before, gson.toJson(model.save()));
    }

    @Test
    void twentyToolsAreEnoughAndStartSpendsMovementOnly() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 20, target);
        model.buildRoad(pioneer);
        assertEquals(0, pioneer.getMovementThirds());
        assertEquals(20, pioneer.getTools());
        assertFalse(road(model, target));
        assertTrue(model.canCancelBuildRoad(pioneer));
    }

    @Test
    void anUnsplitFinalThirdIsEnoughToStart() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        pioneer.setMovementThirds(1);
        assertTrue(model.canBuildRoad(pioneer));
        model.buildRoad(pioneer);
        assertEquals(0, pioneer.getMovementThirds());
    }

    @Test
    void zeroMovementBusyAndExistingRoadReject() {
        final Model model = fixture();
        final UnitFreeColonist idle = pioneer(model, 100, target);
        idle.setMovementThirds(0);
        assertEquals(BuildRoadProblem.NO_MOVES,
                model.getBuildRoadProblem(idle).orElseThrow());
        idle.setMovementThirds(3);
        model.buildRoad(idle);
        assertEquals(BuildRoadProblem.BUSY,
                model.getBuildRoadProblem(idle).orElseThrow());
        final UnitFreeColonist builder = pioneer(model, 100, other);
        model.getMap().buildRoad(other);
        assertEquals(BuildRoadProblem.ALREADY_ROAD,
                model.getBuildRoadProblem(builder).orElseThrow());
    }

    @Test
    void unavailableUnitsReject() {
        final Model model = fixture();
        // The galleon is not on the map, so it is not an available unit.
        final Unit ship = model.getUnitById(3);
        assertEquals(BuildRoadProblem.UNAVAILABLE,
                model.getBuildRoadProblem(ship).orElseThrow());
        final UnitFreeColonist gone = pioneer(model, 100, target);
        model.destroyUnit(gone);
        assertEquals(BuildRoadProblem.UNAVAILABLE,
                model.getBuildRoadProblem(gone).orElseThrow());
    }

    // ---- Timing ------------------------------------------------------------

    @Test
    void ordinaryWorkCompletesAfterTwoOwnerUpdates() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        model.buildRoad(pioneer);
        assertEquals(2, action(pioneer).getRemainingTurns());
        nextOwnerTurn(model);
        assertEquals(1, action(pioneer).getRemainingTurns());
        assertFalse(road(model, target));
        assertEquals(100, pioneer.getTools());
        nextOwnerTurn(model);
        assertTrue(road(model, target));
        assertEquals(80, pioneer.getTools());
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
    }

    @Test
    void hardyWorkCompletesAfterOneOwnerUpdate() {
        final Model model = fixture();
        final UnitFreeColonist hardy = hardy(model, 100, target);
        model.buildRoad(hardy);
        assertEquals(1, action(hardy).getRemainingTurns());
        nextOwnerTurn(model);
        assertTrue(road(model, target));
        assertEquals(80, hardy.getTools());
    }

    // ---- Completion, cancellation and stale work ---------------------------

    @Test
    void completionRecordsOneRoadAndChargesToolsOnce() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        model.buildRoad(pioneer);
        completeNow(model, pioneer);
        assertTrue(road(model, target));
        assertEquals(80, pioneer.getTools());
        final UnitActionBuildRoad finished = action(pioneer);
        final String saved = gson.toJson(model.save());
        assertTrue(finished.startTurn(model, pioneer));
        assertEquals(saved, gson.toJson(model.save()));
        assertEquals(80, pioneer.getTools());
    }

    @Test
    void explicitCancelDiscardsProgressWithoutChargeOrRoad() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        assertFalse(model.canCancelBuildRoad(pioneer));
        assertThrows(IllegalStateException.class,
                () -> model.cancelBuildRoad(pioneer));
        model.buildRoad(pioneer);
        nextOwnerTurn(model);
        model.cancelBuildRoad(pioneer);
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(0, pioneer.getMovementThirds());
        assertEquals(100, pioneer.getTools());
        assertFalse(road(model, target));
        for (int update = 0; update < 3; update++) {
            nextOwnerTurn(model);
        }
        assertFalse(road(model, target));
        assertEquals(100, pioneer.getTools());
    }

    @Test
    void competingPioneersCompleteOneRoadAndChargeOnlyTheFirstFinisher() {
        final Model model = fixture();
        final UnitFreeColonist first = pioneer(model, 100, target);
        final UnitFreeColonist second = pioneer(model, 100, target);
        model.buildRoad(first);
        model.buildRoad(second);
        action(first).setRemainingTurns(1);
        action(second).setRemainingTurns(1);
        // The real owner update completes the first order and releases it; the
        // second order finds the road and ends as stale, also released.
        first.startTurn();
        assertTrue(road(model, target));
        assertEquals(80, first.getTools());
        assertEquals(UnitActionType.noAction, first.getAction().getType());
        second.startTurn();
        assertEquals(100, second.getTools());
        assertEquals(UnitActionType.noAction, second.getAction().getType());
        assertTrue(road(model, target));
    }

    @Test
    void staleLostToolsEndWithoutRoadOrCharge() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        model.buildRoad(pioneer);
        pioneer.setTools(5);
        nextOwnerTurn(model);
        assertFalse(road(model, target));
        assertEquals(5, pioneer.getTools());
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
    }

    @Test
    void retainedCanceledCallbackHasNoEffect() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        model.buildRoad(pioneer);
        final UnitActionBuildRoad old = action(pioneer);
        model.cancelBuildRoad(pioneer);
        final String before = gson.toJson(model.save());
        assertTrue(old.startTurn(model, pioneer));
        assertEquals(before, gson.toJson(model.save()));
        assertFalse(road(model, target));
    }

    // ---- Persistence --------------------------------------------------------

    @Test
    void unfinishedWorkAndCompletedRoadSurviveContinueWithoutRepeating() {
        final GameModelPo data = data(TerrainType.GRASSLAND);
        Model model = make(data);
        final UnitFreeColonist pioneer = pioneer(model, 100, target);
        final UnitFreeColonist builder = pioneer(model, 100, other);
        final int id = pioneer.getId();
        model.buildRoad(pioneer);
        model.buildRoad(builder);
        nextOwnerTurn(model);
        model = reload(data, model);
        assertEquals(1, action(model.getUnitById(id)).getRemainingTurns());
        assertFalse(road(model, target));
        nextOwnerTurn(model);
        model = reload(data, model);
        assertTrue(road(model, target));
        assertTrue(road(model, other));
        final String saved = gson.toJson(model.save());
        model = reload(data, model);
        assertEquals(saved, gson.toJson(model.save()));
        assertEquals(80, ((UnitFreeColonist) model.getUnitById(id)).getTools());
    }

    @Test
    void roadSquareCannotBeBuiltTwiceAndWaterCannotHoldARoad() {
        final Model model = fixture();
        model.getMap().buildRoad(target);
        assertThrows(IllegalArgumentException.class,
                () -> model.getMap().buildRoad(target));
        final Model sea = make(data(TerrainType.OCEAN));
        assertThrows(IllegalArgumentException.class,
                () -> sea.getMap().buildRoad(target));
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 3 })
    void invalidSavedCountdownIsRejected(final int countdown) {
        final UnitActionBuildRoadPo po = new UnitActionBuildRoadPo();
        po.setRemainingTurns(countdown);
        assertThrows(IllegalArgumentException.class,
                () -> UnitActionConverter.convert(po));
        assertThrows(IllegalArgumentException.class,
                () -> new UnitActionBuildRoad().setRemainingTurns(countdown));
    }
}
