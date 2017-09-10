package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Construction {

	private final ConstructionType type;

	private final List<ConstructionSlot> workingSlots;

	Construction(final ConstructionType type) {
		this.type = Preconditions.checkNotNull(type);
		workingSlots = Lists.newArrayList();
		for (int i = 0; i < type.getSlotsForWorkers(); i++) {
			workingSlots.add(new ConstructionSlot());
		}
	}

	public ConstructionType getType() {
		return type;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Construction.class).add("name", type.name()).toString();
	}

	public PlaceConstructionSlot placeWorker(final int position, final Unit unit) {
		Preconditions.checkNotNull(unit);
		ConstructionSlot constructionSlot = getSlotAt(position);
		final PlaceConstructionSlot placeConstructionSlot = new PlaceConstructionSlot(unit, constructionSlot);
		place(position, placeConstructionSlot);
		return placeConstructionSlot;
	}

	void place(final int position, final PlaceConstructionSlot placeConstruction) {
		Preconditions.checkArgument(position >= 0, "Position index have to bigger that 0.");
		Preconditions.checkArgument(position < type.getSlotsForWorkers(),
				"Maximum number of slots for workers is (%s), you try to put worker at (%s).",
				type.getSlotsForWorkers(), position);
		Preconditions.checkState(workingSlots.get(position).isEmpty(), "Working slot is already ocupied.");
		workingSlots.get(position).set(placeConstruction);
	}

	public List<ConstructionSlot> getConstructionSlots() {
		return ImmutableList.copyOf(workingSlots);
	}
	
	ConstructionSlot getSlotAt(final int index){
		return workingSlots.get(index);
	}
}
