package org.microcol.model.generation;

import java.util.Random;

import org.microcol.model.Location;

/**
 * Smooth random field in the range 0 to 1. Random values sit on a coarse
 * lattice and are interpolated between, so neighbouring squares share a region.
 */
final class LatticeNoise {

    /** Distance in squares between two lattice points. */
    private static final int CELL = 6;

    private final double[][] values;

    LatticeNoise(final Random random) {
        values = new double[GeneratedWorld.WIDTH / CELL
                + 2][GeneratedWorld.HEIGHT / CELL + 2];
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[x].length; y++) {
                values[x][y] = random.nextDouble();
            }
        }
    }

    /**
     * @param at required square of the map
     * @return value of the field at that square
     */
    double at(final Location at) {
        final double gx = (double) at.getX() / CELL;
        final double gy = (double) at.getY() / CELL;
        final int x0 = (int) gx;
        final int y0 = (int) gy;
        final double sx = smooth(gx - x0);
        final double sy = smooth(gy - y0);
        final double top = interpolate(values[x0][y0], values[x0 + 1][y0], sx);
        final double bottom = interpolate(values[x0][y0 + 1],
                values[x0 + 1][y0 + 1], sx);
        return interpolate(top, bottom, sy);
    }

    private static double smooth(final double fraction) {
        return fraction * fraction * (3 - 2 * fraction);
    }

    private static double interpolate(final double from, final double to,
            final double share) {
        return from + (to - from) * share;
    }

}
