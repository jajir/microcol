package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Connect unit and high see between each other.
 */
public class HighSeaUnit {

	private final Unit unit;

	/**
	 * When it's <code>true</code> than ship travel from colonies to Europe.
	 * When it's <code>false</code> than ship travel from Europe to colonies.
	 */
	private final boolean isTravelToEurope;

	/**
	 * How many turns will ships before reach destination,
	 */
	private int remainigTurns;

	public HighSeaUnit(final Unit unit, final boolean isTravelToEurope, final int requiredTurns) {
		Preconditions.checkArgument(UnitType.isShip(unit.getType()));
		this.unit = Preconditions.checkNotNull(unit);
		this.isTravelToEurope = isTravelToEurope;
		this.remainigTurns = requiredTurns;
	}

	public int getRemainigTurns() {
		return remainigTurns;
	}

	public void setRemainigTurns(int remainigTurns) {
		this.remainigTurns = remainigTurns;
	}

	public Unit getUnit() {
		return unit;
	}

	public boolean isTravelToEurope() {
		return isTravelToEurope;
	}

}
