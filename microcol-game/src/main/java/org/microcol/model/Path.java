package org.microcol.model;

import java.util.Collection;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Path {
	private final ImmutableList<Location> locations;

	private Path(final List<Location> locations) {
		Preconditions.checkNotNull(locations);
		Preconditions.checkArgument(locations.size() > 0, "Path cannot be empty.");
		checkAdjacent(locations);

		// Throws NPE if any element is null.
		this.locations = ImmutableList.copyOf(locations);
	}

	private void checkAdjacent(final List<Location> locations) {
		for (int i = 1; i < locations.size(); i++) {
			final Location prevLocation = locations.get(i - 1);
			final Location nextLocation = locations.get(i);
			// Possible NPE is not problem here.
			if (!prevLocation.isAdjacent(nextLocation)) {
				throw new IllegalArgumentException(String.format("Locations are not adjacent: %s", locations));
			}
		}
	}

	public boolean contains(final Location location) {
		Preconditions.checkNotNull(location);

		return locations.contains(location);
	}

	// NPE pokud se narazi na null (kdyz je shoda drive, tak nic)
	public boolean containsAny(final Collection<Location> locations) {
		Preconditions.checkNotNull(locations);

		return locations.stream().anyMatch(location -> contains(location));
	}

	public List<Location> getLocations() {
		return locations;
	}

	public Location getStart() {
		return locations.get(0);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("locations", locations)
			.toString();
	}

	public static Path of(final List<Location> locations) {
		return new Path(locations);
	}
}
