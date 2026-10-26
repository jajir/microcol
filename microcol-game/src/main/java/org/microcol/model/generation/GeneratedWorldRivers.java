package org.microcol.model.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

/**
 * River courses of a generated world. Each course is a simple path of land
 * squares from flat land beside uplands towards water, leaving a dry coastal
 * buffer. Hills, mountains and squares touching water cannot carry rivers. A
 * course never touches another course except at its own end, so the rivers
 * cannot form loops or merge. The first course is the main river and has at
 * least {@link #MAIN_MIN_LENGTH} squares; the others are shorter.
 */
final class GeneratedWorldRivers {

    static final int MAIN_MIN_LENGTH = 8;

    static final int MAIN_MAX_LENGTH = 16;

    static final int SMALL_MIN_LENGTH = 3;

    static final int SMALL_MAX_LENGTH = 7;

    /** Smaller rivers wanted beside the main river. */
    static final int SMALL_COUNT = 3;

    private final GeneratedGrid grid;

    private final LatticeNoise elevation;

    private final Random random;

    /** Squares already used by an accepted course. */
    private final Set<Location> occupied = new HashSet<>();

    private GeneratedWorldRivers(final GeneratedGrid grid,
            final LatticeNoise elevation, final Random random) {
        this.grid = grid;
        this.elevation = elevation;
        this.random = random;
    }

    /**
     * @param grid      required terrain, land already placed
     * @param elevation required height field that guides the water downhill
     * @param random    required generator of this attempt
     * @return courses, main river first; each lists its squares from source to
     *         outlet
     * @throws IllegalStateException when no main river of the minimum length
     *                               reaches the coastal buffer
     */
    static List<List<Location>> trace(final GeneratedGrid grid,
            final LatticeNoise elevation, final Random random) {
        return new GeneratedWorldRivers(grid, elevation, random).run();
    }

    private List<List<Location>> run() {
        final List<Location> sources = new ArrayList<>(grid
                .squaresWith(type -> !type.isSea() && type != TerrainType.HILL
                        && type != TerrainType.MOUNTAIN));
        sources.removeIf(at -> touchesWater(at)
                || grid.neighbours(at).stream().noneMatch(neighbour -> {
                    final TerrainType type = grid.terrainAt(neighbour);
                    return type == TerrainType.HILL
                            || type == TerrainType.MOUNTAIN;
                }));
        Collections.shuffle(sources, random);
        final List<List<Location>> rivers = new ArrayList<>();
        final List<Location> main = firstCourse(sources, MAIN_MIN_LENGTH,
                MAIN_MAX_LENGTH);
        if (main == null) {
            throw new IllegalStateException(
                    "No main river reaches the coastal buffer");
        }
        rivers.add(main);
        for (int i = 0; i < SMALL_COUNT; i++) {
            final List<Location> small = firstCourse(sources, SMALL_MIN_LENGTH,
                    SMALL_MAX_LENGTH);
            if (small == null) {
                break;
            }
            rivers.add(small);
        }
        return rivers;
    }

    /**
     * @return the first course from the sources that fits the length bounds, or
     *         null when none does
     */
    private List<Location> firstCourse(final List<Location> sources,
            final int minLength, final int maxLength) {
        for (final Location source : sources) {
            if (occupied.contains(source) || touchesOccupied(source)) {
                continue;
            }
            final List<Location> course = walk(source, minLength, maxLength);
            if (course != null) {
                occupied.addAll(course);
                return course;
            }
        }
        return null;
    }

    /**
     * @return a course from the source to a square beside the coastal buffer,
     *         within the length bounds, or null when the walk gets stuck or too
     *         long
     */
    private List<Location> walk(final Location source, final int minLength,
            final int maxLength) {
        final List<Location> course = new ArrayList<>();
        course.add(source);
        final Set<Location> own = new HashSet<>(course);
        while (true) {
            final Location current = course.get(course.size() - 1);
            if (course.size() >= minLength && grid.neighbours(current).stream()
                    .anyMatch(at -> grid.isLand(at) && touchesWater(at))) {
                return course;
            }
            if (course.size() >= maxLength) {
                return null;
            }
            final Location next = nextSquare(current, own);
            if (next == null) {
                return null;
            }
            course.add(next);
            own.add(next);
        }
    }

    /**
     * @return the lowest free land square beside the current end, or null when
     *         every choice would touch another course
     */
    private Location nextSquare(final Location current,
            final Set<Location> own) {
        final List<Location> options = new ArrayList<>(
                grid.neighbours(current));
        Collections.shuffle(options, random);
        Location best = null;
        for (final Location option : options) {
            if (grid.isLand(option)
                    && grid.terrainAt(option) != TerrainType.HILL
                    && grid.terrainAt(option) != TerrainType.MOUNTAIN
                    && !touchesWater(option) && !occupied.contains(option)
                    && !own.contains(option)
                    && !touchesCourse(option, current, own) && (best == null
                            || elevation.at(option) < elevation.at(best))) {
                best = option;
            }
        }
        return best;
    }

    /**
     * @return {@code true} when a square other than {@code current} beside
     *         {@code option} belongs to this course or an earlier one
     */
    private boolean touchesCourse(final Location option, final Location current,
            final Set<Location> own) {
        for (final Location neighbour : grid.neighbours(option)) {
            if (!neighbour.equals(current) && (own.contains(neighbour)
                    || occupied.contains(neighbour))) {
                return true;
            }
        }
        return false;
    }

    private boolean touchesOccupied(final Location at) {
        return grid.neighbours(at).stream().anyMatch(occupied::contains);
    }

    private boolean touchesWater(final Location at) {
        return at.getNeighbors().stream()
                .anyMatch(neighbour -> grid.isWater(neighbour));
    }

}
