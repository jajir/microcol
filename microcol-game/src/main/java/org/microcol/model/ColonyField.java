package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Class represents on field outside colony. When unit is placed here than some
 * good is produces each turn.
 */
public class ColonyField {

	private Model model;
	
	private final Colony colony;
	
	private final Location location;

	private PlaceColonyField placeColonyField;

	ColonyField(final Location location, final Colony colony) {
		this.location = Preconditions.checkNotNull(location);
		this.colony = Preconditions.checkNotNull(colony);
	}

	public Location getLocation() {
		return location;
	}
	
	public Terrain getTerrain() {
		return model.getMap().getTerrainAt(colony.getLocation().add(location));
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ColonyField.class).add("location", location)
				.add("placeColonyField", placeColonyField).toString();
	}

	public boolean isEmpty() {
		return placeColonyField == null;
	}
	
	public Unit getUnit() {
		Preconditions.checkState(!isEmpty(), "There is no assigned unit");
		return placeColonyField.getUnit();
	}
	
	void setPlaceColonyField(PlaceColonyField placeColonyField) {
		this.placeColonyField = placeColonyField;
	}
}
