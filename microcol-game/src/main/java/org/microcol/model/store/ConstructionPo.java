package org.microcol.model.store;

import java.util.List;

import org.microcol.model.ConstructionType;

public class ConstructionPo {
	
	private ConstructionType type;

	private List<ConstructionSlotPo> slots;

	/**
	 * @return the type
	 */
	public ConstructionType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(ConstructionType type) {
		this.type = type;
	}

	/**
	 * @return the slots
	 */
	public List<ConstructionSlotPo> getSlots() {
		return slots;
	}

	/**
	 * @param slots the slots to set
	 */
	public void setSlots(List<ConstructionSlotPo> slots) {
		this.slots = slots;
	}

}
