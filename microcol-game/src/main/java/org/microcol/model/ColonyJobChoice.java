package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

/** A read-only job offer bound to the worker and exact destination shown. */
public final class ColonyJobChoice {
    public enum Kind {
        CURRENT_FIELD, BEST_FIELD, INDOOR
    }

    public enum UnavailableReason {
        ACTOR_UNAVAILABLE, EQUIPMENT_BLOCKED, CROP_UNSUPPORTED,
        NO_ELIGIBLE_FIELD, BUILDING_FULL
    }

    private final Model model;
    private final Colony colony;
    private final Unit worker;
    private final Player owner;
    private final Place source;
    private final GoodsType sourceCrop;
    private final Kind kind;
    private final GoodsType goodsType;
    private final ColonyField field;
    private final Construction construction;
    private final ConstructionSlot slot;
    private final ConstructionType constructionType;
    private final Goods potential;
    private final Goods currentFieldYield;
    private final Goods bestFieldYield;
    private final Goods inputNeeded;
    private final Goods currentStock;
    private final UnavailableReason unavailableReason;
    private final boolean specialty;

    private ColonyJobChoice(final Model model, final Colony colony,
            final Unit worker, final Kind kind, final GoodsType goodsType,
            final ColonyField field, final ColonyField currentField,
            final ColonyField bestField, final Construction construction,
            final ConstructionSlot slot, final UnavailableReason reason) {
        this.model = model;
        this.colony = colony;
        this.worker = worker;
        owner = colony.getOwner();
        source = worker.getPlace();
        sourceCrop = worker.isAtPlaceColonyField()
                ? worker.getPlaceColonyField().getProducedGoodsType()
                : null;
        this.kind = kind;
        this.goodsType = goodsType;
        this.field = field;
        this.construction = construction;
        this.slot = slot;
        constructionType = construction == null ? null : construction.getType();
        unavailableReason = reason;
        potential = reason != null ? null
                : field != null
                        ? field.getPotentialProduction(worker, goodsType)
                        : slot.getPotentialProduction(worker);
        currentFieldYield = currentField != null
                && currentField.canProduce(goodsType)
                        ? currentField.getPotentialProduction(worker, goodsType)
                        : null;
        bestFieldYield = bestField == null ? null
                : bestField.getPotentialProduction(worker, goodsType);
        inputNeeded = constructionType == null
                || constructionType.getConsumptionPerTurn().isEmpty() ? null
                        : ConstructionSlot.getPotentialInput(constructionType,
                                worker);
        currentStock = inputNeeded == null ? null
                : colony.getWarehouse().getGoods(inputNeeded.getType());
        specialty = worker.getType().getProductionModifier(goodsType) > 1;
    }

    static List<ColonyJobChoice> forWorker(final Model model,
            final Colony colony, final Unit worker) {
        if (worker == null) {
            return List.of();
        }
        final List<ColonyJobChoice> choices = new ArrayList<>();
        final ColonyField current = colony.getColonyFields().stream()
                .filter(field -> field.isWorkedBy(worker)).findFirst()
                .orElse(null);
        final boolean actorAvailable = colony.canWorkerActHere(worker);
        final boolean canMove = colony.canAcceptWorker(worker);
        for (final GoodsType goods : GoodsType.GOOD_TYPES) {
            if (colony.getColonyFields().stream().noneMatch(
                    field -> field.isValid() && field.canProduce(goods))) {
                continue;
            }
            ColonyField best = null;
            for (final ColonyField candidate : colony.getColonyFields()) {
                if (canUseField(colony, worker, candidate, goods)
                        && (best == null || candidate
                                .getPotentialProduction(worker, goods)
                                .getAmount() > best
                                        .getPotentialProduction(worker, goods)
                                        .getAmount())) {
                    best = candidate;
                }
            }
            if (current != null) {
                final UnavailableReason reason = !actorAvailable
                        ? UnavailableReason.ACTOR_UNAVAILABLE
                        : !current.canProduce(goods)
                                ? UnavailableReason.CROP_UNSUPPORTED
                                : null;
                choices.add(new ColonyJobChoice(model, colony, worker,
                        Kind.CURRENT_FIELD, goods, current, current, best, null,
                        null, reason));
            }
            final UnavailableReason reason = !actorAvailable
                    ? UnavailableReason.ACTOR_UNAVAILABLE
                    : best != null ? null
                            : !canMove ? UnavailableReason.EQUIPMENT_BLOCKED
                                    : UnavailableReason.NO_ELIGIBLE_FIELD;
            choices.add(
                    new ColonyJobChoice(model, colony, worker, Kind.BEST_FIELD,
                            goods, best, current, best, null, null, reason));
        }
        for (final Construction building : colony.getConstructions()) {
            if (building.getProducedGoodsType().isEmpty()) {
                continue;
            }
            final ConstructionSlot target = building.getConstructionSlots()
                    .stream()
                    .filter(slot -> !slot.isEmpty() && slot.getUnit() == worker)
                    .findFirst()
                    .orElseGet(() -> building.getConstructionSlots().stream()
                            .filter(slot -> slot.canAccept(worker)).findFirst()
                            .orElse(null));
            final UnavailableReason reason = !actorAvailable
                    ? UnavailableReason.ACTOR_UNAVAILABLE
                    : target != null ? null
                            : !canMove ? UnavailableReason.EQUIPMENT_BLOCKED
                                    : UnavailableReason.BUILDING_FULL;
            choices.add(new ColonyJobChoice(model, colony, worker, Kind.INDOOR,
                    building.getProducedGoodsType().orElseThrow(), null, null,
                    null, building, target, reason));
        }
        return List.copyOf(choices);
    }

