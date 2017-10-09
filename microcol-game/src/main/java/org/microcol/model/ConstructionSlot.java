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
	
	public int getMaxConsumptionPerTurn(final int baseConstructionProductionPerTurn, final GoodType producedGoodType) {
		if(isEmpty()){
			return 0;
		}
		return (int)(baseConstructionProductionPerTurn * getUnit().getType().getProductionModifier(producedGoodType));
	}
	
	//TODO it's duplicated method to previous one
	public int getMaxProductionPerTurn(final int baseConstructionProductionPerTurn, final GoodType producedGoodType) {
		if(isEmpty()){
			return 0;
		}
		return (int)(baseConstructionProductionPerTurn * getUnit().getType().getProductionModifier(producedGoodType));		
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
