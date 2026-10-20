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

    ColonyField(final Model model, final Direction direction,
            final Colony colony) {
        this.model = Preconditions.checkNotNull(model);
        this.direction = Preconditions.checkNotNull(direction);
        this.colony = Preconditions.checkNotNull(colony);
    }

    ColonyFieldPo save() {
        final ColonyFieldPo out = new ColonyFieldPo();
        out.setDirection(direction.getVector());
        if (placeColonyField != null) {
            out.setWorkerId(placeColonyField.getUnit().getId());
            out.setProducedGoodsType(placeColonyField.getProducedGoodsType());
        }
        return out;
    }

    public boolean canProduce(final GoodsType goodsType) {
        return getMap().isValid(getLocation()) && !isMissingDock()
                && getTerrain().canProduce(goodsType);
    }

    /** Whether this water square needs a completed port facility. */
    public boolean isMissingDock() {
        return getMap().isValid(getLocation()) && getTerrainType().isSea()
                && !colony.wasConstructionBuilded(ConstructionType.DOCK)
                && !colony.wasConstructionBuilded(ConstructionType.DRYDOCK)
                && !colony.wasConstructionBuilded(ConstructionType.SHIPYARD);
    }

    public boolean isWorkedByAnotherColony() {
        return isEmpty() && isValid()
                && model.isColonyFieldWorked(getLocation());
    }

    public boolean canAccept(final Unit unit, final GoodsType goodsType) {
        if (!isValid() || !colony.getColonyFields().contains(this)
                || goodsType == null || !isEmpty() || !canProduce(goodsType)
                || !model.isColonyFieldAvailable(colony.getOwner(),
                        getLocation())) {
            return false;
        }
        return colony.canAcceptWorker(unit);
    }

    private boolean canWorkerActHere(final Unit unit) {
        if (!isValid() || !colony.getColonyFields().contains(this)) {
            return false;
        }
        return colony.canWorkerActHere(unit);
    }

    boolean isWorkedBy(final Unit unit) {
        return unit != null && unit.isAtPlaceColonyField()
                && placeColonyField != null
                && placeColonyField == unit.getPlaceColonyField();
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
        final ToStringHelper toStringHelper = MoreObjects
                .toStringHelper(ColonyField.class).add("direction", direction)
                .add("colonyLocation", colony.getLocation())
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

    public boolean isNotEmpty() {
        return placeColonyField != null;
    }

    /**
     * Get turn production. It's empty when no unit is assigned here.
     *
     * @return production at this colony field per turn
     */
    public Optional<Goods> getProduction() {
        if (placeColonyField == null) {
            return Optional.empty();
        }
        return getProduction(colony.getPoliticalProduction());
    }

    /**
     * Get turn production for the political snapshot of one update.
     *
     * @param political support bonus and Tory penalty to apply
     * @return production at this colony field per turn, empty without a unit
     */
    Optional<Goods> getProduction(final PoliticalProduction political) {
        if (placeColonyField == null) {
            return Optional.empty();
        } else {
            final GoodsType producing = placeColonyField.getProducedGoodsType();
            return Optional.of(getPotentialProduction(
                    placeColonyField.getUnit(), producing, political));
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

    /**
     * Fresh read-only production of the worker. An outside worker is previewed
     * as a new resident, which changes the support fraction.
     */
    Goods getPotentialProduction(final Unit worker, final GoodsType goodsType) {
        return getPotentialProduction(worker, goodsType,
                colony.getPoliticalProductionFor(worker));
    }

    Goods getPotentialProduction(final Unit worker, final GoodsType goodsType,
            final PoliticalProduction political) {
        final Goods without = getJobProduction(worker, goodsType,
                getUnlimitedGoodsTypeProduction(goodsType, false), political);
        return without.setAmount(without.getAmount()
                + getDepositExtra(worker, goodsType, without, political));
    }

    /**
     * Extra output that the job takes from a finite deposit, after the job and
     * political modifiers and limited by the remaining units. The extra is
     * measured on the job output, so a specialist never multiplies a last
     * partial reserve.
     *
     * @param worker    worker of the square
     * @param goodsType produced goods
     * @param without   job output without the deposit bonus
     * @param political political snapshot of the update
     * @return extra units the reserve still allows, zero when none
     */
    private int getDepositExtra(final Unit worker, final GoodsType goodsType,
            final Goods without, final PoliticalProduction political) {
        final int extra = getJobProduction(worker, goodsType,
                getUnlimitedGoodsTypeProduction(goodsType, true), political)
                .getAmount() - without.getAmount();
        // Only a usable square yields, so an empty extra needs no terrain.
        return extra > 0 ? getTerrain().getLimitedDepositExtra(goodsType, extra)
                : 0;
    }

    /**
     * Goods that the current job takes from its finite deposit in one update.
     * Previews never call this; the colony takes it once from the committed
     * output. Output without a finite deposit takes nothing.
     *
     * @param political political snapshot of the update
     * @return goods taken from the reserve, empty when nothing is taken
     */
    Optional<Goods> getDepositExtraction(final PoliticalProduction political) {
        if (placeColonyField == null || !getMap().isValid(getLocation())
                || isMissingDock()) {
            return Optional.empty();
        }
        final GoodsType goodsType = placeColonyField.getProducedGoodsType();
        if (getTerrain().getResourceOverlay()
                .getInitialReserve(goodsType) == 0) {
            return Optional.empty();
        }
        final Unit worker = placeColonyField.getUnit();
        final Goods without = getJobProduction(worker, goodsType,
                getUnlimitedGoodsTypeProduction(goodsType, false), political);
        final int taken = getDepositExtra(worker, goodsType, without,
                political);
        return taken > 0 ? Optional.of(Goods.of(goodsType, taken))
                : Optional.empty();
    }

    /**
     * Output of one worker from the terrain output of this square.
     *
     * @param worker        worker of the square
     * @param goodsType     produced goods
     * @param terrainOutput output of the square before the job
     * @param political     political snapshot of the update
     * @return production of the job
     */
    private Goods getJobProduction(final Unit worker, final GoodsType goodsType,
            final int terrainOutput, final PoliticalProduction political) {
        final Goods base = Goods.of(goodsType, terrainOutput);
        final Goods worked = base.multiply(worker.getType()
                .getFieldProductionModifier(goodsType, getTerrainType()));
        final Goods production = political.apply(base, worked);
        if (goodsType == GoodsType.FUR && colony.getOwner().getCongressMembers()
                .contains(Founder.HENRY_HUDSON)) {
            // Zero yield stays zero, so doubling never creates furs.
            return production
                    .setAmount(Math.multiplyExact(production.getAmount(), 2));
        }
        return production;
    }

    public int getGoodsTypeProduction(final GoodsType goodsType) {
        return getMap().isValid(getLocation()) && !isMissingDock()
                ? getTerrain().canProduceAmmount(goodsType)
                : 0;
    }

    /**
     * Terrain output before the finite deposit limit, with or without the
     * deposit bonus.
     */
    private int getUnlimitedGoodsTypeProduction(final GoodsType goodsType,
            final boolean withDeposit) {
        return getMap().isValid(getLocation()) && !isMissingDock()
                ? getTerrain()
                        .getUnlimitedTerrainProduction(goodsType, withDeposit)
                        .getProduction()
                : 0;
    }

    public void setProducedGoodsType(final GoodsType producedGoodsType) {
        model.checkTurnPreparationComplete();
        Preconditions.checkState(placeColonyField != null,
                "There is no unit placed at field");
        setProducedGoodsType(placeColonyField.getUnit(), producedGoodsType);
    }

    public void setProducedGoodsType(final Unit expectedWorker,
            final GoodsType producedGoodsType) {
        model.checkTurnPreparationComplete();
        Preconditions.checkNotNull(expectedWorker);
        Preconditions.checkNotNull(producedGoodsType);
        Preconditions.checkArgument(canProduce(producedGoodsType),
                "Field cannot produce requested goods");
        Preconditions.checkState(
                placeColonyField != null
                        && placeColonyField.getUnit() == expectedWorker
                        && canWorkerActHere(expectedWorker)
                        && expectedWorker.isAtPlaceColonyField()
                        && expectedWorker
                                .getPlaceColonyField() == placeColonyField,
                "Worker or colony job is no longer available");
        placeColonyField.setProducedGoodsType(producedGoodsType);
    }

    /**
     * Method should be called once per turn. It produce resources on field.
     *
     * @param colonyWarehouse required colony warehouse
     */
    public void countTurnProduction(final ColonyWarehouse colonyWarehouse) {
        Preconditions.checkNotNull(colonyWarehouse);
        if (getProduction().isPresent()) {
            final Goods production = getProduction().get();
            colonyWarehouse.addGoodsWithThrowingAway(production,
                    thrownAwayGoods -> {
                        model.addTurnEvent(TurnEventProvider
                                .getGoodsWasThrowsAway(colony.getOwner(),
                                        thrownAwayGoods, colony));
                    });
        }
    }

    void setPlaceColonyField(final PlaceColonyField placeColonyField) {
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
        return model.isExists(colony) && getMap().isValid(getLocation());
    }
}
