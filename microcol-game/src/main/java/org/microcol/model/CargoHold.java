package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class CargoHold {
	private final Unit owner;
	private final List<CargoSlot> slots;

	CargoHold(final Unit owner, final int capacity) {
		this.owner = owner;

		List<CargoSlot> slots = new ArrayList<>();
		for (int i = 0; i < capacity; i++) {
			slots.add(new CargoSlot(this));
		}
		this.slots = ImmutableList.copyOf(slots);
	}

	Unit getOwner() {
		return owner;
	}

	public List<CargoSlot> getSlots() {
		return slots;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("slots", slots)
			.toString();
	}

	void save(final JsonGenerator generator) {
		// FIXME JKA IMPLEMENT
	}

	static CargoHold load(final JsonParser parser) {
		return null; // FIXME JKA IMPLEMENT
	}
}
