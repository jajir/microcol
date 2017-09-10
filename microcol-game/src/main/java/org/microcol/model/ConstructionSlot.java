package org.microcol.model;

import com.google.common.base.Preconditions;

public class ConstructionSlot {

	private PlaceConstructionSlot placeConstruction;

	public boolean isEmpty() {
		return placeConstruction == null;
	}

	public void set(PlaceConstructionSlot placeConstruction) {
		Preconditions.checkState(isEmpty(),
				String.format("Can't insert placeConstruction (%s) slot (%s) is not empty.", placeConstruction, this));
		this.placeConstruction = placeConstruction;
	}
	
	public void clear(){
		placeConstruction = null;
	}
	
	public Unit getUnit(){
		return placeConstruction.getUnit();
	}

}
