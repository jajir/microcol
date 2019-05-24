package org.microcol.gui.screen.colony;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.model.Direction;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Represents tile of colony field. It use on screen coordinates. Top left tile
 * starts at point [0,0].
 */
class ColonyFieldTile {

    /**
     * It should be added to direction to get map location.
     */
    private final static Location SHIFT = Location.of(2, 2);

    private final Tile tile;

    static ColonyFieldTile ofDirection(final Direction direction) {
        return ofLocation(direction.getVector());
    }

    static ColonyFieldTile ofLocation(final Location direction) {
        final Tile tile = Tile.ofLocation(direction.add(SHIFT));
        return new ColonyFieldTile(tile);
    }

    private ColonyFieldTile(final Tile tile) {
        this.tile = Preconditions.checkNotNull(tile);
    }

    Point getTopLeftCorner() {
        return tile.getTopLeftCorner();
    }

    boolean isIn(final Point point) {
        return tile.isIn(point);
    }

}
