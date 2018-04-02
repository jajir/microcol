package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Location {

    public static final Location DIRECTION_NORTH = new Location(0, -1);
    public static final Location DIRECTION_NORTH_EAST = new Location(1, -1);
    public static final Location DIRECTION_EAST = new Location(1, 0);
    public static final Location DIRECTION_SOUTH_EAST = new Location(1, 1);
    public static final Location DIRECTION_SOUTH = new Location(0, 1);
    public static final Location DIRECTION_SOUTH_WEST = new Location(-1, 1);
    public static final Location DIRECTION_WEST = new Location(-1, 0);
    public static final Location DIRECTION_NORTH_WEST = new Location(-1, -1);

    /**
     * Minimal map x-axe value.
     */
    public static final int MAP_MIN_X = 1;

    /**
     * Minimal map y-axe value.
     */
    public static final int MAP_MIN_Y = 1;

    public static final List<Location> DIRECTIONS = ImmutableList.of(DIRECTION_NORTH,
            DIRECTION_NORTH_EAST, DIRECTION_EAST, DIRECTION_SOUTH_EAST, DIRECTION_SOUTH,
            DIRECTION_SOUTH_WEST, DIRECTION_WEST, DIRECTION_NORTH_WEST);

    private final int x;
    private final int y;

    private Location(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDistance(final Location location) {
        Preconditions.checkNotNull(location);

        return Math.abs(x - location.x) + Math.abs(y - location.y);
    }

    public Location add(final Location location) {
        Preconditions.checkNotNull(location);

        return Location.of(x + location.x, y + location.y);
    }

    public boolean isNeighbor(final Location location) {
        Preconditions.checkNotNull(location);

        if (equals(location)) {
            return false;
        }

        return Math.abs(x - location.x) <= 1 && Math.abs(y - location.y) <= 1;
    }

    public List<Location> getNeighbors() {
        return DIRECTIONS.stream().map(direction -> add(direction))
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public int hashCode() {
        return x + (y << Integer.SIZE / 2);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Location)) {
            return false;
        }

        final Location location = (Location) object;

        return x == location.x && y == location.y;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", x).add("y", y).toString();
    }

    public boolean isDirection() {
        return DIRECTIONS.contains(this);
    }

    public static Location of(final int x, final int y) {
        return new Location(x, y);
    }
}
