package org.microcol.model.store;

import org.microcol.model.Direction;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;

public final class PlaceMapPo extends PlacePo {

    private Location location;

    private Direction orientation;

    public Location getLocation() {
	return location;
    }

    public void setLocation(Location location) {
	this.location = location;
    }

    @Override
    public String toString() {
	return MoreObjects.toStringHelper(this.getClass()).add("location", location)
		.add("orientation", orientation).toString();
    }

    public Direction getOrientation() {
	return orientation;
    }

    public void setOrientation(Direction orientation) {
	this.orientation = orientation;
    }

}
