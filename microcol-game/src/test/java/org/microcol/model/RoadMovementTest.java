package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * Road travel: one third of a point for a step that has a road on both squares,
 * ordinary terrain cost otherwise, exact thirds through execution, path
 * affordability and reach, and the final third of movement.
 */
class RoadMovementTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    /** Squares used by the tests lie this far from every colony. */
    private static final int COLONY_CLEARANCE = 4;

    private final GameModelPoDao dao = new GameModelPoDao();

    /** Three squares in a row used by the current test. */
    private Location west;

    private Location middle;

    private Location east;

    /**
     * Load the map and put the remote run on the given terrain. Each square of
     * the run is far from every colony, so no resident worker is changed.
     */
    private GameModelPo data(final TerrainType terrain) {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final List<Location> colonies = data.getModel().getColonies().stream()
                .map(ColonyPo::getLocation).collect(Collectors.toList());
        final int maxX = data.getModel().getMap().getMaxX();
        final int maxY = data.getModel().getMap().getMaxY();
        List<Location> run = null;
        for (int y = 1; y <= maxY && run == null; y++) {
            for (int x = 1; x <= maxX - 2 && run == null; x++) {
                final List<Location> candidate = List.of(Location.of(x, y),
                        Location.of(x + 1, y), Location.of(x + 2, y));
                if (candidate.stream()
                        .allMatch(at -> types.containsKey(at)
                                && types.get(at).isLand()
                                && colonies.stream().allMatch(
                                        colony -> colony.getDistanceChebyshev(
                                                at) >= COLONY_CLEARANCE))) {
                    run = candidate;
                }
            }
        }
        if (run == null) {
            throw new IllegalStateException(
                    "No remote land squares in the map");
        }
        west = run.get(0);
        middle = run.get(1);
        east = run.get(2);
        final Set<Location> trees = new HashSet<>(
                data.getModel().getMap().getTreeSet());
        for (final Location at : run) {
            types.put(at, terrain);
            trees.remove(at);
        }
        data.getModel().getMap().setTerrainType(types);
        data.getModel().getMap().setTrees(trees);
        return data;
    }

    /** Add the given roads and forests to the run already set up by data. */
    private Model model(final GameModelPo data, final Set<Location> roads,
            final Set<Location> forests) {
        final Set<Location> trees = new HashSet<>(
                data.getModel().getMap().getTreeSet());
        trees.addAll(forests);
        data.getModel().getMap().setTrees(trees);
        data.getModel().getMap().setRoads(roads);
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private Model roadChain() {
        final GameModelPo data = data(TerrainType.GRASSLAND);
        return model(data, Set.of(west, middle, east), Set.of());
    }

    private UnitFreeColonist pioneerAt(final Model model, final Location at,
            final int thirds) {
        final UnitFreeColonist pioneer = (UnitFreeColonist) model
                .addUnitOutSideColony(model.getColonyById(0).orElseThrow());
        pioneer.placeToLocation(at);
        pioneer.setMovementThirds(thirds);
        return pioneer;
    }

    // ---- Entry cost -------------------------------------------------------

    @Test
    void connectedRoadStepCostsOneThirdInEitherDirection() {
        final Model model = roadChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        assertEquals(1, pioneer.getEntryCostThirds(west, middle));
        assertEquals(1, pioneer.getEntryCostThirds(middle, west));
        assertEquals(1, pioneer.getEntryCostThirds(middle, east));
    }

    @Test
    void disconnectedEndpointKeepsOrdinaryTerrainCost() {
        final Model none = model(data(TerrainType.GRASSLAND), Set.of(),
                Set.of());
        assertEquals(3,
                pioneerAt(none, west, 3).getEntryCostThirds(west, middle));
        final Model oneRoad = model(data(TerrainType.GRASSLAND), Set.of(west),
                Set.of());
        final UnitFreeColonist onRoad = pioneerAt(oneRoad, west, 3);
        assertEquals(3, onRoad.getEntryCostThirds(west, middle));
        assertEquals(3, onRoad.getEntryCostThirds(middle, west));
    }

    @Test
    void roadOnForestReplacesTheForestCostAndDisconnectedForestKeepsIt() {
        final GameModelPo data = data(TerrainType.GRASSLAND);
        final Set<Location> forest = Set.of(middle);
        final Model road = model(data, Set.of(west, middle), forest);
        assertEquals(1,
                pioneerAt(road, west, 3).getEntryCostThirds(west, middle));
        final Model bare = model(data(TerrainType.GRASSLAND), Set.of(west),
                forest);
        assertEquals(6,
                pioneerAt(bare, west, 3).getEntryCostThirds(west, middle));
    }

    // ---- Execution ---------------------------------------------------------

    @Test
    void movingAlongARoadSpendsExactThirds() {
        final Model model = roadChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        model.moveUnit(pioneer, Path.of(List.of(middle)));
        assertEquals(middle, pioneer.getLocation());
        assertEquals(2, pioneer.getMovementThirds());
        assertEquals(0, pioneer.getActionPoints());
        model.moveUnit(pioneer, Path.of(List.of(east)));
        assertEquals(east, pioneer.getLocation());
        assertEquals(1, pioneer.getMovementThirds());
    }

    @Test
    void finalThirdEntersAConnectedRoadButNotOrdinaryGrass() {
        final Model model = model(data(TerrainType.GRASSLAND),
                Set.of(west, middle), Set.of());
        final UnitFreeColonist pioneer = pioneerAt(model, west, 1);
        assertTrue(pioneer.canAffordStepTo(middle));
        model.moveUnit(pioneer, Path.of(List.of(middle)));
        assertEquals(middle, pioneer.getLocation());
        assertEquals(0, pioneer.getMovementThirds());
        // Entering ordinary grass from a road costs the full three thirds.
        final Model bare = model(data(TerrainType.GRASSLAND), Set.of(west),
                Set.of());
        final UnitFreeColonist stuck = pioneerAt(bare, west, 1);
        assertFalse(stuck.canAffordStepTo(middle));
    }

    @Test
    void unaffordableRoadStepRejectsWithoutMovingOrSpending() {
        final Model model = model(data(TerrainType.GRASSLAND), Set.of(west),
                Set.of());
        final UnitFreeColonist pioneer = pioneerAt(model, west, 2);
        assertThrows(IllegalStateException.class,
                () -> model.moveUnit(pioneer, Path.of(List.of(middle))));
        assertEquals(west, pioneer.getLocation());
        assertEquals(2, pioneer.getMovementThirds());
    }

    // ---- Path, affordability and reach --------------------------------------

    @Test
    void pathAffordabilityUsesTheSameRoadCostAsExecution() {
        final Model model = roadChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 2);
        final List<Location> route = List.of(middle, east);
        assertTrue(pioneer.canAffordPath(route));
        pioneer.setMovementThirds(1);
        assertFalse(pioneer.canAffordPath(route));
    }

    @Test
    void routeSearchPricesRoadStepsSoFractionalMovementReachesFurther() {
        final Model model = roadChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 2);
        final List<Location> route = pioneer.getPath(east).orElseThrow();
        assertEquals(List.of(middle, east), route);
        assertTrue(pioneer.canAffordPath(route));
    }

    @Test
    void stepPreviewCostsMatchTheExecutedStep() {
        final Model model = roadChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        assertEquals(1,
                pioneer.getEntryCostThirds(pioneer.getLocation(), middle));
        model.moveUnit(pioneer, Path.of(List.of(middle)));
        assertEquals(2, pioneer.getMovementThirds());
    }

    @Test
    void fullAllowanceExpensiveStepStillEndsAtZero() {
        final GameModelPo data = data(TerrainType.MOUNTAIN);
        final Model model = model(data, Set.of(), Set.of());
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        model.moveUnit(pioneer, Path.of(List.of(middle)));
        assertEquals(0, pioneer.getMovementThirds());
    }
}
