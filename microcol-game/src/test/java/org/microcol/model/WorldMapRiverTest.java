package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.RiverSegmentPo;

/**
 * Saved river squares and course segments: a shared segment serves both ends,
 * malformed courses are refused before the map is usable, and Save keeps the
 * courses for the next load.
 */
class WorldMapRiverTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private final GameModelPoDao dao = new GameModelPoDao();

    /** Three land squares in a row, the first one found on the map. */
    private List<Location> landRun(final GameModelPo data) {
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final int maxX = data.getModel().getMap().getMaxX();
        final int maxY = data.getModel().getMap().getMaxY();
        for (int y = 1; y <= maxY; y++) {
            for (int x = 1; x <= maxX - 2; x++) {
                final List<Location> run = List.of(Location.of(x, y),
                        Location.of(x + 1, y), Location.of(x + 2, y));
                if (run.stream().allMatch(at -> types.containsKey(at)
                        && types.get(at).isLand())) {
                    return run;
                }
            }
        }
        throw new IllegalStateException("No land run in the map");
    }

    /** One square without land on the map, normally sea. */
    private Location seaSquare(final GameModelPo data) {
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        for (int y = 1; y <= data.getModel().getMap().getMaxY(); y++) {
            for (int x = 1; x <= data.getModel().getMap().getMaxX(); x++) {
                final Location at = Location.of(x, y);
                if (!types.containsKey(at)) {
                    return at;
                }
            }
        }
        throw new IllegalStateException("No sea square in the map");
    }

    private WorldMap riverMap(final GameModelPo data,
            final Map<Location, RiverSize> rivers,
            final List<RiverSegmentPo> segments) {
        data.getModel().getMap().setRivers(rivers);
        data.getModel().getMap().setRiverSegments(segments);
        return new WorldMap(data.getModel());
    }

    @Test
    void mapWithoutRiverBlocksHasNoRiverAndNoCourse() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final WorldMap map = new WorldMap(data.getModel());
        for (final Location at : landRun(data)) {
            assertEquals(RiverSize.NONE, map.getRiverSizeAt(at));
            assertTrue(map.getRiverLinksAt(at).isEmpty());
        }
    }

    @Test
    void segmentJoinsBothSquaresInEitherDirectionOnly() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final List<Location> run = landRun(data);
        final WorldMap map = riverMap(data,
                Map.of(run.get(0), RiverSize.SMALL, run.get(1), RiverSize.LARGE,
                        run.get(2), RiverSize.SMALL),
                List.of(new RiverSegmentPo(run.get(0), run.get(1))));
        assertTrue(map.hasRiverSegment(run.get(0), run.get(1)));
        assertTrue(map.hasRiverSegment(run.get(1), run.get(0)));
        assertFalse(map.hasRiverSegment(run.get(1), run.get(2)));
        assertEquals(Set.of(run.get(1)), map.getRiverLinksAt(run.get(0)));
        assertEquals(Set.of(run.get(0)), map.getRiverLinksAt(run.get(1)));
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(run.get(1)));
        assertEquals(RiverSize.SMALL, map.getRiverSizeAt(run.get(2)));
    }

    @Test
    void malformedCoursesAreRefusedBeforeTheMapIsUsable() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final List<Location> run = landRun(data);
        final Location west = run.get(0);
        final Location middle = run.get(1);
        final Location east = run.get(2);
        final Map<Location, RiverSize> all = Map.of(west, RiverSize.SMALL,
                middle, RiverSize.SMALL, east, RiverSize.SMALL);
        // Two squares that are not neighbours.
        assertThrows(IllegalArgumentException.class, () -> riverMap(data, all,
                List.of(new RiverSegmentPo(west, east))));
        // A square joined to itself.
        assertThrows(IllegalArgumentException.class, () -> riverMap(data, all,
                List.of(new RiverSegmentPo(middle, middle))));
        // A segment whose far end has no river.
        assertThrows(IllegalArgumentException.class,
                () -> riverMap(data, Map.of(west, RiverSize.SMALL),
                        List.of(new RiverSegmentPo(west, middle))));
        // The same pair saved twice, once in each direction.
        assertThrows(IllegalArgumentException.class,
                () -> riverMap(data, all,
                        List.of(new RiverSegmentPo(west, middle),
                                new RiverSegmentPo(middle, west))));
        // A segment without both squares.
        assertThrows(IllegalArgumentException.class, () -> riverMap(data, all,
                List.of(new RiverSegmentPo(west, null))));
        // A square outside the map.
        assertThrows(IllegalArgumentException.class,
                () -> riverMap(data, Map.of(Location.of(0, 1), RiverSize.SMALL),
                        List.of(new RiverSegmentPo(Location.of(0, 1),
                                Location.of(1, 1)))));
        // A river on sea is refused as well.
        final Location sea = seaSquare(data);
        assertThrows(IllegalArgumentException.class,
                () -> riverMap(data, Map.of(sea, RiverSize.SMALL), List.of()));
    }

    @Test
    void savedRiversAndSegmentsLoadUnchanged() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final List<Location> run = landRun(data);
        final WorldMap map = riverMap(data,
                Map.of(run.get(0), RiverSize.SMALL, run.get(1), RiverSize.LARGE,
                        run.get(2), RiverSize.SMALL),
                List.of(new RiverSegmentPo(run.get(0), run.get(1)),
                        new RiverSegmentPo(run.get(1), run.get(2))));
        final ModelPo saved = new ModelPo();
        map.save(saved);
        assertEquals(2, saved.getMap().getRiverSegments().size());
        final WorldMap reloaded = new WorldMap(saved);
        assertEquals(RiverSize.SMALL, reloaded.getRiverSizeAt(run.get(0)));
        assertEquals(RiverSize.LARGE, reloaded.getRiverSizeAt(run.get(1)));
        assertTrue(reloaded.hasRiverSegment(run.get(1), run.get(2)));
        assertFalse(reloaded.hasRiverSegment(run.get(0), run.get(2)));
        assertEquals(Set.of(run.get(0), run.get(2)),
                reloaded.getRiverLinksAt(run.get(1)));
    }

    @Test
    void savedRiverForestFieldAndUnrelatedBonusAreRejected() {
        final GameModelPo forest = dao.loadFromClassPath(MAP);
        final Location at = landRun(forest).get(0);
        forest.getModel().getMap().setTrees(Set.of(at));
        forest.getModel().getMap().setRivers(Map.of(at, RiverSize.SMALL));
        assertThrows(IllegalArgumentException.class,
                () -> new WorldMap(forest.getModel()));
        final GameModelPo field = dao.loadFromClassPath(MAP);
        field.getModel().getMap().setTrees(Set.of());
        field.getModel().getMap().setFields(Set.of(at));
        field.getModel().getMap().setRivers(Map.of(at, RiverSize.SMALL));
        assertThrows(IllegalArgumentException.class,
                () -> new WorldMap(field.getModel()));
        final GameModelPo resource = dao.loadFromClassPath(MAP);
        resource.getModel().getMap().setTrees(Set.of());
        resource.getModel().getMap().setRivers(Map.of(at, RiverSize.SMALL));
        resource.getModel().getMap()
                .setResources(Map.of(at, ResourceOverlay.PRIME_FOOD));
        assertThrows(IllegalArgumentException.class,
                () -> new WorldMap(resource.getModel()));
    }
}
