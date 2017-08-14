package org.microcol.model;

import com.google.common.base.Preconditions;

public class PlaceCargoSlot extends AbstractPlace {

	private final CargoSlot cargoSlot;

	PlaceCargoSlot(final Unit unitToCargo, final CargoSlot cargoSlot) {
		super(unitToCargo);
		this.cargoSlot = Preconditions.checkNotNull(cargoSlot);
		cargoSlot.unsafeStore(this);
	}
	
	@Override
	public void destroy() {
		cargoSlot.empty();
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
