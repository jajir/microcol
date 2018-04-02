package org.microcol.model;

import org.microcol.model.store.ColonyFieldPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * Class represents on field outside colony. When unit is placed here than some
 * good is produces each turn.
 */
public class ColonyField {

    private final Model model;

    private final Colony colony;

    private final Location direction;

    private PlaceColonyField placeColonyField;

    ColonyField(final Model model, final Location location, final Colony colony) {
        this.model = Preconditions.checkNotNull(model);
        this.direction = Preconditions.checkNotNull(location);
        this.colony = Preconditions.checkNotNull(colony);
        Preconditions.checkArgument(location.isDirection(),
                "Field location (%s) is not a valid direction", location);
    }

    ColonyFieldPo save() {
        final ColonyFieldPo out = new ColonyFieldPo();
        out.setDirection(direction);
        if (!isEmpty()) {
            out.setWorkerId(getUnit().getId());
            out.setProducedGoodType(getProducedGoodType());
        }
        return out;
    }

    public boolean canProduce(final GoodType goodType) {
        return getTerrainType().canProduce(goodType);
    }

    public Location getDirection() {
        return direction;
    }

    Colony getColony() {
        return colony;
    }

    public TerrainType getTerrainType() {
        return getMap().getTerrainTypeAt(getLocation());
    }

    public Terrain getTerrain() {
        return getMap().getTerrainAt(getLocation());
    }

    /**
     * World map colony field location
     * 
     * @return location where is colony field at map
     */
    public Location getLocation() {
        return colony.getLocation().add(direction);
    }

    private WorldMap getMap() {
        return model.getMap();
    }

    public String getColonyName() {
        return colony.getName();
    }

    @Override
    public String toString() {
        final ToStringHelper toStringHelper = MoreObjects.toStringHelper(ColonyField.class)
                .add("direction", direction).add("colonyLocation", colony.getLocation())
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

    public int isPossibleToProduceOfGooodsType(final GoodType goodType) {
        return getTerrain().canProduceAmmount(goodType);
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

    public void setPlaceColonyField(final PlaceColonyField placeColonyField) {
        this.placeColonyField = Preconditions.checkNotNull(placeColonyField);
    }

    void empty(final boolean validate) {
        placeColonyField = null;
        if (validate) {
            colony.verifyNumberOfUnitsOptionallyDestroyColony();
        }
    }

    public boolean isValid() {
        return model.isExists(colony);
    }
}
