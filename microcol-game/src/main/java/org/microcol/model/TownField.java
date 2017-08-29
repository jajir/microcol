package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Class represents on field outside colony. When unit is placed here than some
 * good is produces each turn.
 */
public class TownField {

	private Model model;
	
	private final Town town;
	
	private final Location location;

	private PlaceTownField placeTownField;

	TownField(final Location location, final Town town) {
		this.location = Preconditions.checkNotNull(location);
		this.town = Preconditions.checkNotNull(town);
	}

	public Location getLocation() {
		return location;
	}
	
	public Terrain getTerrain() {
		return model.getMap().getTerrainAt(town.getLocation().add(location));
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(TownField.class).add("location", location)
				.add("placeTownField", placeTownField).toString();
	}

	public boolean isEmpty() {
		return placeTownField == null;
	}
	
	public Unit getUnit() {
		Preconditions.checkState(!isEmpty(), "There is no assigned unit");
		return placeTownField.getUnit();
	}
	
	void setPlaceTownField(PlaceTownField placeTownField) {
		this.placeTownField = placeTownField;
	}
}
