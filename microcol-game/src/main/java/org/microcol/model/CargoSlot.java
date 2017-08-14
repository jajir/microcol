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
	private GoodAmmount cargoGoods;

	CargoSlot(final Cargo hold) {
		this.cargo = Preconditions.checkNotNull(hold);
	}

	public boolean isEmpty() {
		return cargoUnit == null && cargoGoods == null;
	}

	public void empty() {
		Preconditions.checkState(!isEmpty(), "Cargo slot (%s) is already empty.", this);
		cargoUnit = null;
		cargoGoods = null;
	}

	public Optional<Unit> getUnit() {
		if(cargoUnit==null){
			return Optional.empty();
		}else{
			return Optional.of(cargoUnit.getUnit());
		}
	}

	public Optional<GoodAmmount> getGoods() {
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
		Preconditions.checkState(cargo.getOwner().getOwner().equals(unit.getOwner()), "Owners must be same (%s - %s).",
				cargo.getOwner().getOwner(), unit.getOwner());

		cargoUnit = new PlaceCargoSlot(unit, this);
		unit.store(this);
	}

	public void store(final GoodAmmount goodAmmount) {
		Preconditions.checkNotNull(goodAmmount);
		Preconditions.checkState(isEmpty(), "Cargo slot (%s) is already loaded.", this);

		cargoGoods = goodAmmount;
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
