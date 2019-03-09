package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.ConstructionSlotPo;

import com.google.common.base.Preconditions;

public class ConstructionSlot {

    private final Construction construction;

    private final Model model;

    private PlaceConstructionSlot placeConstruction;

    ConstructionSlot(final Model model, final Construction construction) {
        this.model = Preconditions.checkNotNull(model);
        this.construction = Preconditions.checkNotNull(construction);
    }

    public boolean isEmpty() {
        return placeConstruction == null;
    }

    public void set(PlaceConstructionSlot placeConstruction) {
        Preconditions.checkState(isEmpty(),
                String.format("Can't insert placeConstruction (%s) slot (%s) is not empty.",
                        placeConstruction, this));
        this.placeConstruction = placeConstruction;
        model.fireUnitMovedToConstruction(placeConstruction.getUnit());
    }

    Colony getColony() {
        return construction.getColony();
    }

    ConstructionSlotPo save() {
        final ConstructionSlotPo out = new ConstructionSlotPo();
        if (!isEmpty()) {
            out.setWorkerId(getUnit().getId());
        }
        return out;
    }

    public void clear(final boolean validate) {
        placeConstruction = null;
        if (validate) {
            construction.verifyNumberOfUnitsOptionallyDestroyColony();
        }
    }

    public Unit getUnit() {
        return placeConstruction.getUnit();
    }

    /**
     * When there is unit than return unit production modifier otherwise return
     * 0.
     * <p>
     * Production modifier is optional bonus to base production. For example
     * skilled expert rum distiller distill more rum per turn than free
     * colonist. some specific skills.
     * </p>
     *
     * @return return production modifier
     */
    public float getProductionModifier() {
        if (isEmpty()) {
            return 0;
        } else {
            if (getProducedType().isPresent()) {
                return getUnit().getType().getProductionModifier(getProducedType().get());
            } else {
                return 0;
            }
        }
    }

    /**
     * Provide information how much could be produced / consumed from given
     * source goods.
     *
     * @param sourceGoods
     *            optional source goods
     * @return how much could be produced and consumed per turn
     */
    public ConstructionTurnProduction getProduction(final Goods sourceGoods) {
        final ConstructionType type = construction.getType();

        Preconditions.checkArgument(type.getProduce().isPresent(),
                "Method can't count production when construction doesn't produce any goods.");
        Preconditions.checkArgument(type.getConsumed().isPresent(),
                "Methods can't be called when production doesn't consume any goods.");
        Preconditions.checkArgument(sourceGoods != null,
                "Source goods is required, but it's null.");

        if (isEmpty()) {
            return new ConstructionTurnProduction(Goods.of(type.getConsumed().get()),
                    Goods.of(type.getProduce().get()), null);
        } else {
            // construction consume goods
            final Goods turnProduction = type.getProductionPerTurn().get()
                    .multiply(getProductionModifier());
            final Goods consumed = type.getConsumptionPerTurn().get();
            if (sourceGoods.isGreaterOrEqualsThan(consumed)) {
                // There is enough source to produce all goods.
                return new ConstructionTurnProduction(consumed, turnProduction, null);
            } else {
                // There is not enough source to produce everything.
                final Goods partialProduction = Goods.of(turnProduction.getType(),
                        (int) (sourceGoods.getAmount() * type.getProductionRatio()));
                final Goods finalProduction = partialProduction.multiply(getProductionModifier());
                final Goods wastedProduction = turnProduction.substract(finalProduction);
                final Goods partialConsumed = Goods.of(consumed.getType(),
                        partialProduction.divide(type.getProductionRatio()).getAmount());
                return new ConstructionTurnProduction(partialConsumed, finalProduction,
                        wastedProduction);
            }
        }
    }

    /**
     * Return production of slot per turn. Work just in case when production
     * doesn't consume any other goods.
     * 
     * @return
     */
    public ConstructionTurnProduction getProduction() {
        final ConstructionType type = construction.getType();
        Preconditions.checkArgument(type.getProduce().isPresent(),
                "Construction doesn't produce any goods.");
        Preconditions.checkArgument(!type.getConsumed().isPresent(),
                "Construction consume some goods.");
        final Goods turnProduction = type.getProductionPerTurn().get();
        return new ConstructionTurnProduction(null,
                turnProduction.multiply(getProductionModifier()), null);

    }

    private Optional<GoodsType> getProducedType() {
        return construction.getType().getProduce();
    }

    public boolean isValid() {
        return construction.isValid();
    }

}
