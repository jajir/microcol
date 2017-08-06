package org.microcol.model;

import java.util.Optional;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class CargoSlot {
	private final CargoHold hold;
	private Optional<Unit> cargo;

	CargoSlot(final CargoHold hold) {
		this.hold = hold;
		this.cargo = Optional.empty();
	}

	public boolean isEmpty() {
		return !cargo.isPresent();
	}

	public Optional<Unit> getUnit() {
		return cargo;
	}

	/**
	 * This method doesn's store store unti and 
	 * @param unit
	 */
	void unsafeStore(final Unit unit){
		cargo = Optional.of(unit);
		unit.storeWithoutEvent(this);
	}
	
	public void store(final Unit unit) {
		Preconditions.checkNotNull(unit);
		if (cargo.isPresent()) { // TODO JKA Temporary fix - cargo.get() is problem
			Preconditions.checkState(!cargo.isPresent(), "Cargo slot (%s) is already loaded with some unit (%s).", this, cargo.get());
		}
		Preconditions.checkState(hold.getOwner().getOwner().equals(unit.getOwner()), "Owners must be same (%s - %s).", hold.getOwner().getOwner(), unit.getOwner());

		cargo = Optional.of(unit);
		unit.store(this);
	}

	public Unit unload(final Location targetLocation) {
		Preconditions.checkNotNull(targetLocation);
		Preconditions.checkState(cargo.isPresent(), "Cargo slot (%s) is empty.", this);
		Preconditions.checkArgument(hold.getOwner().getLocation().isNeighbor(targetLocation),"Unit (%s) can't unload at location (%s), it's too far",hold.getOwner(),targetLocation);

		final Unit unit = cargo.get();
		unit.unload(targetLocation);
		cargo = Optional.empty();

		return unit;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("cargo", cargo)
			.toString();
	}

	void save(final JsonGenerator generator) {
		// TODO JKA Implement save/load
	}

	static CargoSlot load(final JsonParser parser) {
		return null; // TODO JKA Implement save/load
	}
}
