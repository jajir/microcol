package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class TownSection {

	private final Location location;

	private Unit unit;

	TownSection(final Location location, final Unit unit) {
		this.location = Preconditions.checkNotNull(location);
		this.unit = unit;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(TownSection.class).add("location", location).toString();
	}
}
