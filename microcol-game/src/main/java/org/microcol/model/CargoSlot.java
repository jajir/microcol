package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.CargoSlotPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class CargoSlot {

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

	CargoSlot(final Cargo hold) {
		this.cargo = Preconditions.checkNotNull(hold);
	}

	CargoSlot(final Cargo hold, final GoodAmount goodAmount) {
		this.cargo = Preconditions.checkNotNull(hold);
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
	
	public void removeCargo(final GoodType goodType, final int amount){
		Preconditions.checkArgument(isLoadedGood(), "Cargo (%s) doesn't contains any good.", this);
		Preconditions.checkArgument(cargoGoods.getGoodType().equals(goodType),
				"Cargo (%s) doesn't contains same typa as was transfered (%s).", this, goodType);
		Preconditions.checkArgument(cargoGoods.getAmount() >= amount,
				"Can't transfer more amount (%s) than is in stored (%s).", amount, this);
		cargoGoods.setAmount(cargoGoods.getAmount() - amount);
		if (cargoGoods.getAmount() == 0) {
			cargoGoods = null;
		}
	}

	public Optional<GoodAmount> getGoods() {
		return Optional.of(cargoGoods);
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
	public void buyAndStore(final GoodAmount goodAmount) {
		Preconditions.checkNotNull(goodAmount);
		Preconditions.checkState(isEmpty(), "Cargo slot (%s) is already loaded.", this);
		getOwnerPlayer().buy(goodAmount);
		cargoGoods = goodAmount;
	}
	
	public void sellAndEmpty(final GoodAmount goodAmount) {
		Preconditions.checkNotNull(goodAmount);
		Preconditions.checkState(!isEmpty(), "Cargo slot (%s) is already empty.", this);
		Preconditions.checkState(getGoods().isPresent(), "Cargo slot (%s) doesn't contains goods.", this);
		Preconditions.checkState(getGoods().get().getGoodType().equals(goodAmount.getGoodType()),
				"Cargo slot (%s) doesn't contains correct goods type.", this);
		getOwnerPlayer().sell(goodAmount);
		cargoGoods.setAmount(cargoGoods.getAmount() - goodAmount.getAmount());
		if (cargoGoods.getAmount() <= 0) {
			cargoGoods = null;
		}
	}

	/**
	 * @param goodAmount
	 *            required good amount
	 * @param sourceCargoSlot
	 *            required source cargo slot
	 */
	public void storeFromCargoSlot(final GoodAmount goodAmount, final CargoSlot sourceCargoSlot) {
		Preconditions.checkNotNull(goodAmount);
		Preconditions.checkState(getOwnerPlayer().equals(sourceCargoSlot.getOwnerPlayer()),
				"Source cargo slot and target cargo slots doesn't belongs to same user (%s) (%s).", getOwnerPlayer(),
				sourceCargoSlot.getOwnerPlayer());
		if (!isEmpty()) {
			Preconditions.checkState(isLoadedGood(), "Attempt to move cargo to slot occupied with unit.");
			Preconditions.checkState(getGoods().get().getGoodType().equals(goodAmount.getGoodType()),
					"Tranfered cargo is diffrent type this is stored in slot. Stored=(%s), transfered=(%s)",
					getGoods().get(), cargoGoods);
		}
		Preconditions.checkState(sourceCargoSlot.getGoods().isPresent(),
				"Source cargo slot doesn't contains any good,(%s)", sourceCargoSlot);
		final GoodAmount sourceGoodAmount = sourceCargoSlot.getGoods().get();
		Preconditions.checkState(sourceGoodAmount.getGoodType().equals(goodAmount.getGoodType()),
				"Source cargo slot contains diffrent good than was transfered. Source cargo slot=(%s), Transfered=(%s)",
				sourceCargoSlot, goodAmount);
		if (sourceGoodAmount.getAmount() == goodAmount.getAmount()) {
			sourceCargoSlot.empty();
		} else if (sourceGoodAmount.getAmount() < goodAmount.getAmount()) {
			throw new IllegalArgumentException(String.format(
					"Transfered ammount is higher that is in source. Source cargo slot=(%s), Transfered=(%s)",
					sourceCargoSlot, goodAmount));
		} else {
			sourceGoodAmount.setAmount(sourceGoodAmount.getAmount() - goodAmount.getAmount());
		}
		cargoGoods = goodAmount;
	}
	
	//XXX store* methods should share some code
	
	public void storeFromColonyWarehouse(final GoodAmount goodAmount, final Colony colony) {
		Preconditions.checkNotNull(goodAmount);
		Preconditions.checkState(getOwnerPlayer().equals(colony.getOwner()),
				"Source colony warehouse and target cargo slots doesn't belongs to same user (%s) (%s).",
				getOwnerPlayer(), colony.getOwner());
		if (!isEmpty()) {
			Preconditions.checkState(isLoadedGood(), "Attempt to move cargo to slot occupied with unit.");
			Preconditions.checkState(getGoods().get().getGoodType().equals(goodAmount.getGoodType()),
					"Tranfered cargo is diffrent type this is stored in slot. Stored=(%s), transfered=(%s)",
					getGoods().get(), cargoGoods);
		}
		final ColonyWarehouse warehouse = colony.getColonyWarehouse();
		final Integer sourceAmount = warehouse.getGoodAmmount(goodAmount.getGoodType());
		if (sourceAmount < goodAmount.getAmount()) {
			throw new IllegalArgumentException(String.format(
					"Transfered ammount is higher that is in source. Source warehouse=(%s), Transfered=(%s)",
					sourceAmount, goodAmount));
		} else {
			warehouse.removeFromWarehouse(goodAmount.getGoodType(), goodAmount.getAmount());
		}
		cargoGoods = goodAmount;
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
