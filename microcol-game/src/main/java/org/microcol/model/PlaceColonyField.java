package org.microcol.model;

import com.google.common.base.MoreObjects;
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

	@Override
	public void destroy() {
		colonyField.setPlaceColonyField(null);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PlaceColonyField.class)
				.add("unitId", getUnit().getId())
				.add("unitType", getUnit().getType())
				.add("colonyName", colonyField.getColonyName())
				.toString();
	}

}
