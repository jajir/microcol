package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Unit is placed in some building.
 */
public class PlaceConstruction extends AbstractPlace {

	private final Construction construction;

	public PlaceConstruction(final Unit unit, final Construction construction) {
		super(unit);
		this.construction = Preconditions.checkNotNull(construction);
	}

	@Override
	public String getName() {
		return "Construction";
	}

	public Construction getConstruction() {
		return construction;
	}
}
