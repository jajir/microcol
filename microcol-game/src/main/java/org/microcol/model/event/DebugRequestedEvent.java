package org.microcol.model.event;

import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class DebugRequestedEvent extends ModelEvent {
	private final List<Location> locations;

	public DebugRequestedEvent(final Model model, final List<Location> locations) {
		super(model);

		this.locations = ImmutableList.copyOf(locations);
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
