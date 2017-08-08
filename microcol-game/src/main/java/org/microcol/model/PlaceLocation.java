package org.microcol.model;

public class PlaceLocation extends AbstractPlace {

	private Location location;

	PlaceLocation(final Unit unit, final Location location) {
		super(unit);
		this.location = location;
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
