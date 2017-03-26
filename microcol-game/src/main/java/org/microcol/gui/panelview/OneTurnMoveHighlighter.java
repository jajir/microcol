package org.microcol.gui.panelview;

import java.util.List;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

public class OneTurnMoveHighlighter {

	private List<Location> locations;

	public OneTurnMoveHighlighter() {
	}

	void setLocations(final List<Location> locations) {
		this.locations = Preconditions.checkNotNull(locations);
	}

	public boolean isItHighlighted(final Location location) {
		return locations != null && locations.contains(location);
	}

}
