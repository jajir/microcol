package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * This place say that unit in Europe port pier.
 */
public class PlaceEuropePier extends AbstractPlace {

	PlaceEuropePier(final Unit unit) {
		super(unit);
		Preconditions.checkArgument(!UnitType.isShip(unit.getType()), "Ship can't be placed to Europe port pier.");
	}

	@Override
	public String getName() {
		return "Europe pier";
	}

}
