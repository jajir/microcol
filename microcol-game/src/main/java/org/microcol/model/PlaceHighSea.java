package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Connect unit and high see between each other.
 * 
 */
public class PlaceHighSea extends AbstractPlace {

	/**
	 * When it's <code>true</code> than ship travel from colonies to Europe.
	 * When it's <code>false</code> than ship travel from Europe to colonies.
	 */
	private final boolean isTravelToEurope;

	/**
	 * How many turns will ships before reach destination,
	 */
	private int remainigTurns;

	public PlaceHighSea(final Unit unit, final boolean isTravelToEurope, final int requiredTurns) {
		super(unit);
		Preconditions.checkArgument(UnitType.isShip(unit.getType()), "Only ships could be placed to high sea.");
		this.isTravelToEurope = isTravelToEurope;
		this.remainigTurns = requiredTurns;
	}

	public int getRemainigTurns() {
		return remainigTurns;
	}

	public void setRemainigTurns(int remainigTurns) {
		this.remainigTurns = remainigTurns;
	}

	public boolean isTravelToEurope() {
		return isTravelToEurope;
	}

	@Override
	public String getName() {
		if (isTravelToEurope) {
			return "Travel to Europe";
		} else {
			return "Travel to Colonies";
		}
	}

}
