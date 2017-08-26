package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Class represents on field outside colony. When unit is placed here than some
 * good is produces each turn.
 */
public class TownField {

	private final Location location;

	private PlaceTownField placeTownField;

	TownField(final Location location) {
		this.location = Preconditions.checkNotNull(location);
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(TownField.class).add("location", location)
				.add("placeTownField", placeTownField).toString();
	}
}
