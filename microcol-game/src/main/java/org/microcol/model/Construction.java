package org.microcol.model;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

	ConstructionSlot getSlotAt(final int index) {
		return workingSlots.get(index);
	}

	//TODO method result doesn't correspond to produce()
	public int getProductionPerTurn() {
		final AtomicInteger sum = new AtomicInteger(getType().getBaseProductionPerTurn());
		workingSlots.forEach(slot -> {
			if (!slot.isEmpty()) {
				// TODO JJ use here unit production multiplier.
				sum.addAndGet(type.getProductionPerTurn());
			}
		});
		return sum.get();
	}

	List<ConstructionSlot> getOrderedSlots() {
		return workingSlots.stream()
				.sorted(Comparator.comparing(sort -> -sort.getProductionModifier(getType().getProduce().get())))
				.collect(Collectors.toList());
	}

	/**
	 * Method should be called once per turn. It produce resources on field.
	 *
	 * @param colonyWarehouse
	 *            required colony warehouse
	 */
	public void produce(final ColonyWarehouse colonyWarehouse) {
		if (getType().getProduce().isPresent()) {
			final GoodType producedGoodType = getType().getProduce().get();
			// TODO in all cases is ignored base production per turn
			if (getType().getConsumed().isPresent()) {
				final GoodType consumedGoodType = getType().getConsumed().get();
				getOrderedSlots().forEach(slot -> {
					final int maxConsumption = slot.getMaxConsumptionPerTurn(getType().getConsumptionPerTurn(),
							producedGoodType);
					final int availableGoods = colonyWarehouse.getGoodAmmount(consumedGoodType);
					final int canConsumeGoods = Math.min(availableGoods, maxConsumption);
					final int willBeProduced = (int) (canConsumeGoods * getType().getProductionRatio());
					colonyWarehouse.removeFromWarehouse(consumedGoodType, canConsumeGoods);
					colonyWarehouse.addToWarehouse(producedGoodType, willBeProduced);
				});
			} else {
				/**
				 * Unit doesn't require any inputs to produce some goods.
				 */
				workingSlots.forEach(slot -> {
					final int productionPerTurn = slot.getMaxProductionPerTurn(getType().getProductionPerTurn(),
							producedGoodType) + getType().getBaseProductionPerTurn();
					colonyWarehouse.addToWarehouse(producedGoodType, productionPerTurn);
				});
			}
		}
	}

}
