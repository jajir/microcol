package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.Location;
import org.microcol.model.RiverSize;

class RiverShapeTest {
    private static final Location CENTER = Location.of(10, 10);

    @Test
    void straightCourseBendsAndKeepsTheSameShapeAfterRedraw() {
        final Location west = Location.of(9, 10);
        final Location east = Location.of(11, 10);
        final RiverShape river = new RiverShape(CENTER, RiverSize.SMALL,
                Map.of(west, RiverSize.SMALL, east, RiverSize.SMALL));
        final Map<Location, RiverSize> reversed = new LinkedHashMap<>();
        reversed.put(east, RiverSize.SMALL);
        reversed.put(west, RiverSize.SMALL);
        final RiverShape redraw = new RiverShape(CENTER, RiverSize.SMALL,
                reversed);
        assertArrayEquals(river.bands.get(0).x, redraw.bands.get(0).x);
        assertArrayEquals(river.bands.get(0).y, redraw.bands.get(0).y);
        assertFalse(river.roundCenter);
        final RiverShape.Band band = river.bands.get(0);
        double min = 45, max = 0;
        for (int i = 0; i < band.y.length / 2; i++) {
            final double center = (band.y[i] + band.y[band.y.length - 1 - i])
                    / 2;
            min = Math.min(min, center);
            max = Math.max(max, center);
        }
        assertTrue(max - min > 1,
                "A straight network edge still needs visible curvature");
    }

    @Test
    void everyDirectionAndSizeSharesBothBanksWithItsNeighbour() {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                for (final RiverSize size : new RiverSize[] { RiverSize.SMALL,
                        RiverSize.LARGE }) {
                    final Location next = Location.of(10 + dx, 10 + dy);
                    final RiverShape a = new RiverShape(CENTER, size,
                            Map.of(next, RiverSize.LARGE));
                    final RiverShape b = new RiverShape(next, RiverSize.LARGE,
                            Map.of(CENTER, size));
                    final RiverShape.Band first = a.bands.get(0);
                    final RiverShape.Band second = b.bands.get(0);
                    final int end = first.x.length / 2 - 1;
                    assertEquals(first.x[end], second.x[end + 1] + dx * 45,
                            .000001);
                    assertEquals(first.y[end], second.y[end + 1] + dy * 45,
                            .000001);
                    assertEquals(first.x[end + 1], second.x[end] + dx * 45,
                            .000001);
                    assertEquals(first.y[end + 1], second.y[end] + dy * 45,
                            .000001);
                }
            }
        }
    }

    @Test
    void allTurnsAndJunctionsHaveFiniteBoundedGeometryAndBridges() {
        final Map<Location, RiverSize> neighbours = new LinkedHashMap<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    neighbours.put(Location.of(10 + dx, 10 + dy),
                            RiverSize.LARGE);
                }
            }
        }
        for (final Location first : neighbours.keySet()) {
            for (final Location last : neighbours.keySet()) {
                if (!first.equals(last)) {
                    final RiverShape curve = new RiverShape(CENTER,
                            RiverSize.LARGE, Map.of(first, RiverSize.SMALL,
                                    last, RiverSize.LARGE));
                    verifyBounds(curve);
                    assertTrue(curve.bridgeX > 7 && curve.bridgeX < 38);
                    assertTrue(curve.bridgeY > 7 && curve.bridgeY < 38);
                    assertTrue(Double.isFinite(curve.bridgeAngle));
                }
            }
        }
        final RiverShape junction = new RiverShape(CENTER, RiverSize.LARGE,
                neighbours);
        verifyBounds(junction);
        assertEquals(8, junction.bands.size());
        assertTrue(junction.roundCenter);
        final RiverShape isolated = new RiverShape(CENTER, RiverSize.SMALL,
                Map.of());
        assertTrue(isolated.bands.isEmpty());
        assertTrue(isolated.roundCenter);
    }

    private void verifyBounds(final RiverShape curve) {
        for (final RiverShape.Band band : curve.bands) {
            for (int i = 0; i < band.x.length; i++) {
                assertTrue(Double.isFinite(band.x[i])
                        && Double.isFinite(band.y[i]));
                assertTrue(band.x[i] >= -7 && band.x[i] <= 52);
                assertTrue(band.y[i] >= -7 && band.y[i] <= 52);
            }
        }
    }
}
