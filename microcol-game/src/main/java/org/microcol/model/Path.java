package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Path {
	private final ImmutableList<Location> locations;

	public Path(final List<Location> locations) {
		Preconditions.checkNotNull(locations);
		Preconditions.checkArgument(locations.size() > 0, "Path cannot be empty.");
		Preconditions.checkArgument(isValid(locations), "Invalid path: %s", locations);

		this.locations = ImmutableList.copyOf(locations);
	}

	private boolean isValid(final List<Location> locations) {
		// TODO JKA Use streams
		for (int i = 1; i < locations.size(); i++) {
			final Location prevLocation = locations.get(i - 1);
			final Location nextLocation = locations.get(i);
			if (!prevLocation.isAdjacent(nextLocation)) {
				return false;
			}
		}

		return true;
	}

	// TODO JKA Test
	public boolean contains(final Location location) {
		Preconditions.checkNotNull(location);

		return locations.contains(location);
	}

	public List<Location> getLocations() {
		return locations;
	}

	// TODO JKA Test
	public Location getFirstLocation() {
		return locations.get(0);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("locations", locations)
			.toString();
	}
}
