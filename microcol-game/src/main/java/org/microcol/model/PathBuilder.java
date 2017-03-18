package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class PathBuilder {
	private final List<Location> locations;

	public PathBuilder() {
		locations = new ArrayList<>();
	}

	public boolean isEmpty() {
		return locations.isEmpty();
	}

	public int getLength() {
		return locations.size();
	}

	public PathBuilder add(final int x, final int y) {
		locations.add(Location.of(x, y));

		return this;
	}

	public PathBuilder add(final Location location) {
		locations.add(location);

		return this;
	}

	public Path build() {
		return new Path(locations);
	}
}
