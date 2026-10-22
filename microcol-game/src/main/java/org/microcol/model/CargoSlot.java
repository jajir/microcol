package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.unit.Ship;

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
     * Place of a unit that occupies this hold as one of the holds it reserves,
     * but not as its first hold (R-0134). Such a hold has no unit of its own.
     */
    private PlaceCargoSlot reservation;

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
        Preconditions.checkArgument(
                goods.getAmount() <= MAX_CARGO_SLOT_CAPACITY,
                "Cargo slot can't contain more than %s goods, but was %s.",
                MAX_CARGO_SLOT_CAPACITY, goods.getAmount());
    }

    CargoSlotPo save() {
        final CargoSlotPo out = new CargoSlotPo();
        if (isLoadedGood()) {
            out.setAmount(cargoGoods.getAmount());
            out.setGoodsType(cargoGoods.getType());
        }
        if (isLoadedUnit()) {
            out.setUnitId(getUnit().get().getId());
        } else if (isReservedHold()) {
            out.setUnitId(reservation.getUnit().getId());
        }
        return out;
    }

    public Player getOwnerPlayer() {
        return getOwnerUnit().getOwner();
    }

    public boolean isEmpty() {
        return cargoUnit == null && cargoGoods == null && reservation == null;
    }

    /**
     * @return whether this hold is the first hold of a unit stored here; a
     *         multi-hold unit is reachable only through this hold
     */
    public boolean isLoadedUnit() {
        return cargoUnit != null;
    }

    /**
     * @return whether this hold is held by a unit that occupies another hold
     *         first; it is occupied, but offers no unit of its own
     */
    public boolean isReservedHold() {
        return reservation != null;
    }

    /**
     * @return the unit that reserves this hold without occupying it first, or
     *         empty when this hold is not reserved
     */
    public Optional<Unit> getReservedUnit() {
        return reservation == null ? Optional.empty()
                : Optional.of(reservation.getUnit());
    }

    public boolean isLoadedGood() {
        return cargoGoods != null;
    }

    /**
     * Release the unit or goods of this hold. A first hold of a multi-hold unit
     * releases every hold that the unit reserved, exactly once.
     */
    public void empty() {
        Preconditions.checkState(!isEmpty(),
                "Cargo slot (%s) is already empty.", this);
        Preconditions.checkState(!isReservedHold(),
                "Reserved cargo slot (%s) is released by its unit.", this);
        if (cargoUnit != null) {
            cargo.getSlots().forEach(slot -> {
                if (slot.reservation == cargoUnit) {
                    slot.reservation = null;
                }
            });
        }
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
        requireOperationalCarrier();
        Preconditions.checkNotNull(goods);
        Preconditions.checkArgument(isLoadedGood(),
                "Cargo slot (%s) doesn't contains any good.", this);
        Preconditions.checkArgument(
                cargoGoods.getType().equals(goods.getType()),
                "Cargo (%s) doesn't contains same type as was transfered (%s).",
                this, goods.getType());
        Preconditions.checkArgument(cargoGoods.getAmount() >= goods.getAmount(),
                "Can't remove more amount (%s) than is stored (%s).",
                goods.getAmount(), this);
        cargoGoods = cargoGoods.substract(goods);
        if (cargoGoods.getAmount() == 0) {
            cargoGoods = null;
        }
    }

    public Optional<Goods> getGoods() {
        return Optional.ofNullable(cargoGoods);
    }

    /**
     * Board a passenger through the shared gameplay transfer validation.
     *
     * @param unit required unit
     */
    public void store(final Unit unit) {
        requireOperationalCarrier();
        Preconditions.checkNotNull(unit);
        Preconditions.checkState(isEmpty(),
                "Cargo slot (%s) is already loaded.", this);
        Preconditions.checkState(
                getOwnerUnit().getOwner().equals(unit.getOwner()),
                "Owners must be same (%s - %s).", getOwnerUnit().getOwner(),
                unit.getOwner());

        final PlaceCargoSlot tmp = new PlaceCargoSlot(unit, this);
        unit.placeToCargoSlot(tmp);
    }

    /**
     * This simply put some other unit to cargo store. It should not be used for
     * unit at map. Only storable passengers can be placed, and only into a ship
     * hold; ships and wagons are carriers and never passengers. This structural
     * check is independent of turn state, so restoration and royal soldier
     * creation can use this overload.
     *
     * @param placeCargoSlot required place describing relation to unit
     */
    public void store(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkNotNull(placeCargoSlot);
        Preconditions.checkState(isEmpty(),
                "Cargo slot (%s) is already loaded.", this);
        Preconditions.checkArgument(placeCargoSlot.getCargoSlot() == this,
                "Place cargo slot (%s) describes different cargo slot than (%s).",
                placeCargoSlot, this);
        final Unit unit = placeCargoSlot.getUnit();
        Preconditions.checkState(unit.isStorable(),
                "Unit (%s) is carrier and cannot be passenger of cargo slot (%s).",
                unit, this);
        Preconditions.checkState(getOwnerUnit() instanceof Ship,
                "Passenger (%s) can be stored only in ship hold, not in (%s).",
                unit, getOwnerUnit());
        Preconditions.checkState(
                getOwnerUnit().getOwner().equals(unit.getOwner()),
                "Owners must be same (%s - %s).", getOwnerUnit().getOwner(),
                unit.getOwner());

        cargoUnit = placeCargoSlot;
    }

    /**
     * Reserve this empty hold for a unit whose first hold is another hold of
     * the same ship (R-0134). The unit is still one object: this hold only
     * points to its place, so it is released together with the first hold.
     *
     * @param placeCargoSlot required place of the unit, stored in another hold
     */
    void reserve(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkNotNull(placeCargoSlot);
        Preconditions.checkState(isEmpty(),
                "Cargo slot (%s) is already loaded.", this);
        Preconditions.checkState(getOwnerUnit() instanceof Ship,
                "Reserved hold (%s) must be a ship hold.", this);
        Preconditions.checkArgument(
                placeCargoSlot.getCargoSlot() != this && placeCargoSlot
                        .getCargoSlot().getOwnerUnit() == getOwnerUnit(),
                "Reserved hold (%s) must belong to the same ship as the unit's first hold.",
                this);
        reservation = placeCargoSlot;
    }

    /**
     * Transfer goods form Europe port.
     *
     * @param goods required good amount will
     * @return how much of goods was transferred
     */
    public Goods buyAndStoreFromEuropePort(final Goods goods) {
        return storeFromEuropePort(goods, null);
    }

    /**
     * Buy at the quote the player confirmed; a changed price buys nothing.
     *
     * @param goods   required goods, clamped to the free space before charging
     * @param offered required purchase quote shown before confirmation
     * @return how much of goods was transferred
     */
    public Goods buyAndStoreFromEuropePort(final Goods goods,
            final EuropeTradeQuote offered) {
        return storeFromEuropePort(goods,
                Preconditions.checkNotNull(offered, "Offered quote is null"));
    }

    /** @param offered confirmed quote, or {@code null} for the live price */
    private Goods storeFromEuropePort(final Goods goods,
            final EuropeTradeQuote offered) {
        requireOperationalCarrier();
        Preconditions.checkNotNull(goods);
        verifyThatItCouldStored(goods);
        // Clamp to free space before charging, so admission can't fail after
        // gold was spent.
        final Goods toBuy = maxPossibleGoodsToMoveHere(goods);
        if (toBuy.isZero()) {
            // nothing requested or no space to store
            return toBuy;
        }
        if (offered == null) {
            getOwnerPlayer().buyGoods(toBuy);
        } else {
            getOwnerPlayer().buyGoods(toBuy, offered);
        }
        addGoods(toBuy);
        return toBuy;
    }

    void sellAndEmpty(final Goods goods) {
        sellAndEmpty(goods, null);
    }

    /** @param offered confirmed quote, or {@code null} for the live price */
    void sellAndEmpty(final Goods goods, final EuropeTradeQuote offered) {
        requireOperationalCarrier();
        Preconditions.checkNotNull(goods);
        Preconditions.checkState(!isEmpty(),
                "Cargo slot (%s) is already empty.", this);
        Preconditions.checkState(getGoods().isPresent(),
                "Cargo slot (%s) doesn't contains goods.", this);
        Preconditions.checkArgument(
                getGoods().get().getType().equals(goods.getType()),
                "Cargo slot (%s) doesn't contains correct goods type.", this);
        Preconditions.checkArgument(
                getGoods().get().isGreaterOrEqualsThan(goods),
                "Attempt to sell more goods %s than is stored in %s", goods,
                this);

        if (offered == null) {
            getOwnerPlayer().sellGoods(goods);
        } else {
            getOwnerPlayer().sellGoods(goods, offered);
        }
        cargoGoods = cargoGoods.substract(goods);
        if (cargoGoods.isZero()) {
            cargoGoods = null;
        }
    }

    /**
     * Store goods from source cargo slot to this one.
     *
     * @param transferredGoods required good amount
     * @param sourceCargoSlot  required source cargo slot
     * @return really transfered goods
     */
    public Goods storeFromCargoSlot(final Goods transferredGoods,
            final CargoSlot sourceCargoSlot) {
        requireOperationalCarrier();
        Preconditions.checkNotNull(sourceCargoSlot);
        sourceCargoSlot.requireOperationalCarrier();
        verifyThatItCouldStored(transferredGoods);
        getOwnerPlayer().checkTurnPreparationComplete();
        Preconditions.checkNotNull(sourceCargoSlot,
                "source cargo slot is null");
        Preconditions.checkArgument(
                getOwnerPlayer().equals(sourceCargoSlot.getOwnerPlayer()),
                "Source cargo slot and target cargo slots doesn't belongs to same user (%s) (%s).",
                getOwnerPlayer(), sourceCargoSlot.getOwnerPlayer());
        Preconditions.checkArgument(sourceCargoSlot.getGoods().isPresent(),
                "Source cargo slot doesn't contains any good,(%s)",
                sourceCargoSlot);

        final Goods sourceGoods = sourceCargoSlot.getGoods().get();
        Preconditions.checkArgument(
                sourceGoods.getType().equals(transferredGoods.getType()),
                "Source cargo slot contains diffrent good than was transfered. Source cargo slot=(%s), Transfered=(%s)",
                sourceCargoSlot, transferredGoods);

        final Goods toTransfer = maxPossibleGoodsToMoveHere(sourceGoods,
                transferredGoods);
        if (toTransfer.isZero()) {
            return toTransfer;
        }
        sourceCargoSlot.removeCargo(toTransfer);
        addGoods(toTransfer);
        return toTransfer;
    }

    /**
     * Store goods warehouse to this one.
     *
     * @param transferredGoods required good amount
     * @param colony           required colony containing source warehouse
     */
    public void storeFromColonyWarehouse(final Goods transferredGoods,
            final Colony colony) {
        requireOperationalCarrier();
        verifyThatItCouldStored(transferredGoods);
        getOwnerPlayer().checkTurnPreparationComplete();
        Preconditions.checkNotNull(colony, "colony is null");
        Preconditions.checkArgument(getOwnerPlayer().equals(colony.getOwner()),
                "Source colony warehouse and target cargo slots doesn't belongs to same user (%s) (%s).",
                getOwnerPlayer(), colony.getOwner());

        final ColonyWarehouse warehouse = colony.getWarehouse();
        final Goods warehouseGoods = warehouse
                .getGoods(transferredGoods.getType());

        final Goods toTransfer = maxPossibleGoodsToMoveHere(warehouseGoods,
                transferredGoods);
        if (toTransfer.isZero()) {
            return;
        }
        warehouse.removeGoods(toTransfer);
        addGoods(toTransfer);
    }

    /**
     * Get maximum of goods that could be stored to this cargo slot.
     *
     * @param availableSourceGoods required maximum number of units in source,
     *                             source could be Europe port, warehouse or
     *                             ship cargo slot
     * @param transferredGoods     required maximum transferred goods
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
     * @param transferredGoods required transferred goods
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
     * @param transferredGoods required transferred goods amount
     */
    private void verifyThatItCouldStored(final Goods transferredGoods) {
        requireOperationalCarrier();
        Preconditions.checkNotNull(transferredGoods);
        if (isLoadedUnit() || isReservedHold()) {
            throw new IllegalArgumentException(
                    "Attempt to move cargo to slot occupied with unit.");
        }
        if (isLoadedGood()) {
            Preconditions.checkArgument(
                    getGoods().get().getType()
                            .equals(transferredGoods.getType()),
                    "Tranfered cargo is diffrent type this is stored in slot. Stored=(%s), transfered=(%s)",
                    getGoods().get(), cargoGoods);
        }
    }

    private void requireOperationalCarrier() {
        final Unit carrier = getOwnerUnit();
        Preconditions.checkState(
                !(carrier instanceof Ship) || !((Ship) carrier).isDamaged(),
                "Damaged ship cargo is inaccessible");
    }

    /**
     * Return how many of goods could be maximally stored to this cargo slot.
     *
     * @return return available space for goods
     */
    public int getAvailableCapacity() {
        if (isLoadedUnit() || isReservedHold()) {
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
        verifyThatItCouldStored(goods);
        Preconditions.checkArgument(goods.getAmount() <= getAvailableCapacity(),
                "Can't store %s goods into %s, free capacity is %s.",
                goods.getAmount(), this, getAvailableCapacity());
        if (goods.isZero()) {
            // Never create an occupied hold with zero goods.
            return;
        }
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
