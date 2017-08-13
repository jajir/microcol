package org.microcol.model;

import com.google.common.base.Preconditions;

public class PlaceBuilder {

	private Location location;

	private Integer shipIncomingToColonies = null;

	private Integer shipIncomingToEurope = null;

	private boolean shipIsInEuropePortPier = false;

	public PlaceBuilder setLocation(final Location location) {
		Preconditions.checkNotNull(location);
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!shipIsInEuropePortPier, "Ship is alredy in Europe port pier");
		this.location = location;
		return this;
	}

	public PlaceBuilder setShipIncomingToColonies(int inHowManyturns) {
		Preconditions.checkState(location == null, "Location was already set");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!shipIsInEuropePortPier, "Ship is alredy in Europe port pier");
		shipIncomingToColonies = inHowManyturns;
		return this;
	}

	public PlaceBuilder setShipIncomingToEurope(int inHowManyturns) {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(!shipIsInEuropePortPier, "Ship is alredy in Europe port pier");
		shipIncomingToEurope = inHowManyturns;
		return this;
	}

	public PlaceBuilder setShipToEuropePortPier() {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		shipIsInEuropePortPier = true;
		return this;
	}

	public Place build(final Unit unit) {
		if (location != null) {
			return new PlaceLocation(unit, location);
		} else if (shipIncomingToColonies != null) {
			return new PlaceHighSea(unit, false, shipIncomingToColonies);
		} else if (shipIncomingToEurope != null) {
			return new PlaceHighSea(unit, true, shipIncomingToEurope);
		} else if (shipIsInEuropePortPier) {
			return new PlaceEuropePier(unit);
		} else {
			throw new IllegalStateException("Place builder doesn't have any place");
		}
	}

}
