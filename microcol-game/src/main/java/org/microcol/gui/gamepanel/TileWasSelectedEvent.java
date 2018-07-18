package org.microcol.gui.gamepanel;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused tile at map.
 *
 */
public final class TileWasSelectedEvent {

    private final Location location;

    private final ScrollToFocusedTile scrollToFocusedTile;

    public TileWasSelectedEvent(final Location location,
            final ScrollToFocusedTile scrollToFocusedTile) {
        this.location = Preconditions.checkNotNull(location);
        this.scrollToFocusedTile = Preconditions.checkNotNull(scrollToFocusedTile);
    }

    /**
     * Return selected location.
     *
     * @return selected {@link Location}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get scrolling strategy.
     *
     * @return the scrollToFocusedTile
     */
    public ScrollToFocusedTile getScrollToFocusedTile() {
        return scrollToFocusedTile;
    }

}
