package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ConstructionSlotPo;
import org.microcol.model.turnevent.TurnEventProvider;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Construction {

    private final Model model;

    private ConstructionType type;

    private final List<ConstructionSlot> workingSlots;

    private final Colony colony;

    Construction(final Model model, final Colony colony, final ConstructionType type,
            final Function<Construction, List<ConstructionSlot>> constructionsSlotBuilder) {
        this.model = Preconditions.checkNotNull(model);
        this.colony = Preconditions.checkNotNull(colony);
        this.type = Preconditions.checkNotNull(type);
        this.workingSlots = Preconditions.checkNotNull(constructionsSlotBuilder.apply(this));
    }

    static Construction build(final Model model, final Colony colony, final ConstructionType type) {
        return new Construction(model, colony, type, construction -> {
            final List<ConstructionSlot> list = new ArrayList<>();
            for (int i = 0; i < type.getSlotsForWorkers(); i++) {
                list.add(new ConstructionSlot(model, construction));
            }
            return list;
        });
    }

    ConstructionPo save() {
        final ConstructionPo out = new ConstructionPo();
        out.setType(type);
        out.setSlots(getSaveConstructionSlots());
        return out;
    }

    Colony getColony() {
        return colony;
    }

    void upgrade() {
        type = type.getUpgradeTo().orElseThrow(() -> new IllegalStateException(
                String.format("Colony '%s' can't be upgraded", this)));
    }

    void verifyNumberOfUnitsOptionallyDestroyColony() {
        colony.verifyNumberOfUnitsOptionallyDestroyColony();
    }

    private List<ConstructionSlotPo> getSaveConstructionSlots() {
        final List<ConstructionSlotPo> out = new ArrayList<>();
        workingSlots.forEach(slot -> out.add(slot.save()));
        return out;
    }

    public ConstructionType getType() {
        return type;
    }

    public Optional<GoodsType> getProducedGoodsType() {
        return type.getProduce();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Construction.class).add("name", type.name()).toString();
    }

    public PlaceConstructionSlot placeWorker(final int position, final Unit unit) {
        Preconditions.checkNotNull(unit);
        ConstructionSlot constructionSlot = getSlotAt(position);
        final PlaceConstructionSlot placeConstructionSlot = new PlaceConstructionSlot(unit,
                constructionSlot);
        place(position, placeConstructionSlot);
        return placeConstructionSlot;
    }

    void place(final int position, final PlaceConstructionSlot placeConstruction) {
        Preconditions.checkArgument(position >= 0, "Position index have to bigger that 0.");
        Preconditions.checkArgument(position < type.getSlotsForWorkers(),
                "Maximum number of slots for workers is (%s), you try to put worker at (%s).",
                type.getSlotsForWorkers(), position);
        Preconditions.checkState(workingSlots.get(position).isEmpty(),
                "Working slot is already ocupied.");
        workingSlots.get(position).set(placeConstruction);
    }

    public List<ConstructionSlot> getConstructionSlots() {
        return ImmutableList.copyOf(workingSlots);
    }

    ConstructionSlot getSlotAt(final int index) {
        return workingSlots.get(index);
    }

    List<ConstructionSlot> getOrderedSlots() {
        return workingSlots.stream()
                .sorted(Comparator.comparing(sort -> -sort.getProductionModifier()))
                .collect(Collectors.toList());
    }

    /**
     * Return production definition for constructions that doesn't consume any
     * sources. For example Church.
     *
     * @return construction production statistics per turn
     */
    ConstructionTurnProduction getProduction() {
        Preconditions.checkArgument(getType().getProduce().isPresent(),
                "This construction doesn't produce anything.");
        Preconditions.checkArgument(!getType().getConsumed().isPresent(),
                "This construction consume something. Construction: %s", this);
        ConstructionTurnProduction out = ConstructionTurnProduction.EMPTY;
        for (final ConstructionSlot slot : getConstructionSlots()) {
            final ConstructionTurnProduction tmp = slot.getProduction();
            out = out.add(tmp);
        }
        return out;
    }

    /**
     * Return production definition for constructions that consume some sources.
     * For example Blacksmith.
     *
     * @param sourceGoods
     *            required source goods. How many goods could be consumed during
     *            production.
     * @return construction production statistics per turn
     */
    ConstructionTurnProduction getProduction(final Goods sourceGoods) {
        Preconditions.checkNotNull(sourceGoods);
        Preconditions.checkArgument(getType().getProduce().isPresent(),
                "This construction doesn't produce anything.");
        Preconditions.checkArgument(getType().getConsumed().isPresent(),
                "This construction doesn't consume anything.");
        Goods remaining = sourceGoods;
        ConstructionTurnProduction out = ConstructionTurnProduction.EMPTY;
        for (final ConstructionSlot slot : getConstructionSlots()) {
            final ConstructionTurnProduction tmp = slot.getProduction(sourceGoods);
            out = out.add(tmp);
            remaining = remaining.substract(tmp.getConsumedGoods().get());
        }
        return out;
    }

    /**
     * Method should be called once per turn. It produce resources in
     * construction.
     *
     * @param colony
     *            required colony where is warehouse and construction placed
     * @param warehouse
     *            required colony warehouse
     */
    public void countTurnProduction(final Colony colony, final ColonyWarehouse warehouse) {
        getType().getProduce().ifPresent(producedGoodsType -> {
            if (getType().getConsumed().isPresent()) {
                final GoodsType consumedGoods = getType().getConsumed().get();
                final ConstructionTurnProduction prod = getProduction(
                        warehouse.getGoods(consumedGoods));
                warehouse.removeGoods(prod.getConsumedGoods().get());
                warehouse.addGoodsWithThrowingAway(prod.getProducedGoods().get(),
                        thrownAwayGoods -> {
                            model.addTurnEvent(TurnEventProvider.getGoodsWasThrowsAway(
                                    colony.getOwner(), thrownAwayGoods, colony));
                        });
            } else {
                final ConstructionTurnProduction prod = getProduction();
                warehouse.addGoodsWithThrowingAway(prod.getProducedGoods().get(),
                        thrownAwayGoods -> {
                            model.addTurnEvent(TurnEventProvider.getGoodsWasThrowsAway(
                                    colony.getOwner(), thrownAwayGoods, colony));
                        });
            }

        });
    }

    public boolean isValid() {
        return colony.isValid();
    }

}
