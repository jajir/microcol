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
import org.microcol.model.store.RiverSegmentPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * River travel: one third of a point for a step along a connected river segment
 * in either direction, ordinary terrain cost across a river without a segment,
 * no stacking with a road, and exact thirds through execution, path
 * affordability and reach.
 */
class RiverMovementTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    /** Squares used by the tests lie this far from every colony. */
    private static final int COLONY_CLEARANCE = 4;

    private final GameModelPoDao dao = new GameModelPoDao();

    /** Three squares in a row used by the current test. */
    private Location west;

    private Location middle;

    private Location east;

    /**
     * Load the map and put the remote run on grassland. Each square of the run
     * is far from every colony, so no resident worker is changed.
     */
    private GameModelPo data() {
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
            types.put(at, TerrainType.GRASSLAND);
            trees.remove(at);
        }
        data.getModel().getMap().setTerrainType(types);
        data.getModel().getMap().setTrees(trees);
        return data;
    }

    /** Add the given rivers, courses and roads to the run set up by data. */
    private Model model(final GameModelPo data,
            final Map<Location, RiverSize> rivers,
            final List<RiverSegmentPo> segments, final Set<Location> roads) {
        data.getModel().getMap().setRivers(rivers);
        data.getModel().getMap().setRiverSegments(segments);
        data.getModel().getMap().setRoads(roads);
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    /** Three small river squares with a course west-middle and middle-east. */
    private Model riverChain() {
        final GameModelPo data = data();
        return model(data,
                Map.of(west, RiverSize.SMALL, middle, RiverSize.SMALL, east,
                        RiverSize.SMALL),
                List.of(new RiverSegmentPo(west, middle),
                        new RiverSegmentPo(middle, east)),
                Set.of());
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
    void connectedRiverStepCostsOneThirdInEitherDirection() {
        final Model model = riverChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        assertEquals(1, pioneer.getEntryCostThirds(west, middle));
        assertEquals(1, pioneer.getEntryCostThirds(middle, west));
        assertEquals(1, pioneer.getEntryCostThirds(middle, east));
    }

    @Test
    void crossingARiverWithoutItsSegmentKeepsOrdinaryCost() {
        final GameModelPo data = data();
        final Model model = model(data,
                Map.of(west, RiverSize.SMALL, middle, RiverSize.LARGE, east,
                        RiverSize.SMALL),
                List.of(new RiverSegmentPo(west, middle)), Set.of());
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        assertEquals(1, pioneer.getEntryCostThirds(west, middle));
        // Adjacent river squares without a joining segment cost grassland.
        assertEquals(3, pioneer.getEntryCostThirds(middle, east));
        assertEquals(3, pioneer.getEntryCostThirds(east, middle));
    }

    @Test
    void roadAndRiverOnTheSameStepDoNotStack() {
        final GameModelPo data = data();
        final Model model = model(data,
                Map.of(west, RiverSize.SMALL, middle, RiverSize.SMALL),
                List.of(new RiverSegmentPo(west, middle)),
                Set.of(west, middle));
        final UnitFreeColonist pioneer = pioneerAt(model, west, 3);
        // Both discounts give the same single third, never less.
        assertEquals(1, pioneer.getEntryCostThirds(west, middle));
    }

    // ---- Execution ---------------------------------------------------------

    @Test
    void movingAlongARiverSpendsExactThirds() {
        final Model model = riverChain();
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
    void finalThirdEntersAConnectedRiverButNotAnUnconnectedSquare() {
        final Model model = riverChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 1);
        assertTrue(pioneer.canAffordStepTo(middle));
        model.moveUnit(pioneer, Path.of(List.of(middle)));
        assertEquals(middle, pioneer.getLocation());
        assertEquals(0, pioneer.getMovementThirds());
        // Entering grassland without a segment from the same river square costs
        // the full three thirds, so one third is not enough.
        final GameModelPo data = data();
        final Model stuck = model(data,
                Map.of(west, RiverSize.SMALL, middle, RiverSize.SMALL),
                List.of(), Set.of());
        assertFalse(pioneerAt(stuck, west, 1).canAffordStepTo(middle));
    }

    @Test
    void unaffordableRiverCrossingRejectsWithoutMovingOrSpending() {
        final GameModelPo data = data();
        final Model model = model(data,
                Map.of(west, RiverSize.SMALL, middle, RiverSize.SMALL),
                List.of(), Set.of());
        final UnitFreeColonist pioneer = pioneerAt(model, west, 2);
        assertThrows(IllegalStateException.class,
                () -> model.moveUnit(pioneer, Path.of(List.of(middle))));
        assertEquals(west, pioneer.getLocation());
        assertEquals(2, pioneer.getMovementThirds());
    }

    // ---- Path, affordability and reach --------------------------------------

    @Test
    void pathAffordabilityUsesTheSameRiverCostAsExecution() {
        final Model model = riverChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 2);
        final List<Location> route = List.of(middle, east);
        assertTrue(pioneer.canAffordPath(route));
        pioneer.setMovementThirds(1);
        assertFalse(pioneer.canAffordPath(route));
    }

    @Test
    void routeSearchPricesRiverStepsSoFractionalMovementReachesFurther() {
        final Model model = riverChain();
        final UnitFreeColonist pioneer = pioneerAt(model, west, 2);
        final List<Location> route = pioneer.getPath(east).orElseThrow();
        assertEquals(List.of(middle, east), route);
        assertTrue(pioneer.canAffordPath(route));
    }
}
