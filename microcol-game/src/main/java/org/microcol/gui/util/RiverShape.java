package org.microcol.gui.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.microcol.gui.Tile;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.RiverSize;

/**
 * Tile-local river bands; adjacent tiles share endpoints, tangents and widths.
 */
public final class RiverShape {

    private static final double HALF = Tile.TILE_WIDTH_IN_PX / 2.0;
    private static final int STEPS = 24;

    final List<Band> bands = new ArrayList<>();
    final double width;
    final boolean roundCenter;
    double bridgeX = HALF;
    double bridgeY = HALF;
    double bridgeAngle;

    public RiverShape(final Location at, final RiverSize size,
            final Map<Location, RiverSize> neighbours) {
        width = width(size);
        final List<Location> joined = new ArrayList<>(neighbours.keySet());
        joined.sort(Comparator.comparingInt(Location::getX)
                .thenComparingInt(Location::getY));
        roundCenter = joined.size() != 2;
        final double phase = at.getX() * 1.7 + at.getY() * 2.3;
        if (joined.size() == 2) {
            final Location first = joined.get(0);
            final Location last = joined.get(1);
            addBand((first.getX() - at.getX() + 1) * HALF,
                    (first.getY() - at.getY() + 1) * HALF,
                    (last.getX() - at.getX() + 1) * HALF,
                    (last.getY() - at.getY() + 1) * HALF,
                    (width + width(neighbours.get(first))) / 2,
                    (width + width(neighbours.get(last))) / 2, phase, true);
        } else {
            for (final Location neighbour : joined) {
                addBand(HALF, HALF, (neighbour.getX() - at.getX() + 1) * HALF,
                        (neighbour.getY() - at.getY() + 1) * HALF, width,
                        (width + width(neighbours.get(neighbour))) / 2, phase,
                        false);
            }
        }
    }

    /** Choose one known ocean edge ahead of a visible terminal course. */
    public static Direction mouthDirection(final Location at,
            final Set<Location> riverLinks,
            final Map<Location, TerrainType> knownTerrain) {
        if (riverLinks.size() != 1) {
            return null;
        }
        final Location upstream = riverLinks.iterator().next();
        Direction mouth = null;
        int bestAlignment = 0;
        // A corner-only contact must not cut through either adjacent land tile.
        for (final Direction direction : List.of(Direction.north,
                Direction.east, Direction.south, Direction.west)) {
            final Location vector = direction.getVector();
            final TerrainType water = knownTerrain.get(at.add(vector));
            final int alignment = (at.getX() - upstream.getX()) * vector.getX()
                    + (at.getY() - upstream.getY()) * vector.getY();
            if ((water == TerrainType.OCEAN || water == TerrainType.HIGH_SEA)
                    && alignment > bestAlignment) {
                mouth = direction;
                bestAlignment = alignment;
            }
        }
        return mouth;
    }

    private static double width(final RiverSize size) {
        return Tile.TILE_WIDTH_IN_PX * (size == RiverSize.LARGE ? .28 : .12);
    }

    private void addBand(final double x0, final double y0, final double x1,
            final double y1, final double width0, final double width1,
            final double phase, final boolean through) {
        final double dx = x1 - x0;
        final double dy = y1 - y0;
        final double length = Math.hypot(dx, dy);
        final double controlX = through ? HALF : (x0 + x1) / 2;
        final double controlY = through ? HALF : (y0 + y1) / 2;
        final Band band = new Band();
        for (int i = 0; i <= STEPS; i++) {
            final double t = (double) i / STEPS;
            final double u = 1 - t;
            final double envelope = Math.pow(Math.sin(Math.PI * t), 2);
            final double bend = 2.2 * envelope * Math.sin(phase + Math.PI * t);
            final double bendSlope = 2.2 * Math.PI
                    * (Math.sin(2 * Math.PI * t) * Math.sin(phase + Math.PI * t)
                            + envelope * Math.cos(phase + Math.PI * t));
            final double x = u * u * x0 + 2 * u * t * controlX + t * t * x1
                    - dy / length * bend;
            final double y = u * u * y0 + 2 * u * t * controlY + t * t * y1
                    + dx / length * bend;
            final double tangentX = 2
                    * (u * (controlX - x0) + t * (x1 - controlX))
                    - dy / length * bendSlope;
            final double tangentY = 2
                    * (u * (controlY - y0) + t * (y1 - controlY))
                    + dx / length * bendSlope;
            final double tangentLength = Math.hypot(tangentX, tangentY);
            final double localWidth = t < .5 ? width0 + (width - width0) * 2 * t
                    : width + (width1 - width) * (2 * t - 1);
            final double radius = localWidth / 2
                    * (1 + .13 * envelope * Math.sin(phase + 2 * Math.PI * t));
            final int opposite = band.x.length - 1 - i;
            band.x[i] = x - tangentY / tangentLength * radius;
            band.y[i] = y + tangentX / tangentLength * radius;
            band.x[opposite] = x + tangentY / tangentLength * radius;
            band.y[opposite] = y - tangentX / tangentLength * radius;
            if (through && i == STEPS / 2) {
                bridgeX = x;
                bridgeY = y;
                bridgeAngle = Math.toDegrees(Math.atan2(tangentY, tangentX))
                        + 90;
            } else if (!through && bands.isEmpty() && i == 0) {
                bridgeAngle = Math.toDegrees(Math.atan2(tangentY, tangentX))
                        + 90;
            }
        }
        bands.add(band);
    }

    static final class Band {
        final double[] x = new double[2 * (STEPS + 1)];
        final double[] y = new double[2 * (STEPS + 1)];
    }
}
