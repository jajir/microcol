package org.microcol.gui.panelview;

import java.util.List;

import org.microcol.model.Location;

/**
 * Helps highlight area where user could move in first turn.
 */
public class OneTurnMoveHighlighter {

	private List<Location> locations;

	public OneTurnMoveHighlighter() {
	}

	void setLocations(final List<Location> locations) {
		this.locations = locations;
	}

	public boolean isItHighlighted(final Location location) {
		return locations != null && locations.contains(location);
	}

}
