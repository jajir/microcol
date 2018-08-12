package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Location {

    /**
     * Minimal map x-axe value.
     */
    public static final int MAP_MIN_X = 1;

    /**
     * Minimal map y-axe value.
     */
    public static final int MAP_MIN_Y = 1;

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

    /**
     * Return distance between two locations. It use Euclidean distance.
     *
     * @param location
     *            required location
     * @return distance between two locations
     */
    public int getDistance(final Location location) {
        Preconditions.checkNotNull(location);

        final int diffX = x - location.x;
        final int diffY = y - location.y;
        return (int) Math.round(Math.sqrt(diffX * diffX + diffY * diffY));
    }

    /**
     * Return distance between two locations. It use Manhattan distance. It's
     * described at <a href= "https://en.wiktionary.org/wiki/Manhattan_distance"
     * >https://en.wiktionary.org/wiki/Manhattan_distance</a>.
     * <p>
     * This distance is better for path calculations because it nicely work with
     * integers.
     * </p>
     *
     * @param location
     *            required location
     * @return distance between two locations
     */
    public int getDistanceManhattan(final Location location) {
        Preconditions.checkNotNull(location);

        return Math.abs(x - location.x) + Math.abs(y - location.y);
    }

    public Location add(final Location location) {
        Preconditions.checkNotNull(location);

        return Location.of(x + location.x, y + location.y);
    }

    public Location sub(final Location location) {
        Preconditions.checkNotNull(location);

        return Location.of(x - location.x, y - location.y);
    }

    public boolean isNeighbor(final Location location) {
        Preconditions.checkNotNull(location);

        if (equals(location)) {
            return false;
        }

        return Math.abs(x - location.x) <= 1 && Math.abs(y - location.y) <= 1;
    }

    public List<Location> getNeighbors() {
        return Direction.getVectors().stream().map(direction -> add(direction))
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
        return Direction.getVectors().contains(this);
    }

    public static Location of(final int x, final int y) {
        return new Location(x, y);
    }
}
