package org.microcol.model;

import org.microcol.model.store.ConstructionSlotPo;
import org.microcol.model.unit.UnitFreeColonist;

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
        Preconditions.checkState(isEmpty(), String.format(
                "Can't insert placeConstruction (%s) slot (%s) is not empty.",
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
            if (getConstructionType().getProductionPerTurn().isPresent()) {
                final Goods productionPerTurn = getConstructionType()
                        .getProductionPerTurn().get();
                return getUnit().getType()
                        .getProductionModifier(productionPerTurn.getType());
            } else {
                return 0;
            }
        }
    }

    /**
     * Provide information how much could be produced / consumed from given
     * source goods.
     *
     * @param sourceGoods optional source goods
     * @return how much could be produced and consumed per turn
     */
    public ConstructionTurnProduction getProduction(final Goods sourceGoods) {
        return getProduction(sourceGoods, getCurrentPolitical());
    }

    /**
     * Same as {@link #getProduction(Goods)} for the political snapshot of one
     * update.
     */
    ConstructionTurnProduction getProduction(final Goods sourceGoods,
            final PoliticalProduction political) {
        final ConstructionType type = construction.getType();

        Preconditions.checkArgument(type.getProductionPerTurn().isPresent(),
                "Method can't count production when construction doesn't produce any goods.");
        Preconditions.checkArgument(type.getConsumptionPerTurn().isPresent(),
                "Methods can't be called when production doesn't consume any goods.");
        Preconditions.checkArgument(sourceGoods != null,
                "Source goods is required, but it's null.");

        // Validate the source type even when this slot is empty.
        sourceGoods.isGreaterOrEqualsThan(
                type.getConsumptionPerTurn().get().getEmpty());
        if (isEmpty()) {
            return new ConstructionTurnProduction(
                    type.getConsumptionPerTurn().get().getEmpty(),
                    type.getProductionPerTurn().get().getEmpty(), null);
        }

        return productionForBudget(type,
                getPotentialProduction(getUnit(), political).getAmount(),
                sourceGoods.getAmount());
    }

    private PoliticalProduction getCurrentPolitical() {
        return isEmpty() ? PoliticalProduction.NONE
                : getColony().getPoliticalProduction();
    }

    /**
     * Return production of slot per turn. Work just in case when production
     * doesn't consume any other goods.
     *
     * @return Return production for slot which do not consume any goods.
     */
    public ConstructionTurnProduction getProduction() {
        return getProduction(getCurrentPolitical());
    }

    /**
     * Same as {@link #getProduction()} for the political snapshot of one
     * update.
     */
    ConstructionTurnProduction getProduction(
            final PoliticalProduction political) {
        final ConstructionType type = construction.getType();
        Preconditions.checkArgument(type.getProductionPerTurn().isPresent(),
                "Construction doesn't produce any goods.");
        Preconditions.checkArgument(!type.getConsumptionPerTurn().isPresent(),
                "Construction consume some goods.");
        final Goods turnProduction = isEmpty()
                ? type.getProductionPerTurn().get().getEmpty()
                : getPotentialProduction(getUnit(), political);
        return new ConstructionTurnProduction(null, turnProduction, null);

    }

    /**
     * Fresh read-only production of the worker. An outside worker is previewed
     * as a new resident, which changes the support fraction.
     */
    Goods getPotentialProduction(final Unit worker) {
        return getPotentialProduction(worker,
                getColony().getPoliticalProductionFor(worker));
    }

    Goods getPotentialProduction(final Unit worker,
            final PoliticalProduction political) {
        final Goods base = getConstructionType().getProductionPerTurn()
                .orElseThrow();
        return potentialProduction(base, worker, political);
    }

    Goods getPotentialInput(final Unit worker) {
        return getPotentialInput(getConstructionType(), worker,
                getColony().getPoliticalProductionFor(worker));
    }

    static Goods getPotentialInput(final ConstructionType type,
            final Unit worker, final PoliticalProduction political) {
        final Goods baseInput = type.getConsumptionPerTurn().orElseThrow();
        final Goods baseOutput = type.getProductionPerTurn().orElseThrow();
        final Goods potential = potentialProduction(baseOutput, worker,
                political);
        return baseInput.setAmount(inputForOutput(type, potential.getAmount()));
    }

    static ConstructionTurnProduction productionForBudget(
            final ConstructionType type, final int outputBudget,
            final int availableInput) {
        Preconditions.checkArgument(outputBudget >= 0 && availableInput >= 0);
        final Goods baseInput = type.getConsumptionPerTurn().orElseThrow();
        final Goods baseOutput = type.getProductionPerTurn().orElseThrow();
        final long possibleOutput = (long) availableInput
                * baseOutput.getAmount() / baseInput.getAmount();
        final int actual = (int) Math.min(outputBudget, possibleOutput);
        final int consumed = inputForOutput(type, actual);
        return new ConstructionTurnProduction(baseInput.setAmount(consumed),
                baseOutput.setAmount(actual), actual == outputBudget ? null
                        : baseOutput.setAmount(outputBudget - actual));
    }

    private static int inputForOutput(final ConstructionType type,
            final int output) {
        final int baseInput = type.getConsumptionPerTurn().orElseThrow()
                .getAmount();
        final int baseOutput = type.getProductionPerTurn().orElseThrow()
                .getAmount();
        return Math.toIntExact(
                ((long) output * baseInput + baseOutput - 1) / baseOutput);
    }

    private static Goods potentialProduction(final Goods base,
            final Unit worker, final PoliticalProduction political) {
        final UnitType type = worker.getType();
        final int penalty = type == UnitType.INDENTURED_SERVANT ? 1
                : type == UnitType.PETTY_CRIMINAL ? 2
                        : isNativeConvert(worker)
                                ? NativeConversion.INDOOR_PENALTY
                                : 0;
        final Goods ordinary = base
                .setAmount(Math.max(0, base.getAmount() - penalty));
        final Goods worked = ordinary
                .multiply(type.getProductionModifier(base.getType()));
        return political.apply(base, worked);
    }

    public ConstructionType getConstructionType() {
        return construction.getType();
    }

    Construction getConstruction() {
        return construction;
    }

    public boolean isValid() {
        return construction.isValid();
    }

    public boolean canAccept(final Unit unit) {
        return isValid()
                && getColony().getConstructions().contains(construction)
                && construction.getConstructionSlots().contains(this)
                && isEmpty() && getColony().canAcceptWorker(unit)
                && getColony().canAcceptSchoolTeacher(this, unit)
                && isWorkAllowed(unit, getConstructionType());
    }

    /**
     * A native convert may not staff a manufacturing building, one with an
     * input consumed per turn (R-0149). Every other worker is unaffected.
     *
     * @param worker required prospective worker
     * @param type   required building type of the slot
     * @return whether the worker may take a slot of this building
     */
    static boolean isWorkAllowed(final Unit worker,
            final ConstructionType type) {
        return type.getConsumptionPerTurn().isEmpty()
                || !isNativeConvert(worker);
    }

    private static boolean isNativeConvert(final Unit worker) {
        return worker instanceof UnitFreeColonist
                && ((UnitFreeColonist) worker).isNativeConvert();
    }

}
