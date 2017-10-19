package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;

public class CargoPo {

	private List<CargoSlotPo> slots = new ArrayList<>();

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(CargoPo.class).add("slots", slots).toString();
	}

	/**
	 * @return the slots
	 */
	public List<CargoSlotPo> getSlots() {
		return slots;
	}

	/**
	 * @param slots
	 *            the slots to set
	 */
	public void setSlots(List<CargoSlotPo> slots) {
		this.slots = slots;
	}

}
