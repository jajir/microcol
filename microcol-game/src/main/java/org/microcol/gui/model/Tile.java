package org.microcol.gui.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;

public class Tile {

	private final List<Unit> units = new ArrayList<Unit>();

	private final String name;

	private final String description;

	private final int moveCost;

	public Tile(final String name, final String description, final int moveCost) {
		this.name = name;
		this.description = description;
		this.moveCost = moveCost;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Tile.class).add("name", name).add("moveCost", moveCost).add("units", units)
				.toString();
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

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getMoveCost() {
		return moveCost;
	}

}
