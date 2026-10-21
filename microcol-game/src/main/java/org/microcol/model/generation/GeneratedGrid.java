package org.microcol.model.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

/**
 * Read access to the terrain of a generated world under construction. Squares
 * that are not listed are ocean; every square outside the map is neither land
 * nor water.
 */
final class GeneratedGrid {

    /** Orthogonal steps; rivers and land neighbours use only these. */
    static final int[][] STEPS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    private static final Comparator<Location> ROW_MAJOR = Comparator
            .comparingInt(Location::getY).thenComparingInt(Location::getX);

    private final Map<Location, TerrainType> types;

    GeneratedGrid(final Map<Location, TerrainType> types) {
        this.types = types;
    }

    static boolean isOnMap(final Location at) {
        return at.getX() >= 1 && at.getX() <= GeneratedWorld.WIDTH
                && at.getY() >= 1 && at.getY() <= GeneratedWorld.HEIGHT;
    }

    TerrainType terrainAt(final Location at) {
        return types.getOrDefault(at, TerrainType.OCEAN);
    }

    boolean isLand(final Location at) {
        return isOnMap(at) && !terrainAt(at).isSea();
    }

    boolean isWater(final Location at) {
        return isOnMap(at) && terrainAt(at).isSea();
    }

    /**
     * @param at required square
     * @return orthogonal neighbours that lie on the map, in fixed order
     */
    List<Location> neighbours(final Location at) {
        final List<Location> out = new ArrayList<>();
        for (final int[] step : STEPS) {
            final Location next = Location.of(at.getX() + step[0],
                    at.getY() + step[1]);
            if (isOnMap(next)) {
                out.add(next);
            }
        }
        return out;
    }

    /**
     * @param filter required test of the terrain of a listed square
     * @return listed squares that pass the test, in row-major order
     */
    List<Location> squaresWith(final Predicate<TerrainType> filter) {
        final List<Location> out = new ArrayList<>();
        for (final Map.Entry<Location, TerrainType> entry : types.entrySet()) {
            if (filter.test(entry.getValue())) {
                out.add(entry.getKey());
            }
        }
        return sorted(out);
    }

    /**
     * @param squares required squares
     * @return the squares in row-major order, so random draws never depend on
     *         hash order
     */
    static List<Location> sorted(final Collection<Location> squares) {
        final List<Location> out = new ArrayList<>(squares);
        out.sort(ROW_MAJOR);
        return out;
    }

}
