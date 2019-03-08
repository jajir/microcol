package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ConstructionSlotPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Construction {

    private ConstructionType type;

    private final List<ConstructionSlot> workingSlots;

    private final Colony colony;

    Construction(final Colony colony, final ConstructionType type,
            final Function<Construction, List<ConstructionSlot>> constructionsSlotBuilder) {
        this.colony = Preconditions.checkNotNull(colony);
        this.type = Preconditions.checkNotNull(type);
        this.workingSlots = Preconditions.checkNotNull(constructionsSlotBuilder.apply(this));
    }

    static Construction build(final Model model, final Colony colony, final ConstructionType type) {
        return new Construction(colony, type, construction -> {
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
                .sorted(Comparator.comparing(
                        sort -> -sort.getProductionModifier(getType().getProduce().get())))
                .collect(Collectors.toList());
    }

    //TODO when construction doesn't consume goods second parameter is useless
    //TODO add test and redesign it to Goods class
    ConstructionTurnProduction getProduction(final Goods sourceGoods) {
        Preconditions.checkNotNull(sourceGoods);
        if (getType().getProduce().isPresent()) {
            final GoodsType producedGoodsType = getType().getProduce().get();
            /*
             * Consumption per turn is always 0. Even when there are base
             * production like crosses. Base production doesn't consume any
             * sources.
             */
            int consumptionPerTurn = 0;
            int productionPerTurn = type.getBaseProductionPerTurn();
            int productioPerTurnBlocked = 0;
            for (final ConstructionSlot slot : getConstructionSlots()) {
                if (!slot.isEmpty()) {
                    float multiplier = slot.getProductionModifier(producedGoodsType);
                    final int tmpProd = (int) (getType().getProductionPerTurn() * multiplier);
                    final int tmpCons = getType().getConsumptionPerTurn();

                    final int canBeconsumed = sourceGoods.getAmount() - consumptionPerTurn;

                    if (tmpCons <= canBeconsumed) {
                        productionPerTurn += tmpProd;
                        consumptionPerTurn += tmpCons;
                    } else {
                        final int partialProd = (int) (getType().getProductionRatio()
                                * canBeconsumed * multiplier);
                        consumptionPerTurn += canBeconsumed;
                        productionPerTurn += partialProd;
                        productioPerTurnBlocked += tmpProd - partialProd;
                    }
                }
            }
            return new ConstructionTurnProduction(
                    Goods.of(sourceGoods.getType(), consumptionPerTurn),
                    Goods.of(getType().getProduce().get(), productionPerTurn),
                    Goods.of(getType().getProduce().get(), productioPerTurnBlocked));
        } else {
            return new ConstructionTurnProduction(
                    Goods.of(sourceGoods.getType(), 0),
                    Goods.of(getType().getProduce().get(), 0),
                    Goods.of(getType().getProduce().get(), 0));
        }
    }

    /**
     * Method should be called once per turn. It produce resources in construction.
     *
     * @param colony
     *            required colony where is warehouse and construction placed
     * @param warehouse
     *            required colony warehouse
     */
    public void countTurnProduction(final Colony colony, final ColonyWarehouse warehouse) {
        getType().getProduce().ifPresent(producedGoodsType -> {
            if (getType().getConsumed().isPresent()) {
                final GoodsType consumedGoodsType = getType().getConsumed().get();
                final Goods availableGoodsSource = warehouse.getGoods(consumedGoodsType);
                final ConstructionTurnProduction prod = getProduction(availableGoodsSource);
                warehouse.removeGoods(prod.getConsumedGoods());
                //TODO following could be impossible, warehouse could be full.
                warehouse.addGoods(prod.getProducedGoods());
            } else {
                final Goods availableGoodsSource = warehouse.getGoods(producedGoodsType);
                final ConstructionTurnProduction prod = getProduction(availableGoodsSource);
                warehouse.addGoods(prod.getProducedGoods());
            }
        });
    }

    public boolean isValid() {
        return colony.isValid();
    }

}
