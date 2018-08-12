package org.microcol.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Enumeration represents direction in which unit could move. Each direction is
 * defined as vector. All non-zero length on axes X and Y have length 1. Direction vectors are counted at:
 * <pre>
 *[-1,-1]   North
 *            |
 *            |
 *            |
 *            | [0,0]
 * -----------+----------- East
 *            |
 *            |
 *            |
 *            |
 *            |           [1,1]
 * </pre>
 * 
 */
public enum Direction {

    north(Location.of(0, -1)),

    northEast(Location.of(1, -1)),

    east(Location.of(1, 0)),

    southEast(Location.of(1, 1)),

    south(Location.of(0, 1)),

    southWest(Location.of(-1, 1)),

    west(Location.of(-1, 0)),

    northWest(Location.of(-1, -1));

    private final static List<Direction> ALL = ImmutableList.copyOf(Direction.values());

    /**
     * Based on given vector it return direction.
     *
     * @param vector
     *            required vector
     * @return found direction
     */
    public static Direction valueOf(final Location vector) {
        Preconditions.checkNotNull(vector);
        for (final Direction dir : Direction.values()) {
            if (vector.equals(dir.getVector())) {
                return dir;
            }
        }
        throw new IllegalArgumentException(
                String.format("It's no posible to find direction for vector '%s'.", vector));
    }

    private final Location vector;

    private Direction(final Location vector) {
        this.vector = Preconditions.checkNotNull(vector);
    }

    /**
     * Get all direction as list
     *
     * @return immutable list of all directions
     */
    public static List<Direction> getAll() {
        return ALL;
    }

    /**
     * Return immutable list of all vectors.
     *
     * @return immutable list of locations
     */
    public static List<Location> getVectors() {
        return ALL.stream().map(Direction::getVector).collect(ImmutableList.toImmutableList());
    }

    public Location getVector() {
        return vector;
    }

    public boolean isOrientedNorth() {
        return vector.getY() < 0;
    }

    public boolean isOrientedSouth() {
        return vector.getY() > 0;
    }

    public boolean isOrientedEast() {
        return vector.getX() > 0;
    }

    public boolean isOrientedWest() {
        return vector.getX() < 0;
    }

}
