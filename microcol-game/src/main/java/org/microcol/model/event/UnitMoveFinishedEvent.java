package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class UnitMoveFinishedEvent extends ModelEvent {

	private final Unit unit;

	public UnitMoveFinishedEvent(final Model model, final Unit unit) {
		super(model);

		this.unit = Preconditions.checkNotNull(unit);
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("unit", unit).toString();
	}

}
