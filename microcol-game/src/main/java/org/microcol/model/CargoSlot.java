package org.microcol.model;

import java.util.Optional;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

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
		return MoreObjects.toStringHelper(this).add("cargo", cargoUnit).toString();
	}

	void save(final JsonGenerator generator) {
		// TODO JKA Implement save/load
	}

	static CargoSlot load(final JsonParser parser) {
		return null; // TODO JKA Implement save/load
	}

	Cargo getHold() {
		return cargo;
	}
}
