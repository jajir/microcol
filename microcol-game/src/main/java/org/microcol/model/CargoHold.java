package org.microcol.model;

import java.util.List;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public final class CargoHold {
	private final Unit owner;
	private final List<CargoSlot> slots;

	CargoHold(final Unit owner, final int capacity) {
		this.owner = owner;

		final ImmutableList.Builder<CargoSlot> builder = ImmutableList.builder();
		for (int i = 0; i < capacity; i++) {
			builder.add(new CargoSlot(this));
		}
		this.slots = builder.build();
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
		// TODO JKA Implement save/load
	}

	static CargoHold load(final JsonParser parser) {
		return null; // TODO JKA Implement save/load
	}
}
