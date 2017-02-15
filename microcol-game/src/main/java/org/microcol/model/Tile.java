package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;

public class Tile {

	private final List<Unit> units = new ArrayList<Unit>();

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Tile.class).add("units", units).toString();
	}

	public List<Unit> getUnits() {
		return units;
	}

	public Unit getFirstMovableUnit() {
		if (!units.isEmpty()) {
			return units.get(0);
		}
		return null;
	}

}
