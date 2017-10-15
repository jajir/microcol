package org.microcol.model;

import org.microcol.model.store.ConstructionSlotPo;

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
	
	ConstructionSlotPo save(){
		final ConstructionSlotPo out = new ConstructionSlotPo();
		if (!isEmpty()) {
			out.setWorkerId(getUnit().getId());
		}
		return out;
	}
	
	public void clear(){
		placeConstruction = null;
	}
	
	public Unit getUnit(){
		return placeConstruction.getUnit();
	}
	
	/**
	 * When there is unit than return unit production modifier otherwise return
	 * 0.
	 *
	 * @param producedGoodType
	 *            required produces good type
	 * @return return production modifier
	 */
	public float getProductionModifier(final GoodType producedGoodType){
		if(isEmpty()){
			return 0;
		}else{
			return getUnit().getType().getProductionModifier(producedGoodType);
		}
	}

}
