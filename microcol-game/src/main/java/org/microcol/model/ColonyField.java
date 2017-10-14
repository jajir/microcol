package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * Class represents on field outside colony. When unit is placed here than some
 * good is produces each turn.
 */
public class ColonyField {

	private Model model;
	
	private final Colony colony;
	
	private final Location direction;

	private PlaceColonyField placeColonyField;

	ColonyField(final Location location, final Colony colony) {
		this.direction = Preconditions.checkNotNull(location);
		this.colony = Preconditions.checkNotNull(colony);
		Preconditions.checkArgument(location.isDirection(),
				"Field location (%s) is not a valid direction", location);
	}
	
	public boolean canProduce(final GoodType goodType){
		return getTerrainType().canProduce(goodType);
	}

	public Location getDirection() {
		return direction;
	}
	
	public TerrainType getTerrainType() {
		return getMap().getTerrainTypeAt(colony.getLocation().add(direction));
	}
	
	public Terrain getTerrain() {
		return getMap().getTerrainAt(colony.getLocation().add(direction));
	}
	
	private WorldMap getMap(){
		return model.getMap();
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}
	
	public String getColonyName(){
		return colony.getName();
	}

	@Override
	public String toString() {
		final ToStringHelper toStringHelper = MoreObjects.toStringHelper(ColonyField.class)
				.add("direction", direction)
				.add("colonyLocation", colony.getLocation())
				.add("colonyName", getColonyName());
		if (isEmpty()) {
			toStringHelper.addValue("isEmpty");
		} else {
			toStringHelper.add("unitId", getUnit().getId());
		}
		return toStringHelper.toString();
	}

	public boolean isEmpty() {
		return placeColonyField == null;
	}
	
	public Unit getUnit() {
		Preconditions.checkState(!isEmpty(), "There is no assigned unit");
		return placeColonyField.getUnit();
	}
	
	public GoodType getProducedGoodType() {
		return placeColonyField == null ? null : placeColonyField.getProducedGoodType();
	}
	
	public int getProducedGoodsAmmount() {
		Preconditions.checkNotNull(placeColonyField, "There in no unit placed at field");
		final GoodType producing = placeColonyField.getProducedGoodType();
		return getTerrain().canProduceAmmount(producing);
	}
	
	public void setProducedGoodType(final GoodType producedGoodType) {
		Preconditions.checkNotNull(placeColonyField, "There in no unit placed at field");
		placeColonyField.setProducedGoodType(producedGoodType);
	}
	
	/**
	 * Method should be called once per turn. It produce resources on field.
	 *
	 * @param colonyWarehouse
	 *            required colony warehouse
	 */
	public void produce(final ColonyWarehouse colonyWarehouse) {
		if (isEmpty()) {
			return;
		}
		colonyWarehouse.addToWarehouse(getProducedGoodType(), getProducedGoodsAmmount());
	}
	
	public void setPlaceColonyField(PlaceColonyField placeColonyField) {
		this.placeColonyField = placeColonyField;
	}
}
