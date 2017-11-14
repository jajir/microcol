package org.microcol.model;

import org.microcol.model.store.ConstructionSlotPo;

import com.google.common.base.Preconditions;

public class ConstructionSlot {

	private final Construction construction;
	
	private PlaceConstructionSlot placeConstruction;
	
	ConstructionSlot(final Construction construction){
		this.construction = Preconditions.checkNotNull(construction);
	}

	public boolean isEmpty() {
		return placeConstruction == null;
	}

	public void set(PlaceConstructionSlot placeConstruction) {
		Preconditions.checkState(isEmpty(),
				String.format("Can't insert placeConstruction (%s) slot (%s) is not empty.", placeConstruction, this));
		this.placeConstruction = placeConstruction;
	}
	
	Colony getColony(){
		return construction.getColony();
	}
	
	ConstructionSlotPo save(){
		final ConstructionSlotPo out = new ConstructionSlotPo();
		if (!isEmpty()) {
			out.setWorkerId(getUnit().getId());
		}
		return out;
	}
	
	public void clear(final boolean validate){
		placeConstruction = null;
		if(validate){
			construction.verifyNumberOfUnitsOptionallyDestroyColony();
		}
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
	
	public boolean isValid(){
		return construction.isValid();
	}

}
