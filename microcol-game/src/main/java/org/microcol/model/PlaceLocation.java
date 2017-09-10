package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class PlaceLocation extends AbstractPlace {

	private Location location;

	PlaceLocation(final Unit unit, final Location location) {
		super(unit);
		this.location = Preconditions.checkNotNull(location);
	}

	@Override
	public String getName() {
		return "Location";
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PlaceLocation.class)
				.add("unit id", getUnit().getId())
				.add("location", location)
				.toString();
	}

}
