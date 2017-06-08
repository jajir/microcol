package org.microcol.model.event;

import org.microcol.model.CargoSlot;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class UnitStoredEvent extends ModelEvent {
	private final CargoSlot slot;
	private final Unit unit;

	public UnitStoredEvent(final Model model, final CargoSlot slot, final Unit unit) {
		super(model);

		this.slot = Preconditions.checkNotNull(slot);
		this.unit = Preconditions.checkNotNull(unit);
	}

	public CargoSlot getSlot() {
		return slot;
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("slot", slot)
			.add("unit", unit)
			.toString();
	}
}
