package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Tile;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;

/**
 * holds canvas in world map coordinates (Location).
 */
class CanvasInMapCoordinates {

    /**
     * Locations in world of top left corner of visible area.
     */
    private final Location topLeft;

    /**
     * Locations in world of bottom right corner of visible area.
     */
    private final Location bottomRight;

    static CanvasInMapCoordinates make(final VisibleAreaService visibleArea,
            final WorldMap worldMap) {
        final Location lTopLeft = Tile.of(visibleArea.getTopLeft()).toLocation();
        final Location lBottomRight = Tile.of(visibleArea.getBottomRight()).toLocation();
        final Location topLeft = Location.of(Math.max(Location.MAP_MIN_X, lTopLeft.getX()),
                Math.max(Location.MAP_MIN_Y, lTopLeft.getY()));
        final Location bottomRight = Location.of(
                Math.min(lBottomRight.getX(), worldMap.getMaxLocationX()),
                Math.min(lBottomRight.getY(), worldMap.getMaxLocationY()));
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

    protected Location getTopLeft() {
        return topLeft;
    }

    protected Location getBottomRight() {
        return bottomRight;
    }

}
