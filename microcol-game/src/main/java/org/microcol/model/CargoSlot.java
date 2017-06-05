package org.microcol.model;

import java.util.Optional;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class CargoSlot {
	private final CargoHold hold;
	private Optional<Unit> unit;

	CargoSlot(final CargoHold hold) {
		this.hold = hold;
		this.unit = Optional.empty();
	}

	public boolean isEmpty() {
		return unit == null;
	}

	public Optional<Unit> getUnit() {
		return unit;
	}

	public void store(final Unit unit) {
		Preconditions.checkNotNull(unit);
		Preconditions.checkState(hold.getOwner().getOwner().equals(unit.getOwner()), "Owners must be same (%s - %s).", hold.getOwner().getOwner(), unit.getOwner());
		// FIXME JKA check unit is already loaded
		// FIXME JKA check adjacent location
		Preconditions.checkState(this.unit == null, "Cargo slot (%s) is already loaded with some unit (%s).", this, this.unit);

		// FIXME JKA OWNER CARGO HOLD (is unit storeable)
		this.unit = Optional.of(unit);
	}

	public Unit unload(final Location location) {
		Preconditions.checkState(unit != null, "Cargo slot (%s) is empty.", this);
		// FIXME JKA check adjacent location
		// FIXME JKA run "standard" unit location checks

		final Unit unit = this.unit.get();
		// FIXME JKA OWNER CARGO HOLD
		unit.unload(location);

		this.unit = null;

		return unit;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("unit", unit)
			.toString();
	}

	void save(final JsonGenerator generator) {
		// TODO JKA Implement save/load
	}

	static CargoSlot load(final JsonParser parser) {
		return null; // TODO JKA Implement save/load
	}
}
