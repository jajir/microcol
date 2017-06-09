package org.microcol.model.event;

import org.microcol.model.CargoSlot;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class UnitStoredEvent extends ModelEvent {
	private final Unit unit;
	private final CargoSlot slot;

	public UnitStoredEvent(final Model model, final Unit unit, final CargoSlot slot) {
		super(model);

		this.unit = Preconditions.checkNotNull(unit);
		this.slot = Preconditions.checkNotNull(slot);
	}

	public Unit getUnit() {
		return unit;
	}

	public CargoSlot getSlot() {
		return slot;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("unit", unit)
			.add("slot", slot)
			.toString();
	}
}
