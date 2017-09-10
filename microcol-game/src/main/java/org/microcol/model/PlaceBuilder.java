package org.microcol.model;

import com.google.common.base.Preconditions;

public class PlaceBuilder {

	private Location location;

	private Integer shipIncomingToColonies = null;

	private Integer shipIncomingToEurope = null;

	private boolean unitIsInEuropePortPier = false;

	private Unit cargoHolder = null;

	private ConstructionColony constructionColony = null;

	private FieldColony fieldColony = null;

	private void checkThatEverythingIsNull() {
		Preconditions.checkState(location == null, "Location wa already set");
		Preconditions.checkState(shipIncomingToColonies == null, "Ship is alredy on way to colonies");
		Preconditions.checkState(shipIncomingToEurope == null, "Ship is alredy on way to europe");
		Preconditions.checkState(!unitIsInEuropePortPier, "Unit is alredy in Europe port pier");
		Preconditions.checkState(cargoHolder == null, "Unit was already put to cargo");
		Preconditions.checkState(constructionColony == null, "Unit was already put to colony construction");
		Preconditions.checkState(fieldColony == null, "Unit was already put to field");
	}

	public PlaceBuilder setLocation(final Location location) {
		Preconditions.checkNotNull(location);
		checkThatEverythingIsNull();
		this.location = location;
		return this;
	}

	public PlaceBuilder setShipIncomingToColonies(int inHowManyturns) {
		checkThatEverythingIsNull();
		shipIncomingToColonies = inHowManyturns;
		return this;
	}

	public PlaceBuilder setShipIncomingToEurope(int inHowManyturns) {
		checkThatEverythingIsNull();
		shipIncomingToEurope = inHowManyturns;
		return this;
	}

	public PlaceBuilder setUnitToEuropePortPier() {
		checkThatEverythingIsNull();
		unitIsInEuropePortPier = true;
		return this;
	}

	public PlaceBuilder setToCargoSlot(final Unit cargoHolder) {
		checkThatEverythingIsNull();
		this.cargoHolder = Preconditions.checkNotNull(cargoHolder);
		return this;
	}

	public PlaceBuilder setToCostruction(final ConstructionType constructionType, final Colony colony, final int positon) {
		checkThatEverythingIsNull();
		constructionColony = new ConstructionColony(constructionType, colony, positon);
		return this;
	}

	public PlaceBuilder setUnitToFiled(final Location fieldDirection, final Colony colony) {
		checkThatEverythingIsNull();
		fieldColony = new FieldColony(fieldDirection, colony);
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
		} else if (constructionColony != null) {
			return new PlaceConstructionSlot(unit,
					constructionColony.getConstruction().getSlotAt(constructionColony.getPositon()));
		} else if (fieldColony != null) {
			return new PlaceColonyField(unit, fieldColony.getColonyField());
		} else if (cargoHolder != null) {
			return new PlaceCargoSlot(unit, cargoHolder.getCargo().getEmptyCargoSlot().orElseThrow(
					() -> new IllegalStateException("There is no empty cargo slot at unit (" + unit + ")")));
		} else {
			throw new IllegalStateException("Place builder doesn't have any place");
		}
	}

	private class ConstructionColony {

		private final ConstructionType constructionType;
		private final Colony colony;
		private final int positon;

		ConstructionColony(final ConstructionType constructionType, final Colony colony, final int positon) {
			this.constructionType = Preconditions.checkNotNull(constructionType);
			this.colony = Preconditions.checkNotNull(colony);
			this.positon = Preconditions.checkNotNull(positon);
		}

		public Construction getConstruction() {
			return colony.getConstructionByType(constructionType);
		}

		private int getPositon() {
			return positon;
		}

	}

	private class FieldColony {
		private final Location fieldDirection;
		private final Colony colony;

		FieldColony(final Location fieldDirection, final Colony colony) {
			this.fieldDirection = Preconditions.checkNotNull(fieldDirection);
			this.colony = Preconditions.checkNotNull(colony);
		}
		
		public ColonyField getColonyField(){
			return colony.getColonyFieldInDirection(fieldDirection);
		}

	}

}
