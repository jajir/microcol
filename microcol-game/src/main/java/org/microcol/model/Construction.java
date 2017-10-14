package org.microcol.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Construction {

	private final ConstructionType type;

	private final List<ConstructionSlot> workingSlots;
	
	Construction(final ConstructionType type, final List<ConstructionSlot> workingSlots) {
		this.type = Preconditions.checkNotNull(type);
		this.workingSlots = Preconditions.checkNotNull(workingSlots);
	}
	
	static Construction build(final ConstructionType type){
		final List<ConstructionSlot> list = Lists.newArrayList();
		for (int i = 0; i < type.getSlotsForWorkers(); i++) {
			list.add(new ConstructionSlot());
		}
		return new Construction(type, list);
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

	ConstructionSlot getSlotAt(final int index) {
		return workingSlots.get(index);
	}

	public int getProductionPerTurn(final Colony colony) {
		return getProduction(colony, colony.getColonyWarehouse()).getRealProductionPerTurn();
	}

	List<ConstructionSlot> getOrderedSlots() {
		return workingSlots.stream()
				.sorted(Comparator.comparing(sort -> -sort.getProductionModifier(getType().getProduce().get())))
				.collect(Collectors.toList());
	}
	
	/**
	 * Return value is computed base on construction type basic production per turn.
	 * @return
	 */
	public int getBasicProductionPerSlot(){
		return getType().getProductionPerTurn();
	}

	public ConstructionProduction getProduction(final Colony colony, final ColonyWarehouse colonyWarehouse) {
		if (getType().getProduce().isPresent()) {
			final GoodType producedGoodType = getType().getProduce().get();
			ConstructionProduction out = ConstructionProduction.EMPTY;
			for (final ConstructionSlot slot : getConstructionSlots()) {
				final ConstructionProduction tmp = getType().getConstructionProduction(colony)
						.multiply(slot.getProductionModifier(producedGoodType));
				out = out.add(tmp);
			}
			out = out.limit(colonyWarehouse);
			return out;
		}else{
			return ConstructionProduction.EMPTY;
		}
	}
	
	
	/**
	 * Method should be called once per turn. It produce resources on field.
	 *
	 * @param colonyWarehouse
	 *            required colony warehouse
	 */
	public void produce(final Colony colony, final ColonyWarehouse colonyWarehouse) {
		if (getType().getProduce().isPresent()) {
			final GoodType producedGoodType = getType().getProduce().get();
			getConstructionSlots().forEach(slot -> {
				getType().getConstructionProduction(colony).multiply(slot.getProductionModifier(producedGoodType))
						.limit(colonyWarehouse).consume(colonyWarehouse);
			});
		}
	}

}
