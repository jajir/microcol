package org.microcol.model;

import java.util.List;
import java.util.Optional;

import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Cargo {

	//TODO try to remove relation to owner.
	private final Unit owner;
	private final List<CargoSlot> slots;

	// TODO capacity is not required here, it's from unit type
	Cargo(final Unit owner, final int capacity, final CargoPo cargoPo) {
		this.owner = Preconditions.checkNotNull(owner);

		final ImmutableList.Builder<CargoSlot> builder = ImmutableList.builder();
		for (int i = 0; i < capacity; i++) {
			final CargoSlotPo cargoSlotPo = cargoPo.getSlotAt(i);
			if (cargoSlotPo != null && cargoSlotPo.containsGood()) {
				builder.add(new CargoSlot(this, new GoodAmount(cargoSlotPo.getGoodType(), cargoSlotPo.getAmount())));
			} else {
				builder.add(new CargoSlot(this));
			}
		}
		this.slots = builder.build();
	}
	
	Cargo(final Unit owner, final int capacity) {
		this.owner = Preconditions.checkNotNull(owner);

		final ImmutableList.Builder<CargoSlot> builder = ImmutableList.builder();
		for (int i = 0; i < capacity; i++) {
			builder.add(new CargoSlot(this));
		}
		this.slots = builder.build();
	}

	CargoPo save() {
		final CargoPo out = new CargoPo();
		slots.forEach(cargoSlot -> {
			out.getSlots().add(cargoSlot.save());
		});
		return out;
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
}
