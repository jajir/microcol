package org.microcol.gui.event;

import org.microcol.model.Location;
import org.microcol.model.TileOcean;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused
 *
 */
public class FocusedTileEvent {

	private final Location location;

	private final TileOcean tile;
	
	public FocusedTileEvent(final Location location, final TileOcean tile) {
		this.location = Preconditions.checkNotNull(location);
		this.tile = Preconditions.checkNotNull(tile);
	}

	public Location getLocation() {
		return location;
	}

	public TileOcean getTile() {
		return tile;
	}

}
