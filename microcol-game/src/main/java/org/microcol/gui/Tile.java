package org.microcol.gui;

import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Tile is map square at main game screen. Define basic tile constants.
 */
public class Tile {

    /**
     * Define width in pixels of one game tile.
     */
    public static final int TILE_WIDTH_IN_PX = 45;

    /**
     * Define center point of tile.
     */
    public static final Point TILE_SIZE = Point.of(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);

    /**
     * Define center point of tile.
     */
    public static final Point TILE_CENTER = TILE_SIZE.divide(2);

    private final Point topLeftCorner;

    public static Tile of(final Point topLeftCorner) {
        return new Tile(topLeftCorner);
    }

    public static Tile ofLocation(final Location location) {
        return new Tile(
                Point.of(location.getX() - Location.MAP_MIN_X, location.getY() - Location.MAP_MIN_Y)
                        .multiply(TILE_WIDTH_IN_PX));
    }

    private Tile(final Point topLeftCorner) {
        this.topLeftCorner = Preconditions.checkNotNull(topLeftCorner);
    }

    public Point getTopLeftCorner() {
        return topLeftCorner;
    }

    public Point getBottomRightCorner() {
        return topLeftCorner.add(TILE_SIZE);
    }

    public boolean isIn(final Point point) {
        return topLeftCorner.getX() <= point.getX() && getBottomRightCorner().getX() >= point.getX()
                && topLeftCorner.getY() <= point.getY()
                && getBottomRightCorner().getY() >= point.getY();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Tile)) {
            return false;
        }

        final Tile t = (Tile) object;

        return topLeftCorner.equals(t.getTopLeftCorner());
    }

    @Override
    public int hashCode() {
        return topLeftCorner.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", topLeftCorner.getX())
                .add("y", topLeftCorner.getY()).toString();
    }

    /**
     * Get tile position as location
     * 
     * @return location
     */
    public Location toLocation() {
        final Point p = topLeftCorner.divide(TILE_WIDTH_IN_PX).add(Location.MAP_MIN_X,
                Location.MAP_MIN_Y);
        return Location.of(p.getX(), p.getY());
    }

    /**
     * This count location of bottom right corner.
     * 
     * @return return bottom right corner of location when this point belongs
     */
    public Location toLocationCeilUp() {
        final Point p = Point
                .of((int) Math.ceil(topLeftCorner.getX() / (float) TILE_WIDTH_IN_PX),
                        (int) Math.ceil(topLeftCorner.getY() / (float) TILE_WIDTH_IN_PX))
                .add(Location.MAP_MIN_X, Location.MAP_MIN_Y);
        return Location.of(p.getX(), p.getY());
    }

}
