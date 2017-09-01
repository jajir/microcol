package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Unit is placed at field to work outside of colony.
 */
public class PlaceColonyField extends AbstractPlace {

	private final ColonyField colonyField;

	public PlaceColonyField(final Unit unit, final ColonyField colonyField) {
		super(unit);
		this.colonyField = Preconditions.checkNotNull(colonyField);
	}

	@Override
	public String getName() {
		return "Construction";
	}

	private ColonyField getColonyField() {
		return colonyField;
	}

}
