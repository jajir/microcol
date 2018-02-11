package org.microcol.gui.colony;

import org.microcol.model.Colony;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class UnitMovedToFieldEvent {

	private final Unit unit;

	private final Colony colony;

	UnitMovedToFieldEvent(final Unit unit, final Colony colony) {
		this.unit = Preconditions.checkNotNull(unit);
		this.colony = Preconditions.checkNotNull(colony);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("unit", unit).add("colony", colony).toString();
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * @return the colony
	 */
	public Colony getColony() {
		return colony;
	}

}
