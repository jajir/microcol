package org.microcol.model;

import com.google.common.base.Preconditions;

public class PlaceBuilder {

	private Location location;

	private Integer shipIncomingToColonies = null;

	private Integer shipIncomingToEurope = null;

	public PlaceBuilder setLocation(final Location location) {
		Preconditions.checkNotNull(location);
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		this.location = location;
		return this;
	}

	public PlaceBuilder setShipIncomingToColonies(int inHowManyturns) {
		Preconditions.checkState(location == null, "Location was already set");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		shipIncomingToColonies = inHowManyturns;
		return this;
	}

	public PlaceBuilder setShipIncomingToEurope(int inHowManyturns) {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		shipIncomingToEurope = inHowManyturns;
		return this;
	}

	public Place build(final Unit unit) {
		if (location != null) {
			return new PlaceLocation(unit, location);
		} else if (shipIncomingToColonies != null) {
			return new PlaceHighSea(unit, false, shipIncomingToColonies);
		} else if (shipIncomingToEurope != null) {
			return new PlaceHighSea(unit, true, shipIncomingToEurope);
		} else {
			throw new IllegalStateException("Place builder doesn't have any place");
		}
	}

}
