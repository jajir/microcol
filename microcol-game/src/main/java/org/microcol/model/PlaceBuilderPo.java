package org.microcol.model;

import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ConstructionSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class PlaceBuilderPo implements Function<Unit, Place> {

    private final ChainOfCommandStrategy<PlaceBuilderContext, Place> placeBuilders = new ChainOfCommandStrategy<PlaceBuilderContext, Place>(
            Lists.newArrayList((context) -> {
                /**
                 * Map
                 */
                final UnitPo unitPo = context.getUnitPo();
                final Unit unit = context.getUnit();
                if (unitPo.getPlaceMap() != null) {
                    return new PlaceLocation(unit,
                            unitPo.getPlaceMap().getLocation(),
                            unitPo.getPlaceMap().getOrientation());
                }
                return null;
            }, (context) -> {
                /**
                 * High seas
                 */
                final UnitPo unitPo = context.getUnitPo();
                final Unit unit = context.getUnit();
                if (unitPo.getPlaceHighSeas() != null) {
                    final var voyage = unitPo.getPlaceHighSeas();
                    Preconditions.checkArgument(
                            voyage.getWeather() != null
                                    && voyage.getOriginalTurns() != null
                                    && voyage.isOriginalToEurope() != null
                                    && voyage.isTravelToEurope() != null
                                    && voyage.getPosition() != null
                                    && unitPo.getDepartureDirection() != null,
                            "Missing saved voyage metadata");
                    Preconditions.checkArgument(
                            voyage.getPosition() >= 0 && voyage
                                    .getPosition() <= voyage.getOriginalTurns(),
                            "Invalid saved voyage position");
                    final int remaining = voyage.isOriginalToEurope()
                            .booleanValue() == voyage.isTravelToEurope()
                                    .booleanValue()
                                            ? voyage.getOriginalTurns()
                                                    - voyage.getPosition()
                                            : voyage.getPosition();
                    Preconditions.checkArgument(
                            voyage.getRemainigTurns() == remaining,
                            "Saved voyage remaining time disagrees with position");
                    final int ordinary = AtlanticVoyageDuration.calculate(
                            voyage.getWeather(), voyage.isOriginalToEurope(),
                            unitPo.getDepartureDirection(),
                            unitPo.getType().getSpeed());
                    final boolean magellan = unit.getOwner()
                            .getCongressMembers()
                            .contains(Founder.FERDINAND_MAGELLAN);
                    Preconditions.checkArgument(voyage
                            .getOriginalTurns() == ordinary
                            || magellan && voyage
                                    .getOriginalTurns() == AtlanticVoyageDuration
                                            .calculate(voyage.getWeather(),
                                                    voyage.isOriginalToEurope(),
                                                    unitPo.getDepartureDirection(),
                                                    unitPo.getType().getSpeed(),
                                                    true),
                            "Saved voyage duration disagrees with its factors");
                    return new PlaceHighSea(unit, voyage.isOriginalToEurope(),
                            voyage.isTravelToEurope(), voyage.getWeather(),
                            voyage.getOriginalTurns(), voyage.getPosition());
                }
                return null;
            }, (context) -> {
                final var repair = context.getUnitPo().getPlaceShipRepair();
                if (repair == null) {
                    return null;
                }
                Preconditions.checkArgument(
                        repair.isEuropeDestination() != null
                                && repair.isOriginWasPort() != null
                                && repair.getPhase() != null
                                && repair.getRemainingPhaseUpdates() != null
                                && repair.getRepairDuration() != null,
                        "Missing ship repair metadata");
                return new PlaceShipRepair(context.getUnit(),
                        repair.getOrigin(), repair.isOriginWasPort(),
                        repair.isEuropeDestination(),
                        repair.getDestinationColonyId(),
                        PlaceShipRepair.Phase.valueOf(repair.getPhase()),
                        repair.getRemainingPhaseUpdates(),
                        repair.getRepairDuration());
            }, (context) -> {
                /**
                 * Europe port
                 */
                final UnitPo unitPo = context.getUnitPo();
                final Unit unit = context.getUnit();
                final Model model = context.getModel();
                if (unitPo.getPlaceEuropePort() != null) {
                    if (unitPo.getType().isShip()) {
                        return new PlaceEuropePort(unit,
                                model.getEurope().getPort());
                    } else {
                        return new PlaceEuropePier(unit);
                    }
                }
                return null;
            }, (context) -> {
                /**
                 * Colony field
                 */
                final UnitPo unitPo = context.getUnitPo();
                final Unit unit = context.getUnit();
                final Model model = context.getModel();
                final ModelPo modelPo = context.getModelPo();
                if (unitPo.getPlaceColonyField() != null) {
                    for (final ColonyPo colonyPo : modelPo.getColonies()) {
                        final PlaceColonyField out = tryToFindColonyField(
                                colonyPo, unit, model);
                        if (out != null) {
                            return out;
                        }
                    }
                    throw new IllegalArgumentException(String.format(
                            "It's not possible to define place unit (%s) to colony field",
                            unitPo));
                }
                return null;
            }, (context) -> {
                /**
                 * Colony construction
                 */
                final UnitPo unitPo = context.getUnitPo();
                final Unit unit = context.getUnit();
                final Model model = context.getModel();
                final ModelPo modelPo = context.getModelPo();
                if (unitPo.getPlaceConstructionSlot() != null) {
                    for (final ColonyPo colonyPo : modelPo.getColonies()) {
                        final PlaceConstructionSlot out = tryToFindConstructionSlot(
                                colonyPo, unit, model);
                        if (out != null) {
                            return out;
                        }
                    }
                    throw new IllegalArgumentException(String.format(
                            "It's not possible to define place unit (%s) to construction slot",
                            unitPo));
                }
                return null;
            }, (context) -> {
                /**
                 * Native course (R-0148)
                 */
                final var course = context.getUnitPo().getPlaceNativeCourse();
                if (course == null) {
                    return null;
                }
                Preconditions.checkArgument(
                        course.getSettlementId() != null
                                && course.getSettlementId() >= 0,
                        "Missing saved native course settlement");
                return new PlaceNativeCourse(context.getUnit(),
                        course.getSettlementId());
            }, (context) -> {
                /**
                 * Native mission (R-0164)
                 */
                final var mission = context.getUnitPo().getPlaceNativeMission();
                if (mission == null) {
                    return null;
                }
                Preconditions.checkArgument(
                        mission.getSettlementId() != null
                                && mission.getSettlementId() >= 0,
                        "Missing saved native mission settlement");
                return new PlaceNativeMission(context.getUnit(),
                        mission.getSettlementId());
            }, this::tryToBuildPlaceCargoSlot));

    private final UnitPo unitPo;
    private final ModelPo modelPo;
    private final Model model;

    private Place tryToBuildPlaceCargoSlot(final PlaceBuilderContext context) {
        /**
         * Unit's cargo
         */
        final UnitPo unitPo = context.getUnitPo();
        final Unit unit = context.getUnit();
        final ModelPo modelPo = context.getModelPo();
        if (unitPo.getPlaceCargoSlot() != null) {
            // find unit in which cargo should be unit placed
            // place it to correct slot
            final Integer idUnitInCargo = unitPo.getId();
            final Integer slotIndex = getSlotId(idUnitInCargo);
            final UnitPo holdingUnitPo = modelPo
                    .getUnitWithUnitInCargo(idUnitInCargo);
            final UnitWithCargo holdingUnit = getHoldingUnit(holdingUnitPo);
            final CargoSlot cargoSlot = holdingUnit.getCargo()
                    .getSlotByIndex(slotIndex);
            final PlaceCargoSlot placeCargoSlot = new PlaceCargoSlot(unit,
                    cargoSlot);
            cargoSlot.store(placeCargoSlot);
            return placeCargoSlot;
        }
        return null;
    }

    public PlaceBuilderPo(final UnitPo unitPo, final ModelPo modelPo,
            final Model model) {
        this.unitPo = Preconditions.checkNotNull(unitPo);
        this.modelPo = Preconditions.checkNotNull(modelPo);
        this.model = Preconditions.checkNotNull(model);
    }

    private Integer getSlotId(final Integer idUnitInCargo) {
        final UnitPo tmp = modelPo.getUnitWithUnitInCargo(idUnitInCargo);
        int index = 0;
        for (final CargoSlotPo slot : tmp.getCargo().getSlots()) {
            if (idUnitInCargo.equals(slot.getUnitId())) {
                return index;
            }
            index++;
        }
        throw new MicroColException(
                String.format("unable to find slot for (%s)", idUnitInCargo));
    }

    private UnitWithCargo getHoldingUnit(final UnitPo holdingUnitPo) {
        Unit holdingUnit = null;
        final Optional<Unit> oHoldingUnit = model
                .tryGetUnitById(holdingUnitPo.getId());
        if (oHoldingUnit.isPresent()) {
            holdingUnit = oHoldingUnit.get();
        } else {
            // lets create this unit
            holdingUnit = model.createUnit(modelPo, holdingUnitPo);
        }
        Preconditions.checkState(holdingUnit.canHoldCargo(),
                "holding unit (%s) should be able to hold cargo.", holdingUnit);
        return (UnitWithCargo) holdingUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.microcol.model.PlaceBuilder#build(org.microcol.model.Unit)
     */
    @Override
    public Place apply(final Unit unit) {
        return placeBuilders
                .apply(new PlaceBuilderContext(unit, unitPo, modelPo, model));
    }

    private PlaceConstructionSlot tryToFindConstructionSlot(
            final ColonyPo colonyPo, final Unit unit, final Model model) {
        for (final ConstructionPo constructionPo : colonyPo
                .getConstructions()) {
            int slotId = 0;
            for (final ConstructionSlotPo slotPo : constructionPo.getSlots()) {
                if (slotPo.getWorkerId() != null
                        && unit.getId() == slotPo.getWorkerId()) {
                    final Optional<Colony> oColony = model
                            .getColonyById(colonyPo.getId());
                    Preconditions.checkState(oColony.isPresent(),
                            "Colony at (%s) is not in model",
                            colonyPo.getLocation());
                    final Colony colony = oColony.get();
                    final Construction construction = colony
                            .getConstructionByType(constructionPo.getType());
                    final PlaceConstructionSlot out = new PlaceConstructionSlot(
                            unit, construction.getSlotAt(slotId));
                    construction.getSlotAt(slotId).set(out);
                    return out;
                }
                slotId++;
            }
        }
        return null;
    }

    private PlaceColonyField tryToFindColonyField(final ColonyPo colonyPo,
            final Unit unit, final Model model) {
        for (final ColonyFieldPo constructionPo : colonyPo.getColonyFields()) {
            if (constructionPo.getWorkerId() != null
                    && unit.getId() == constructionPo.getWorkerId()) {
                final Optional<Colony> oColony = model
                        .getColonyById(colonyPo.getId());
                Preconditions.checkState(oColony.isPresent(),
                        "Colony at (%s) is not in model",
                        colonyPo.getLocation());
                final Colony colony = oColony.get();
                final Direction direction = Direction
                        .valueOf(constructionPo.getDirection());
                final ColonyField colonyField = colony
                        .getColonyFieldInDirection(direction);
                PlaceColonyField out = new PlaceColonyField(unit, colonyField,
                        constructionPo.getProducedGoodsType());
                colonyField.setPlaceColonyField(out);
                return out;
            }
        }
        return null;
    }

}
