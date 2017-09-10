package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * 
 */
public class PlaceCargoSlot extends AbstractPlace {

	private final CargoSlot cargoSlot;

	PlaceCargoSlot(final Unit unitToCargo, final CargoSlot cargoSlot) {
		super(unitToCargo);
		this.cargoSlot = Preconditions.checkNotNull(cargoSlot);
		cargoSlot.unsafeStore(this);
	}
	
	public Player getCargoSlotOwner(){
		return cargoSlot.getOwnerPlayer();
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
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PlaceCargoSlot.class)
				.add("unit id", getUnit().getId())
				.add("owner", cargoSlot.getOwnerPlayer())
				.toString();
	}

}
