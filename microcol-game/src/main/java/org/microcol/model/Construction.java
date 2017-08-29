package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Construction {

	private final ConstructionType type;

	private final List<PlaceConstruction> workingSlots;
	
	Construction(final ConstructionType type) {
		this.type = Preconditions.checkNotNull(type);
		workingSlots = Lists.newArrayList();
		for(int i=0;i<type.getSlotsForWorkers();i++){
			workingSlots.add(null);
		}
	}

	public ConstructionType getType() {
		return type;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Construction.class).add("name", type.name()).toString();
	}

	public PlaceConstruction placeWorker(final int position, final Unit unit) {
		Preconditions.checkArgument(position >= 0, "Position index have to bigger that 0.");
		Preconditions.checkArgument(position < type.getSlotsForWorkers(),
				"Maximum number of slots for workers is (%s), you try to put worker at (%s).",
				type.getSlotsForWorkers(), position);
		Preconditions.checkNotNull(unit);
		Preconditions.checkState(workingSlots.get(position)==null,"Working slot is already ocupied.");
		final PlaceConstruction placeConstruction = new PlaceConstruction(unit, this);
		workingSlots.set(position, placeConstruction);
		return placeConstruction;
	}

}
