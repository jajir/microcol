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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Path [locations = ");
		builder.append(locations); // TODO JKA Vyzkoušet, jak to vypadá.
		builder.append("]");

		return builder.toString();
	}
}
