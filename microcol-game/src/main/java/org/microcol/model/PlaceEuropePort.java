package org.microcol.model;

/**
 * Class represents state when unit is in port.
 */
public class PlacePort extends AbstractPlace {

	PlacePort(final Unit unit) {
		super(unit);
	}

	@Override
	public String getName() {
		return "Port";
	}

}
