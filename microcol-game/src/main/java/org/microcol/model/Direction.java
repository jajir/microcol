package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Enumeration represents direction in which unit could move. Each direction is
 * defined as vector. All non-zero length on axes X and Y have length 1.
 */
public enum Direction {

    north(Location.DIRECTION_NORTH),
    northEast(Location.DIRECTION_NORTH_EAST),
    east(Location.DIRECTION_EAST),
    southEast(Location.DIRECTION_SOUTH_EAST),
    south(Location.DIRECTION_SOUTH),
    southWest(Location.DIRECTION_SOUTH_WEST),
    west(Location.DIRECTION_WEST),
    northWest(Location.DIRECTION_NORTH_WEST);

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
