package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.CargoSlotPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class CargoSlot {

	/**
	 * It's maximun number of tools that can be put into cargo slot.
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
	private GoodAmount cargoGoods;

	CargoSlot(final Cargo cargo) {
		this.cargo = Preconditions.checkNotNull(cargo);
	}

	CargoSlot(final Cargo cargo, final GoodAmount goodAmount) {
		this.cargo = Preconditions.checkNotNull(cargo);
		this.cargoGoods = Preconditions.checkNotNull(goodAmount);
	}

	CargoSlotPo save() {
		final CargoSlotPo out = new CargoSlotPo();
		if (isLoadedGood()) {
			out.setAmount(cargoGoods.getAmount());
			out.setGoodType(cargoGoods.getGoodType());
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

	public void removeCargo(final GoodType goodType, final int amount) {
		Preconditions.checkArgument(isLoadedGood(), "Cargo (%s) doesn't contains any good.", this);
		Preconditions.checkArgument(cargoGoods.getGoodType().equals(goodType),
				"Cargo (%s) doesn't contains same typa as was transfered (%s).", this, goodType);
		Preconditions.checkArgument(cargoGoods.getAmount() >= amount,
				"Can't transfer more amount (%s) than is in stored (%s).", amount, this);
		cargoGoods = cargoGoods.substract(amount);
		if (cargoGoods.getAmount() == 0) {
			cargoGoods = null;
		}
	}

	public Optional<GoodAmount> getGoods() {
		return Optional.ofNullable(cargoGoods);
	}

	/**
	 * This method doesn's store store unit and
	 * 
	 * @param unit
	 */
	void unsafeStore(final PlaceCargoSlot unit) {
		cargoUnit = unit;
	}

	public void store(final Unit unit) {
		Preconditions.checkNotNull(unit);
		Preconditions.checkState(isEmpty(), "Cargo slot (%s) is already loaded.", this);
		Preconditions.checkState(getOwnerUnit().getOwner().equals(unit.getOwner()), "Owners must be same (%s - %s).",
				getOwnerUnit().getOwner(), unit.getOwner());
		cargoUnit = new PlaceCargoSlot(unit, this);
		unit.placeToCargoSlot(cargoUnit);
	}

	/**
	 * 
	 * @param goodAmount
	 *            required good amount will
	 */
	public void storeFromEuropePort(final GoodAmount goodAmount) {
		Preconditions.checkNotNull(goodAmount);
		verifyThatItCouldStored(goodAmount);
		getOwnerPlayer().buy(goodAmount);
		addGoodsAmount(goodAmount.getGoodType(), goodAmount.getAmount());
	}

	public void sellAndEmpty(final GoodAmount goodAmount) {
		Preconditions.checkNotNull(goodAmount);
		Preconditions.checkState(!isEmpty(), "Cargo slot (%s) is already empty.", this);
		Preconditions.checkState(getGoods().isPresent(), "Cargo slot (%s) doesn't contains goods.", this);
		Preconditions.checkState(getGoods().get().getGoodType().equals(goodAmount.getGoodType()),
				"Cargo slot (%s) doesn't contains correct goods type.", this);
		getOwnerPlayer().sell(goodAmount);
		cargoGoods = cargoGoods.substract(goodAmount.getAmount());
		if (cargoGoods.isZero()) {
			cargoGoods = null;
		}
	}

	/**
	 * Store goods from source cargo slot to this one.
	 * 
	 * @param transferredGoodsAmount
	 *            required good amount
	 * @param sourceCargoSlot
	 *            required source cargo slot
	 */
	public void storeFromCargoSlot(final GoodAmount transferredGoodsAmount, final CargoSlot sourceCargoSlot) {
		verifyThatItCouldStored(transferredGoodsAmount);
		Preconditions.checkNotNull(sourceCargoSlot, "source cargo slot is null");
		Preconditions.checkArgument(getOwnerPlayer().equals(sourceCargoSlot.getOwnerPlayer()),
				"Source cargo slot and target cargo slots doesn't belongs to same user (%s) (%s).", getOwnerPlayer(),
				sourceCargoSlot.getOwnerPlayer());
		Preconditions.checkArgument(sourceCargoSlot.getGoods().isPresent(),
				"Source cargo slot doesn't contains any good,(%s)", sourceCargoSlot);
		
		final GoodAmount sourceGoodAmount = sourceCargoSlot.getGoods().get();
		Preconditions.checkArgument(sourceGoodAmount.getGoodType().equals(transferredGoodsAmount.getGoodType()),
				"Source cargo slot contains diffrent good than was transfered. Source cargo slot=(%s), Transfered=(%s)",
				sourceCargoSlot, transferredGoodsAmount);
		final Integer sourceAmount = sourceCargoSlot.getGoods().get().getAmount();
		
		final int toTransfer = maxPossibleGoodsToMoveHere(sourceAmount, transferredGoodsAmount.getAmount());
		sourceCargoSlot.removeGoodsAmount(toTransfer);
		addGoodsAmount(transferredGoodsAmount.getGoodType(), toTransfer);
	}
	
	/**
	 * Store goods warehouse to this one.
	 * 
	 * @param transferredGoodsAmount
	 *            required good amount
	 * @param colony
	 *            required colony containing source warehouse
	 */
	public void storeFromColonyWarehouse(final GoodAmount transferredGoodsAmount, final Colony colony) {
		verifyThatItCouldStored(transferredGoodsAmount);
		Preconditions.checkNotNull(colony, "colony is null");
		Preconditions.checkArgument(getOwnerPlayer().equals(colony.getOwner()),
				"Source colony warehouse and target cargo slots doesn't belongs to same user (%s) (%s).",
				getOwnerPlayer(), colony.getOwner());
		
		final ColonyWarehouse warehouse = colony.getColonyWarehouse();
		final Integer warehouseAmount = warehouse.getGoodAmmount(transferredGoodsAmount.getGoodType());
		
		final int toTransfer =  maxPossibleGoodsToMoveHere(warehouseAmount, transferredGoodsAmount.getAmount());
		warehouse.removeFromWarehouse(transferredGoodsAmount.getGoodType(), toTransfer);
		addGoodsAmount(transferredGoodsAmount.getGoodType(), toTransfer);
	}

	/**
	 * Get maximum of goods that could be stored to this cargo slot.
	 * 
	 * @param maxSourceGoods
	 *            required maximum number of units in source, source could be
	 *            Europe port, warehouse or ship cargo slot
	 * @param howMuchIsTransferred
	 *            required maximum transferred goods
	 * @return amount of transfered goods
	 */
	public int maxPossibleGoodsToMoveHere(int maxSourceGoods, int howMuchIsTransferred) {
		return Math.min(maxSourceGoods, Math.min(howMuchIsTransferred, getAvailableSpace()));
	}
	
	/**
	 * Validate that goods amount could be stored to this cargo slot
	 * 
	 * @param transferredGoodsAmount
	 *            required transferred goods amount
	 */
	private void verifyThatItCouldStored(final GoodAmount transferredGoodsAmount) {
		Preconditions.checkNotNull(transferredGoodsAmount);
		if (isLoadedUnit()) {
			throw new IllegalArgumentException("Attempt to move cargo to slot occupied with unit.");
		}
		if (isLoadedGood()) {
			Preconditions.checkArgument(getGoods().get().getGoodType().equals(transferredGoodsAmount.getGoodType()),
					"Tranfered cargo is diffrent type this is stored in slot. Stored=(%s), transfered=(%s)",
					getGoods().get(), cargoGoods);
		}
	}

	/**
	 * Return how many of goods could be maximally stored to this cargo slot.
	 * 
	 * @return return available space for goods
	 */
	public int getAvailableSpace() {
		if (cargoGoods == null) {
			return MAX_CARGO_SLOT_CAPACITY;
		} else {
			return MAX_CARGO_SLOT_CAPACITY - cargoGoods.getAmount();
		}
	}
	
	private void addGoodsAmount(final GoodType goodType, final int amount) {
		final GoodAmount tmp = new GoodAmount(goodType, amount);
		if (cargoGoods == null) {
			cargoGoods = tmp;
		} else {
			cargoGoods = cargoGoods.add(tmp);
		}
	}

	protected void removeGoodsAmount(final int amount){
		Preconditions.checkArgument(isLoadedGood());
		cargoGoods = cargoGoods.substract(amount);
		if (cargoGoods.isZero()) {
			cargoGoods = null;
		}
	}
	
	public Unit unload(final Location targetLocation) {
		Preconditions.checkNotNull(targetLocation);
		Preconditions.checkState(!isEmpty(), "Cargo slot (%s) is empty.", this);
		Preconditions.checkArgument(cargo.getOwner().getLocation().isNeighbor(targetLocation),
				"Unit (%s) can't unload at location (%s), it's too far", cargo.getOwner(), targetLocation);

		final Unit unit = cargoUnit.getUnit();
		unit.unload(targetLocation);
		cargoUnit = null;

		return unit;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("cargo", cargoUnit).add("goods", cargoGoods).toString();
	}

	Cargo getHold() {
		return cargo;
	}
}
