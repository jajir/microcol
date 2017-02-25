package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class PathBuilder {
	private final List<Location> locations;

	public PathBuilder() {
		locations = new ArrayList<>();
	}

	public PathBuilder add(final int x, final int y) {
		locations.add(new Location(x, y));

		return this;
	}

	public Path build() {
		return new Path(locations);
	}
}
