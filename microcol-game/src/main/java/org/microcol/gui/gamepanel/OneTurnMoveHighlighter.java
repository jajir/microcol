package org.microcol.gui.gamepanel;

import java.util.List;

import org.microcol.model.Location;

/**
 * Helps to highlight area where use could move in first turn.
 */
public class OneTurnMoveHighlighter {

	private List<Location> locations;

	void setLocations(final List<Location> locations) {
		this.locations = locations;
	}

	public boolean isItHighlighted(final Location location) {
		return locations != null && locations.contains(location);
	}

}
