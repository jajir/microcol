package org.microcol.gui.panelview;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Event is send when new tile locations is selected. 
 */
public class TileWasSelectedEvent {

	private final Location location;
	
	public TileWasSelectedEvent(final Location location) {
		this.location = Preconditions.checkNotNull(location);
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
}
