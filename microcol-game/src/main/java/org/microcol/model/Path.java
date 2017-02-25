package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Path {
	private final ImmutableList<Location> locations;

	public Path(final List<Location> locations) {
		Preconditions.checkNotNull(locations);
		Preconditions.checkArgument(locations.size() > 0);
		Preconditions.checkArgument(isValid(locations));

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

	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("locations", locations)
			.toString();
	}
}
