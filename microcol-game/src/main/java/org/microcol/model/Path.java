package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class Path {
	private final List<Location> locations;

	public Path(final List<Location> locations) {
		// TODO JKA Add not null and not empty test.
		this.locations = new ArrayList<>(locations);
	}

	// TODO JKA Předělat na immutable.
	public List<Location> getLocations() {
		return locations;
	}
}
