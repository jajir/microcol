package org.microcol.model.store;

import org.microcol.model.Location;

import com.google.common.base.MoreObjects;

public class PlaceMapPo extends PlacePo {

	private Location location;
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("location", location).toString();
	}

}
