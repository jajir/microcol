package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.CargoSlotPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class CargoSlot {

    /**
     * It's maximum number of tools that can be put into cargo slot.
     */
    public static final int MAX_CARGO_SLOT_CAPACITY = 100;

    /**
     * Cargo in which is this slot placed.
     */
    private final Cargo cargo;

    /**
     * In this cargo slot could be stored this unit.
     */
    private PlaceCargoSlot cargoUnit;

    /**
     * In this cargo slot could be stored this good.
     */
    private Goods cargoGoods;

    CargoSlot(final Cargo cargo) {
        this.cargo = Preconditions.checkNotNull(cargo);
    }

    CargoSlot(final Cargo cargo, final Goods goods) {
        this.cargo = Preconditions.checkNotNull(cargo);
        this.cargoGoods = Preconditions.checkNotNull(goods);
    }

    CargoSlotPo save() {
        final CargoSlotPo out = new CargoSlotPo();
        if (isLoadedGood()) {
            out.setAmount(cargoGoods.getAmount());
            out.setGoodsType(cargoGoods.getType());
        }
        if (isLoadedUnit()) {
            out.setUnitId(getUnit().get().getId());
        }
        return out;
    }

    public Player getOwnerPlayer() {
        return getOwnerUnit().getOwner();
    }

    public boolean isEmpty() {
        return cargoUnit == null && cargoGoods == null;
    }

    public boolean isLoadedUnit() {
        return cargoUnit != null;
    }

    public boolean isLoadedGood() {
        return cargoGoods != null;
    }

    public void empty() {
        Preconditions.checkState(!isEmpty(), "Cargo slot (%s) is already empty.", this);
        cargoUnit = null;
        cargoGoods = null;
    }

    /**
     * Get index that allows to identify this cargo slot. Index is unique in one
     * ship.
     * 
     * @return return unique cargo slot index
     */
    public int getIndex() {
        return cargo.getIndexOfSlot(this);
    }

    /**
     * Get unit where this cargo slot belongs.
     * 
     * @return return owner unit
     */
    public Unit getOwnerUnit() {
        return cargo.getOwner();
    }

    public Optional<Unit> getUnit() {
        if (cargoUnit == null) {
            return Optional.empty();
        } else {
            return Optional.of(cargoUnit.getUnit());
        }
    }

    public void removeCargo(final Goods goods) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkArgument(isLoadedGood(), "Cargo slot (%s) doesn't contains any good.",
                this);
        Preconditions.checkArgument(cargoGoods.getType().equals(goods.getType()),
                "Cargo (%s) doesn't contains same type as was transfered (%s).", this,
                goods.getType());
        Preconditions.checkArgument(cargoGoods.getAmount() >= goods.getAmount(),
                "Can't remove more amount (%s) than is stored (%s).", goods.getAmount(), this);
        cargoGoods = cargoGoods.substract(goods);
        if (cargoGoods.getAmount() == 0) {
            cargoGoods = null;
        }
    }

    public Optional<Goods> getGoods() {
        return Optional.ofNullable(cargoGoods);
    }

    /**
     * This simply put some other unit to cargo store. It should not be used for
     * unit at map.
     *
     * @param unit
     *            required unit
     */
    public void store(final Unit unit) {
        Preconditions.checkNotNull(unit);
        Preconditions.checkState(isEmpty(), "Cargo slot (%s) is already loaded.", this);
        Preconditions.checkState(getOwnerUnit().getOwner().equals(unit.getOwner()),
                "Owners must be same (%s - %s).", getOwnerUnit().getOwner(), unit.getOwner());

        final PlaceCargoSlot tmp = new PlaceCargoSlot(unit, this);
        unit.placeToCargoSlot(tmp);
        cargoUnit = tmp;
    }

    /**
     * This simply put some other unit to cargo store. It should not be used for
     * unit at map.
     *
     * @param placeCargoSlot
     *            required place describing relation to unit
     */
    public void store(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkNotNull(placeCargoSlot);
        Preconditions.checkState(isEmpty(), "Cargo slot (%s) is already loaded.", this);
        final Unit unit = placeCargoSlot.getUnit();
        Preconditions.checkState(getOwnerUnit().getOwner().equals(unit.getOwner()),
                "Owners must be same (%s - %s).", getOwnerUnit().getOwner(), unit.getOwner());

        cargoUnit = placeCargoSlot;
    }

    /**
     * Transfer goods form Europe port.
     * 
     * @param goods
     *            required good amount will
     * @return how much of goods was transferred
     */
    public Goods buyAndStoreFromEuropePort(final Goods goods) {
        Preconditions.checkNotNull(goods);
        verifyThatItCouldStored(goods);
        if (getGoods().isPresent()) {
            final Goods storedGoods = getGoods().get();
            final Goods maxGoods = storedGoods.setAmount(MAX_CARGO_SLOT_CAPACITY);
            final Goods freeGoods = maxGoods.substract(storedGoods);
            if (freeGoods.isZero()) {
                // no space to store
                return storedGoods.setAmount(0);
            }
            if (freeGoods.isGreaterOrEqualsThan(goods)) {
                // all could be stored
                getOwnerPlayer().buyGoods(goods);
                addGoods(goods);
                return goods;
            } else {
                // just part will be stored
                getOwnerPlayer().buyGoods(freeGoods);
                addGoods(freeGoods);
                return freeGoods;
            }
        } else {
            getOwnerPlayer().buyGoods(goods);
            addGoods(goods);
            return goods;
        }
    }

    void sellAndEmpty(final Goods goods) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkState(!isEmpty(), "Cargo slot (%s) is already empty.", this);
        Preconditions.checkState(getGoods().isPresent(), "Cargo slot (%s) doesn't contains goods.",
                this);
        Preconditions.checkArgument(getGoods().get().getType().equals(goods.getType()),
                "Cargo slot (%s) doesn't contains correct goods type.", this);
        Preconditions.checkArgument(getGoods().get().isGreaterOrEqualsThan(goods),
                "Attempt to sell more goods %s than is stored in %s", goods, this);

        getOwnerPlayer().sellGoods(goods);
        cargoGoods = cargoGoods.substract(goods);
        if (cargoGoods.isZero()) {
            cargoGoods = null;
        }
    }

    /**
     * Store goods from source cargo slot to this one.
     * 
     * @param transferredGoods
     *            required good amount
     * @param sourceCargoSlot
     *            required source cargo slot
     * @return really transfered goods
     */
    public Goods storeFromCargoSlot(final Goods transferredGoods, final CargoSlot sourceCargoSlot) {
        verifyThatItCouldStored(transferredGoods);
        Preconditions.checkNotNull(sourceCargoSlot, "source cargo slot is null");
        Preconditions.checkArgument(getOwnerPlayer().equals(sourceCargoSlot.getOwnerPlayer()),
                "Source cargo slot and target cargo slots doesn't belongs to same user (%s) (%s).",
                getOwnerPlayer(), sourceCargoSlot.getOwnerPlayer());
        Preconditions.checkArgument(sourceCargoSlot.getGoods().isPresent(),
                "Source cargo slot doesn't contains any good,(%s)", sourceCargoSlot);

        final Goods sourceGoods = sourceCargoSlot.getGoods().get();
        Preconditions.checkArgument(sourceGoods.getType().equals(transferredGoods.getType()),
                "Source cargo slot contains diffrent good than was transfered. Source cargo slot=(%s), Transfered=(%s)",
                sourceCargoSlot, transferredGoods);

        final Goods toTransfer = maxPossibleGoodsToMoveHere(sourceGoods, transferredGoods);
        sourceCargoSlot.removeCargo(toTransfer);
        addGoods(toTransfer);
        return toTransfer;
    }

    /**
     * Store goods warehouse to this one.
     * 
     * @param transferredGoods
     *            required good amount
     * @param colony
     *            required colony containing source warehouse
     */
    public void storeFromColonyWarehouse(final Goods transferredGoods, final Colony colony) {
        verifyThatItCouldStored(transferredGoods);
        Preconditions.checkNotNull(colony, "colony is null");
        Preconditions.checkArgument(getOwnerPlayer().equals(colony.getOwner()),
                "Source colony warehouse and target cargo slots doesn't belongs to same user (%s) (%s).",
                getOwnerPlayer(), colony.getOwner());

        final ColonyWarehouse warehouse = colony.getWarehouse();
        final Goods warehouseGoods = warehouse.getGoods(transferredGoods.getType());

        final Goods toTransfer = maxPossibleGoodsToMoveHere(warehouseGoods, transferredGoods);
        warehouse.removeGoods(toTransfer);
        addGoods(toTransfer);
    }

    /**
     * Get maximum of goods that could be stored to this cargo slot.
     * 
     * @param availableSourceGoods
     *            required maximum number of units in source, source could be
     *            Europe port, warehouse or ship cargo slot
     * @param transferredGoods
     *            required maximum transferred goods
     * @return amount of transfered goods
     */
    public Goods maxPossibleGoodsToMoveHere(final Goods availableSourceGoods,
            final Goods transferredGoods) {
        if (availableSourceGoods.getAmount() < transferredGoods.getAmount()) {
            return maxPossibleGoodsToMoveHere(availableSourceGoods);
        } else {
            return maxPossibleGoodsToMoveHere(transferredGoods);
        }
    }

    /**
     * Get info how many goods could be moved here from available goods.
     * 
     * @param transferredGoods
     *            required transferred goods
     * @return how much goods could be stored here
     */
    public Goods maxPossibleGoodsToMoveHere(final Goods transferredGoods) {
        if (getAvailableCapacity() > transferredGoods.getAmount()) {
            return transferredGoods;
        } else {
            return Goods.of(transferredGoods.getType(), getAvailableCapacity());
        }
    }

    /**
     * Validate that goods amount could be stored to this cargo slot
     * 
     * @param transferredGoods
     *            required transferred goods amount
     */
    private void verifyThatItCouldStored(final Goods transferredGoods) {
        Preconditions.checkNotNull(transferredGoods);
        if (isLoadedUnit()) {
            throw new IllegalArgumentException("Attempt to move cargo to slot occupied with unit.");
        }
        if (isLoadedGood()) {
            Preconditions.checkArgument(
                    getGoods().get().getType().equals(transferredGoods.getType()),
                    "Tranfered cargo is diffrent type this is stored in slot. Stored=(%s), transfered=(%s)",
                    getGoods().get(), cargoGoods);
        }
    }

    /**
     * Return how many of goods could be maximally stored to this cargo slot.
     * 
     * @return return available space for goods
     */
    public int getAvailableCapacity() {
        if (isLoadedUnit()) {
            return 0;
        } else {
            if (isLoadedGood()) {
                return MAX_CARGO_SLOT_CAPACITY - cargoGoods.getAmount();
            } else {
                return MAX_CARGO_SLOT_CAPACITY;
            }
        }
    }

    void addGoods(final Goods goods) {
        Preconditions.checkNotNull(goods);
        if (cargoGoods == null) {
            cargoGoods = goods;
        } else {
            cargoGoods = cargoGoods.add(goods);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("cargoUnit", cargoUnit)
                .add("cargoGoods", cargoGoods).toString();
    }

    Cargo getHold() {
        return cargo;
    }
}
