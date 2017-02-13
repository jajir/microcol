package model;

import java.util.ArrayList;
import java.util.List;

public class Tile {

	private final List<Unit> units = new ArrayList<Unit>();

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
