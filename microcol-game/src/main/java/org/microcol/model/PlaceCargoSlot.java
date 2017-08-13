package org.microcol.model;

import com.google.common.base.Preconditions;

public class PlaceCargoSlot extends AbstractPlace {

	private final CargoSlot cargoSlot;

	PlaceCargoSlot(final Unit unit, final CargoSlot cargoSlot) {
		super(unit);
		this.cargoSlot = Preconditions.checkNotNull(cargoSlot);
	}

	@Override
	public String getName() {
		return "Cargo slot";
	}

	public CargoSlot getCargoSlot() {
		return cargoSlot;
	}
	
	public boolean isOwnerAtEuropePort(){
		return cargoSlot.getHold().getOwner().isAtEuropePort();
	}

}
