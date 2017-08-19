package org.microcol.model;

import java.util.List;
import java.util.Optional;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Cargo {

	private final Unit owner;
	private final List<CargoSlot> slots;

	// TODO capacity is not required here, it's from unit type
	Cargo(final Unit owner, final int capacity) {
		this.owner = Preconditions.checkNotNull(owner);

		final ImmutableList.Builder<CargoSlot> builder = ImmutableList.builder();
		for (int i = 0; i < capacity; i++) {
			builder.add(new CargoSlot(this));
		}
		this.slots = builder.build();
	}

	public Optional<CargoSlot> getEmptyCargoSlot() {
		return slots.stream().filter(cargoSlot -> cargoSlot.isEmpty()).findAny();
	}

	Unit getOwner() {
		return owner;
	}
	
	Model getModel() {
		return owner.getModel();
	}

	public List<CargoSlot> getSlots() {
		return slots;
	}

	public CargoSlot getSlotByIndex(final int index) {
		return slots.get(index);
	}

	public int getIndexOfSlot(final CargoSlot cargoSlot) {
		return slots.indexOf(cargoSlot);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("slots", slots).toString();
	}

	void save(final JsonGenerator generator) {
		// TODO JKA Implement save/load
	}

	static Cargo load(final JsonParser parser) {
		return null; // TODO JKA Implement save/load
	}
}
