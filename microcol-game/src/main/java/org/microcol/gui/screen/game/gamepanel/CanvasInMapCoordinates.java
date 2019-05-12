package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Tile;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;

/**
 * holds canvas in world map coordinates (Location).
 */
public class CanvasInMapCoordinates {

    /**
     * Locations in world of top left corner of visible area.
     */
    private final Location topLeft;

    /**
     * Locations in world of bottom right corner of visible area.
     */
    private final Location bottomRight;

    public static CanvasInMapCoordinates make(final VisibleAreaService visibleArea,
            final Location mapSize) {
        final Location lTopLeft = Tile.of(visibleArea.getTopLeft()).toLocation();
        final Location lBottomRight = Tile.of(visibleArea.getBottomRight()).toLocation();
        final Location topLeft = Location.of(Math.max(Location.MAP_MIN_X, lTopLeft.getX()),
                Math.max(Location.MAP_MIN_Y, lTopLeft.getY()));
        final Location bottomRight = Location.of(Math.min(lBottomRight.getX(), mapSize.getX()),
                Math.min(lBottomRight.getY(), mapSize.getY()));
        return new CanvasInMapCoordinates(topLeft, bottomRight);
    }

    private CanvasInMapCoordinates(final Location topLeft, final Location bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CanvasInMapCoordinates.class).add("topLeft", topLeft)
                .add("bottomRight", bottomRight).toString();
    }

    public Location getTopLeft() {
        return topLeft;
    }

    public Location getBottomRight() {
        return bottomRight;
    }

}
