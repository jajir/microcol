package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.turnevent.TurnEventProvider;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;

/**
 * Class represents on field outside colony. When unit is placed here than some
 * good is produces each turn.
 */
public final class ColonyField {

    private final Model model;

    private final Colony colony;

    private final Direction direction;

    private PlaceColonyField placeColonyField;

    ColonyField(final Model model, final Direction direction, final Colony colony) {
        this.model = Preconditions.checkNotNull(model);
        this.direction = Preconditions.checkNotNull(direction);
        this.colony = Preconditions.checkNotNull(colony);
    }

    ColonyFieldPo save() {
        final ColonyFieldPo out = new ColonyFieldPo();
        out.setDirection(direction.getVector());
        if (getProduction().isPresent()) {
            out.setWorkerId(getUnit().get().getId());
            out.setProducedGoodsType(getProduction().get().getType());
        }
        return out;
    }

    public boolean canProduce(final GoodsType goodsType) {
        return getTerrainType().canProduce(goodsType);
    }

    public Direction getDirection() {
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
        return colony.getLocation().add(direction.getVector());
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
        if (getUnit().isPresent()) {
            toStringHelper.addValue("isEmpty");
        } else {
            toStringHelper.add("unitId", getUnit().get().getId());
        }
        return toStringHelper.toString();
    }

    public boolean isEmpty() {
        return placeColonyField == null;
    }

    /**
     * Get turn production. It's empty when no unit is assigned here.
     * 
     * @return production at this colony field per turn
     */
    public Optional<Goods> getProduction() {
        if (placeColonyField == null) {
            return Optional.empty();
        } else {
            final GoodsType producing = placeColonyField.getProducedGoodsType();
            return Optional.of(Goods.of(producing, getTerrain().canProduceAmmount(producing)));
        }
    }

    /**
     * Get unit producing some raw source. When there is no unit empty is
     * returned.
     * 
     * @return optional unit
     */
    public Optional<Unit> getUnit() {
        if (placeColonyField == null) {
            return Optional.empty();
        } else {
            return Optional.of(placeColonyField.getUnit());
        }
    }

    public int getGoodsTypeProduction(final GoodsType goodsType) {
        return getTerrain().canProduceAmmount(goodsType);
    }

    public void setProducedGoodsType(final GoodsType producedGoodsType) {
        Preconditions.checkNotNull(placeColonyField, "There in no unit placed at field");
        placeColonyField.setProducedGoodsType(producedGoodsType);
    }

    /**
     * Method should be called once per turn. It produce resources on field.
     *
     * @param colonyWarehouse
     *            required colony warehouse
     */
    public void countTurnProduction(final ColonyWarehouse colonyWarehouse) {
        Preconditions.checkNotNull(colonyWarehouse);
        if (getProduction().isPresent()) {
            final Goods production = getProduction().get();
            colonyWarehouse.addGoodsWithThrowingAway(production, thrownAwayGoods -> {
                model.addTurnEvent(TurnEventProvider.getGoodsWasThrowsAway(colony.getOwner(),
                        thrownAwayGoods, colony));
            });
        }
    }

    public void setPlaceColonyField(final PlaceColonyField placeColonyField) {
        this.placeColonyField = Preconditions.checkNotNull(placeColonyField);
        model.fireUnitMovedToColonyField(placeColonyField.getUnit());
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
