package org.microcol.model;

import com.google.common.base.Preconditions;

public class PlaceBuilder {

	private Location location;

	private Integer shipIncomingToColonies = null;

	private Integer shipIncomingToEurope = null;

	private boolean unitIsInEuropePortPier = false;

	private Unit cargoHolder = null;
	
	private ConstructionTown constructionTown = null;
	
	private void checkThatEverythingIsNull(){
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!unitIsInEuropePortPier, "Unit is alredy in Europe port pier");
		Preconditions.checkState(this.cargoHolder == null, "Unit was already put to cargo");
		Preconditions.checkState(cargoHolder == null, "Unit was already put to cargo");
		Preconditions.checkState(this.cargoHolder == null, "Unit was already put to cargo");
	}

	public PlaceBuilder setLocation(final Location location) {
		Preconditions.checkNotNull(location);
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!unitIsInEuropePortPier, "Unit is alredy in Europe port pier");
		Preconditions.checkState(cargoHolder == null, "Unit was already put to cargo");
		this.location = location;
		return this;
	}

	public PlaceBuilder setShipIncomingToColonies(int inHowManyturns) {
		Preconditions.checkState(location == null, "Location was already set");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!unitIsInEuropePortPier, "Unit is alredy in Europe port pier");
		Preconditions.checkState(cargoHolder == null, "Unit was already put to cargo");
		shipIncomingToColonies = inHowManyturns;
		return this;
	}

	public PlaceBuilder setShipIncomingToEurope(int inHowManyturns) {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(!unitIsInEuropePortPier, "Unit is alredy in Europe port pier");
		Preconditions.checkState(cargoHolder == null, "Unit was already put to cargo");
		shipIncomingToEurope = inHowManyturns;
		return this;
	}

	public PlaceBuilder setUnitToEuropePortPier() {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(cargoHolder == null, "Unit was already put to cargo");
		unitIsInEuropePortPier = true;
		return this;
	}

	public PlaceBuilder setToCargoSlot(final Unit cargoHolder) {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!unitIsInEuropePortPier, "Unit is alredy in Europe port pier");
		Preconditions.checkState(this.cargoHolder == null, "Unit was already put to cargo");
		this.cargoHolder = Preconditions.checkNotNull(cargoHolder);
		return this;
	}

	public PlaceBuilder setToCostruction(final ConstructionType constructionType, final Town town) {
		checkThatEverythingIsNull();
		constructionTown = new ConstructionTown(constructionType, town);
		return this;
	}

	public Place build(final Unit unit) {
		if (location != null) {
			return new PlaceLocation(unit, location);
		} else if (shipIncomingToColonies != null) {
			return new PlaceHighSea(unit, false, shipIncomingToColonies);
		} else if (shipIncomingToEurope != null) {
			return new PlaceHighSea(unit, true, shipIncomingToEurope);
		} else if (unitIsInEuropePortPier) {
			return new PlaceEuropePier(unit);
		} else if (constructionTown != null) {
			return new PlaceConstruction(unit, constructionTown.getConstruction());
		} else if (cargoHolder != null) {
			return new PlaceCargoSlot(unit, cargoHolder.getCargo().getEmptyCargoSlot().orElseThrow(
					() -> new IllegalStateException("There is no empty cargo slot at unit (" + unit + ")")));
		} else {
			throw new IllegalStateException("Place builder doesn't have any place");
		}
	}

	class ConstructionTown {
		
		private final ConstructionType constructionType;
		private final Town town;

		ConstructionTown(final ConstructionType constructionType, final Town town) {
			this.constructionType = Preconditions.checkNotNull(constructionType);
			this.town = Preconditions.checkNotNull(town);
		}

		public Construction getConstruction() {
			return town.getConstructionByType(constructionType);
		}
		
		public ConstructionType getConstructionType() {
			return constructionType;
		}

		public Town getTown() {
			return town;
		}
	}

}
