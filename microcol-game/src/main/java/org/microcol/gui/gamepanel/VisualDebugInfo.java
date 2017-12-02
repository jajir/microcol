package org.microcol.gui.gamepanel;

import java.util.Arrays;
import java.util.List;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

public class VisualDebugInfo {

	private List<Location> locations;

	public VisualDebugInfo() {
		locations = Arrays.asList();
	}

	public void setLocations(final List<Location> locations) {
		this.locations = Preconditions.checkNotNull(locations);
	}

	public List<Location> getLocations() {
		return locations;
	}

}
