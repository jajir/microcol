package org.microcol.model;

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

}