    private static boolean canUseField(final Colony colony, final Unit worker,
            final ColonyField field, final GoodsType goods) {
        return field.isValid() && colony.getColonyFields().contains(field)
                && field.canProduce(goods)
                && (field.isWorkedBy(worker) ? colony.canWorkerActHere(worker)
                        : field.canAccept(worker, goods));
    }

    /**
     * Revalidate every captured condition before changing a job or equipment.
     */
    public void execute() {
        model.checkTurnPreparationComplete();
        Preconditions.checkState(
                unavailableReason == null && colony.getOwner() == owner
                        && worker.getOwner() == owner
                        && colony.canWorkerActHere(worker)
                        && worker.getPlace() == source
                        && (sourceCrop == null || worker.getPlaceColonyField()
                                .getProducedGoodsType() == sourceCrop),
                "Worker or source job changed; refresh the choices");
        if (field != null) {
            Preconditions.checkState(
                    canUseField(colony, worker, field, goodsType)
                            && potential.equals(field
                                    .getPotentialProduction(worker, goodsType)),
                    "Field or output changed; refresh the choices");
            if (field.isWorkedBy(worker)) {
                field.setProducedGoodsType(worker, goodsType);
            } else {
                worker.placeToColonyField(field, goodsType);
            }
        } else {
            Preconditions
                    .checkState(
                            construction.getType() == constructionType
                                    && colony.getConstructions()
                                            .contains(construction)
                                    && construction
                                            .getConstructionSlots()
                                            .contains(slot)
                                    && (slot.canAccept(worker)
                                            || (!slot.isEmpty() && slot
                                                    .getUnit() == worker))
                                    && potential.equals(slot
                                            .getPotentialProduction(worker)),
                            "Slot or output changed; refresh the choices");
            if (slot.isEmpty()) {
                worker.placeToColonyStructureSlot(slot);
            }
        }
    }

    public Kind getKind() {
        return kind;
    }

    public GoodsType getGoodsType() {
        return goodsType;
    }

    public Optional<Goods> getPotential() {
        return Optional.ofNullable(potential);
    }

    public Optional<Goods> getCurrentFieldYield() {
        return Optional.ofNullable(currentFieldYield);
    }

    public Optional<Goods> getBestFieldYield() {
        return Optional.ofNullable(bestFieldYield);
    }

    public Optional<Location> getTargetLocation() {
        return Optional.ofNullable(field).map(ColonyField::getLocation);
    }

    public Optional<ConstructionType> getConstructionType() {
        return Optional.ofNullable(constructionType);
    }

    public Optional<Goods> getInputNeeded() {
        return Optional.ofNullable(inputNeeded);
    }

    public Optional<Goods> getCurrentStock() {
        return Optional.ofNullable(currentStock);
    }

    public Optional<UnavailableReason> getUnavailableReason() {
        return Optional.ofNullable(unavailableReason);
    }

    public boolean isSpecialty() {
        return specialty;
    }
}
