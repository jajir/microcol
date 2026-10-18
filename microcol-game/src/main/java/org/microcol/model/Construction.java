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

    Construction(final Model model, final Colony colony,
            final ConstructionType type,
            final Function<Construction, List<ConstructionSlot>> constructionsSlotBuilder) {
        this.model = Preconditions.checkNotNull(model);
        this.colony = Preconditions.checkNotNull(colony);
        this.type = Preconditions.checkNotNull(type);
        this.workingSlots = Preconditions
                .checkNotNull(constructionsSlotBuilder.apply(this));
    }

    static Construction build(final Model model, final Colony colony,
            final ConstructionType type) {
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
        if (type.getProductionPerTurn().isPresent()) {
            return Optional.of(type.getProductionPerTurn().get().getType());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Construction.class)
                .add("name", type.name()).toString();
    }

    public PlaceConstructionSlot placeWorker(final int position,
            final Unit unit) {
        Preconditions.checkNotNull(unit);
        ConstructionSlot constructionSlot = getSlotAt(position);
        final PlaceConstructionSlot placeConstructionSlot = new PlaceConstructionSlot(
                unit, constructionSlot);
        place(position, placeConstructionSlot);
        return placeConstructionSlot;
    }

    void place(final int position,
            final PlaceConstructionSlot placeConstruction) {
        Preconditions.checkArgument(position >= 0,
                "Position index have to bigger that 0.");
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

    public Optional<ConstructionSlot> getFirstEmptySlot() {
        return workingSlots.stream().filter(slot -> slot.isEmpty()).findAny();
    }

    ConstructionSlot getSlotAt(final int index) {
        return workingSlots.get(index);
    }

    List<ConstructionSlot> getOrderedSlots() {
        return workingSlots.stream()
                .sorted(Comparator
                        .comparing(sort -> -sort.getProductionModifier()))
                .collect(Collectors.toList());
    }

    /**
     * Return production definition for constructions that doesn't consume any
     * sources. For example Church.
     *
     * @return construction production statistics per turn
     */
    ConstructionTurnProduction getProduction() {
        return getProduction(colony.getPoliticalProduction());
    }

    /**
     * Same as {@link #getProduction()} for the political snapshot of one
     * update. Only staffed slots receive the political change; unattended base
     * bells and crosses do not.
     *
     * @param political support bonus and Tory penalty to apply per job
     * @return construction production statistics per turn
     */
    ConstructionTurnProduction getProduction(
            final PoliticalProduction political) {
        Preconditions.checkNotNull(political);
        Preconditions.checkArgument(
                getType().getProductionPerTurn().isPresent(),
                "This construction doesn't produce anything.");
        Preconditions.checkArgument(
                !getType().getConsumptionPerTurn().isPresent(),
                "This construction consume something. Construction: %s", this);
        ConstructionTurnProduction out = getType().getProductionPerTurn().get()
                .getType() == GoodsType.CROSS
                || getType().getProductionPerTurn().get()
                        .getType() == GoodsType.BELL
                                ? new ConstructionTurnProduction(null, Goods.of(
                                        getType().getProductionPerTurn().get()
                                                .getType(),
                                        getType().getBaseProductionPerTurn()),
                                        null)
                                : ConstructionTurnProduction.EMPTY;
        for (final ConstructionSlot slot : getConstructionSlots()) {
            final ConstructionTurnProduction tmp = slot
                    .getProduction(political);
            out = out.add(tmp);
        }
        if (getType() == ConstructionType.TOWN_HALL
                && out.getProducedGoods().isPresent()) {
            final int base = out.getProducedGoods().get().getAmount();
            final int improved = colony
                    .wasConstructionBuilded(ConstructionType.NEWSPAPER)
                            ? Math.multiplyExact(base, 2)
                            : colony.wasConstructionBuilded(
                                    ConstructionType.PRINTING_PRESS)
                                            ? Math.toIntExact(
                                                    (long) base * 3 / 2)
                                            : base;
            return new ConstructionTurnProduction(null,
                    Goods.of(GoodsType.BELL, improved), null);
        }
        if (out.getProducedGoods().isPresent()
                && out.getProducedGoods().get().getType() == GoodsType.CROSS
                && colony.getOwner().getCongressMembers()
                        .contains(Founder.WILLIAM_PENN)) {
            final int base = out.getProducedGoods().get().getAmount();
            return new ConstructionTurnProduction(null, Goods
                    .of(GoodsType.CROSS, Math.toIntExact((long) base * 3 / 2)),
                    null);
        }
        return out;
    }

    /**
     * Return production definition for constructions that consume some sources.
     * For example Blacksmith.
     *
     * @param sourceGoods required source goods. How many goods could be
     *                    consumed during production.
     * @return construction production statistics per turn
     */
    ConstructionTurnProduction getProduction(final Goods sourceGoods) {
        return getProduction(sourceGoods, colony.getPoliticalProduction());
    }

    /**
     * Same as {@link #getProduction(Goods)} for the political snapshot of one
     * update. The political change raises each staffed job's output budget
     * before the real input limits the total.
     *
     * @param sourceGoods required source goods
     * @param political   support bonus and Tory penalty to apply per job
     * @return construction production statistics per turn
     */
    ConstructionTurnProduction getProduction(final Goods sourceGoods,
            final PoliticalProduction political) {
        Preconditions.checkNotNull(sourceGoods);
        Preconditions.checkNotNull(political);
        Preconditions.checkArgument(
                getType().getProductionPerTurn().isPresent(),
                "This construction doesn't produce anything.");
        Preconditions.checkArgument(
                getType().getConsumptionPerTurn().isPresent(),
                "This construction doesn't consume anything.");
        sourceGoods.isGreaterOrEqualsThan(
                getType().getConsumptionPerTurn().get().getEmpty());
        int outputBudget = 0;
        for (final ConstructionSlot slot : getConstructionSlots()) {
            if (!slot.isEmpty()) {
                outputBudget = Math.addExact(outputBudget,
                        slot.getPotentialProduction(slot.getUnit(), political)
                                .getAmount());
            }
        }
        return ConstructionSlot.productionForBudget(getType(), outputBudget,
                sourceGoods.getAmount());
    }

    /**
     * Method should be called once per turn. It produce resources in
     * construction.
     *
     * @param colony    required colony where is warehouse and construction
     *                  placed
     * @param warehouse required colony warehouse
     */
    public void countTurnProduction(final Colony colony,
            final ColonyWarehouse warehouse) {
        getType().getProductionPerTurn().ifPresent(producedGoodsType -> {
            if (getType().getConsumptionPerTurn().isPresent()) {
                final Goods consumedGoods = getType().getConsumptionPerTurn()
                        .get();
                final ConstructionTurnProduction prod = getProduction(
                        warehouse.getGoods(consumedGoods.getType()));
                warehouse.removeGoods(prod.getConsumedGoods().get());
                warehouse.addGoodsWithThrowingAway(
                        prod.getProducedGoods().get(), thrownAwayGoods -> {
                            model.addTurnEvent(TurnEventProvider
                                    .getGoodsWasThrowsAway(colony.getOwner(),
                                            thrownAwayGoods, colony));
                        });
            } else {
                final ConstructionTurnProduction prod = getProduction();
                warehouse.addGoodsWithThrowingAway(
                        prod.getProducedGoods().get(), thrownAwayGoods -> {
                            model.addTurnEvent(TurnEventProvider
                                    .getGoodsWasThrowsAway(colony.getOwner(),
                                            thrownAwayGoods, colony));
                        });
            }

        });
    }

    public boolean isValid() {
        return colony.isValid();
    }

}
