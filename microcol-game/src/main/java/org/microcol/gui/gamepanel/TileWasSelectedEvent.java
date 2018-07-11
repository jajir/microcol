package org.microcol.gui.gamepanel;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused tile at map.
 *
 */
public final class TileWasSelectedEvent {

    private final Location location;

    public TileWasSelectedEvent(final Location location) {
        this.location = Preconditions.checkNotNull(location);
    }

    public Location getLocation() {
        return location;
    }

}
