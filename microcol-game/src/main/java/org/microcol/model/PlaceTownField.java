package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Unit is placed at field to work outside of colony.
 */
public class PlaceTownField extends AbstractPlace {

	private final TownField townField;

	public PlaceTownField(final Unit unit, final TownField townField) {
		super(unit);
		this.townField = Preconditions.checkNotNull(townField);
	}

	@Override
	public String getName() {
		return "Construction";
	}

	private TownField getTownField() {
		return townField;
	}

}
