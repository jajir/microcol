package org.microcol.model;

import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class PlaceLocation extends AbstractPlace {

    private Location location;

    private Direction orientation;

    PlaceLocation(final Unit unit, final Location location, final Direction orientation) {
	super(unit);
	this.location = Preconditions.checkNotNull(location);
	setOrientation(orientation);
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

    public Direction getOrientation() {
	return orientation;
    }

    public void setOrientation(final Direction orientation) {
	if (orientation == null) {
	    this.orientation = Direction.east;
	} else {
	    this.orientation = orientation;
	}
    }

    @Override
    public String toString() {
	return MoreObjects.toStringHelper(PlaceLocation.class).add("unit id", getUnit().getId())
		.add("location", location).add("orientation", orientation).toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
	final PlaceMapPo out = new PlaceMapPo();
	out.setLocation(location);
	out.setOrientation(orientation);
	unitPo.setPlaceMap(out);
	return out;
    }

}
