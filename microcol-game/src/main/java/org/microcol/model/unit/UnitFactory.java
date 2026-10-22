package org.microcol.model.unit;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Cargo;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeConversion;
import org.microcol.model.NativeNation;
import org.microcol.model.MovementPoints;
import org.microcol.model.Place;
import org.microcol.model.PlaceBuilderPo;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

import com.google.common.collect.Lists;
import com.google.common.base.Preconditions;

/**
 * Class responsible for creating instances of unit.
 */
public class UnitFactory {

    private final ChainOfCommandStrategy<UnitCreateRequest, Unit> unitResolver = new ChainOfCommandStrategy<UnitCreateRequest, Unit>(
            Lists.newArrayList(request -> {
                if (UnitType.ARTILLERY == request.getUnitType()) {
                    return new UnitArtillery(request.getModel(),
                            request.getUnitId(), request.getPlaceBuilder(),
                            request.getOwner(), request.getMovementThirds(),
                            request.getAction(), request.isArtilleryDamaged());
                }
                if (UnitType.COLONIST == request.getUnitType()) {
                    return makeFreeColonist(request);
                }
                return null;
            }, request -> {
                if (UnitType.GALLEON == request.getUnitType()) {
                    return makeGalleon(request);
                }
                return null;
            }, request -> {
                if (UnitType.FRIGATE == request.getUnitType()) {
                    return makeFrigate(request);
                }
                return null;
            }, request -> {
                if (UnitType.CARAVEL == request.getUnitType()) {
                    return makeCaravel(request);
                }
                if (UnitType.MERCHANTMAN == request.getUnitType()) {
                    return makeMerchantman(request);
                }
                if (UnitType.PRIVATEER == request.getUnitType()) {
                    return makePrivateer(request);
                }
                if (UnitType.MAN_OF_WAR == request.getUnitType()) {
                    return makeManOfWar(request);
                }
                return null;
            }, request -> {
                if (UnitType.WAGON == request.getUnitType()) {
                    return makeWagon(request);
                }
                if (UnitType.MASTER_BLACKSMITH == request.getUnitType()) {
                    return makeMasterBlacksmith(request);
                }
                if (UnitType.EUROPEAN_SPECIALISTS
                        .contains(request.getUnitType())
                        || UnitType.SCHOOL_ONLY_SPECIALISTS
                                .contains(request.getUnitType())) {
                    return makeSpecialist(request);
                }
                if (request.getUnitType() == UnitType.INDENTURED_SERVANT
                        || request.getUnitType() == UnitType.PETTY_CRIMINAL) {
                    return makeLaborer(request);
                }
                return null;
            }, request -> {
                if (UnitType.BRAVE == request.getUnitType()) {
                    return makeNativeBrave(request);
                }
                if (UnitType.TREASURE_TRAIN == request.getUnitType()) {
                    return makeTreasure(request);
                }
                return null;
            }));

    private Unit makeFreeColonist(final UnitCreateRequest request) {
        final UnitFreeColonist out = new UnitFreeColonist(request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getMovementThirds(),
                request.getAction(), request.getTools(),
                request.isHoldingGuns(), request.isMounted());
        return out;
    }

    private Unit makeMasterBlacksmith(final UnitCreateRequest request) {
        return new UnitMasterBlacksmith(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction(),
                request.getTools(), request.isHoldingGuns(),
                request.isMounted());
    }

    private Unit makeSpecialist(final UnitCreateRequest request) {
        return new UnitSpecialist(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction(),
                request.getTools(), request.isHoldingGuns(),
                request.isMounted(), request.getUnitType());
    }

    private Unit makeLaborer(final UnitCreateRequest request) {
        return new UnitLaborer(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction(),
                request.getTools(), request.isHoldingGuns(),
                request.isMounted(), request.getUnitType());
    }

    private Unit makeGalleon(final UnitCreateRequest request) {
        final UnitGalleon out = new UnitGalleon(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction());
        return out;
    }

    private Unit makeFrigate(final UnitCreateRequest request) {
        final UnitFrigate out = new UnitFrigate(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction());
        return out;
    }

    private Unit makeCaravel(final UnitCreateRequest request) {
        return new UnitCaravel(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getMovementThirds(),
                request.getAction());
    }

    private Unit makeMerchantman(final UnitCreateRequest request) {
        return new UnitMerchantman(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction());
    }

    private Unit makePrivateer(final UnitCreateRequest request) {
        return new UnitPrivateer(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getMovementThirds(),
                request.getAction());
    }

    private Unit makeManOfWar(final UnitCreateRequest request) {
        return new UnitManOfWar(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getMovementThirds(),
                request.getAction());
    }

    private Unit makeWagon(final UnitCreateRequest request) {
        final UnitWagon out = new UnitWagon(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction());
        return out;
    }

    private Unit makeNativeBrave(final UnitCreateRequest request) {
        return new UnitNativeBrave(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction(),
                request.getNativeHomeSettlementId(), request.isNativeHomeless(),
                request.getNativeRole());
    }

    private Unit makeTreasure(final UnitCreateRequest request) {
        return new UnitTreasure(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getMovementThirds(), request.getAction(),
                request.getTreasureValue());
    }

    /**
     * Validate saved native unit state before any unit is constructed, so a
     * rejected save names the broken condition. A native nation owns only
     * braves and a brave only a native nation; a brave's home is a settlement
     * ID or the explicit homeless flag, never both or neither; a brave stands
     * only on map land. The home's nation is checked once all units exist.
     */
    private static void checkSavedNativeUnit(final Model model,
            final UnitPo unitPo, final Player owner) {
        final boolean brave = unitPo.getType() == UnitType.BRAVE;
        Preconditions.checkArgument(brave == owner.isNative(),
                "Invalid saved native unit owner");
        if (!brave) {
            Preconditions
                    .checkArgument(
                            unitPo.getNativeHomeless() == null
                                    && unitPo
                                            .getNativeHomeSettlementId() == null
                                    && unitPo.getNativeRole() == null,
                            "Invalid saved native brave home");
            return;
        }
        Preconditions.checkArgument(unitPo.getNativeRole() != null,
                "Invalid saved native brave role");
        Preconditions.checkArgument(
                unitPo.getNativeHomeless() != null
                        && unitPo.getNativeHomeless() == (unitPo
                                .getNativeHomeSettlementId() == null),
                "Invalid saved native brave home");
        final Location at = unitPo.getPlaceMap() == null ? null
                : unitPo.getPlaceMap().getLocation();
        Preconditions.checkArgument(
                at != null && model.getMap().isValid(at)
                        && model.getMap().getTerrainTypeAt(at).isLand(),
                "Invalid saved native brave placement: map land only");
    }

    /**
     * Validate a saved treasure train before it is constructed. Only a train
     * carries a face value, the value must be positive, and the train either
     * stands on map land or is stored in a ship's holds, never both. Its owner
     * is checked with the other units by {@link #checkSavedNativeUnit}.
     */
    private static void checkSavedTreasure(final Model model,
            final UnitPo unitPo) {
        final boolean treasure = unitPo.getType() == UnitType.TREASURE_TRAIN;
        Preconditions.checkArgument(
                treasure == (unitPo.getTreasureValue() != null),
                "Invalid saved treasure value");
        Preconditions.checkArgument(
                treasure || unitPo.getTreasurePickupColonyId() == null
                        && unitPo.getTreasurePickupNet() == null,
                "Only treasure can await pickup");
        Preconditions.checkArgument(
                (unitPo.getTreasurePickupColonyId() == null) == (unitPo
                        .getTreasurePickupNet() == null),
                "Incomplete treasure pickup");
        if (!treasure) {
            return;
        }
        Preconditions.checkArgument(unitPo.getTreasureValue() > 0,
                "Invalid saved treasure value");
        if (unitPo.getPlaceCargoSlot() != null) {
            Preconditions.checkArgument(unitPo.getPlaceMap() == null,
                    "Invalid saved treasure placement: a stored train has no map square");
            return;
        }
        final Location at = unitPo.getPlaceMap() == null ? null
                : unitPo.getPlaceMap().getLocation();
        Preconditions.checkArgument(
                at != null && model.getMap().isValid(at)
                        && model.getMap().getTerrainTypeAt(at).isLand(),
                "Invalid saved treasure placement: map land only");
    }

    /**
     * Validate the cargo reservations that the saved file names, before any
     * unit is built. A stored unit must reserve exactly the holds its type
     * requires, all in one ship; a unit placed elsewhere names no hold. Every
     * hold that names a unit must name a stored unit of that same ship, so no
     * hold is an orphan.
     *
     * @param modelPo required saved world
     * @param unitPo  required saved unit being validated
     */
    private static void checkSavedCargoReservations(final ModelPo modelPo,
            final UnitPo unitPo) {
        // Every saved hold must be a real entry before any scan dereferences
        // it.
        modelPo.getUnits().forEach(carrier -> {
            if (carrier.getCargo() != null) {
                Preconditions.checkArgument(
                        carrier.getCargo().getSlots() != null
                                && carrier.getCargo().getSlots().stream()
                                        .allMatch(Objects::nonNull),
                        "Invalid cargo slots");
            }
        });
        if (unitPo.getCargo() != null) {
            unitPo.getCargo().getSlots().stream()
                    .filter(slot -> slot.getUnitId() != null)
                    .forEach(slot -> Preconditions.checkArgument(modelPo
                            .getUnits().stream()
                            .anyMatch(passenger -> slot.getUnitId()
                                    .equals(passenger.getId())
                                    && passenger.getPlaceCargoSlot() != null),
                            "Orphan saved cargo hold"));
        }
        final List<UnitPo> carriers = modelPo.getUnits().stream()
                .filter(carrier -> carrier.getCargo() != null && carrier
                        .getCargo().containsUnitInCargo(unitPo.getId()))
                .collect(Collectors.toList());
        if (unitPo.getPlaceCargoSlot() == null) {
            Preconditions.checkArgument(carriers.isEmpty(),
                    "Orphan saved cargo reservation");
            return;
        }
        Preconditions.checkArgument(carriers.size() == 1,
                "Saved unit must reserve holds of exactly one ship");
        final long holds = carriers.get(0).getCargo().getSlots().stream()
                .filter(slot -> unitPo.getId().equals(slot.getUnitId()))
                .count();
        Preconditions.checkArgument(
                holds == unitPo.getType().getRequiredHolds(),
                "Incomplete saved cargo reservation");
    }

    public Unit createUnit(final Model model, final ModelPo modelPo,
            final UnitPo unitPo) {

        Preconditions.checkArgument(
                unitPo.getType() == UnitType.ARTILLERY
                        ? unitPo.getArtilleryDamaged() != null
                                && !unitPo.isHoldingGuns()
                                && !unitPo.isMounted() && unitPo.getTools() == 0
                                && unitPo.getPlaceColonyField() == null
                                && unitPo.getPlaceConstructionSlot() == null
                                && unitPo.getCargo() != null
                                && unitPo.getCargo().getSlots() != null
                                && unitPo.getCargo().getSlots().isEmpty()
                        : unitPo.getArtilleryDamaged() == null,
                "Invalid saved artillery state");

        Preconditions.checkArgument(unitPo.getMovementThirds() != null,
                "Saved unit has no movement allowance");
        if (unitPo.getPlaceShipRepair() != null) {
            Preconditions
                    .checkArgument(
                            unitPo.getType().isShip()
                                    && unitPo.getMovementThirds() == 0
                                    && unitPo.getPlaceMap() == null
                                    && unitPo.getPlaceHighSeas() == null
                                    && unitPo.getPlaceEuropePort() == null
                                    && unitPo.getPlaceCargoSlot() == null
                                    && unitPo.getPlaceColonyField() == null
                                    && unitPo
                                            .getPlaceConstructionSlot() == null,
                            "Invalid saved ship repair placement");
        }

        final Function<Unit, Place> placeBuilderModelPo = new PlaceBuilderPo(
                unitPo, modelPo, model);

        final UnitCreateRequest unitCreateRequest = new UnitCreateRequest(model,
                placeBuilderModelPo, unitPo.getId(), unitPo.getType(),
                model.getPlayerStore().getPlayerByName(unitPo.getOwnerId()),
                UnitActionConverter.convert(unitPo.getAction()),
                unitPo.getMovementThirds());
        unitCreateRequest.setCargoBuilder(unit -> new Cargo(unit,
                unit.getType().getCargoCapacity(), unitPo.getCargo()));
        unitCreateRequest.setHoldingGuns(unitPo.isHoldingGuns());
        unitCreateRequest.setMounted(unitPo.isMounted());
        unitCreateRequest.setTools(unitPo.getTools());
        unitCreateRequest.setArtilleryDamaged(
                Boolean.TRUE.equals(unitPo.getArtilleryDamaged()));

        unitCreateRequest.setVeteran(unitPo.isVeteran());
        unitCreateRequest
                .setNativeHomeSettlementId(unitPo.getNativeHomeSettlementId());
        unitCreateRequest.setNativeHomeless(
                Boolean.TRUE.equals(unitPo.getNativeHomeless()));
        unitCreateRequest.setNativeRole(unitPo.getNativeRole() == null ? null
                : UnitNativeBrave.Role.valueOf(unitPo.getNativeRole()));
        unitCreateRequest.setTreasureValue(unitPo.getTreasureValue());
        checkSavedNativeUnit(model, unitPo, unitCreateRequest.getOwner());
        checkSavedTreasure(model, unitPo);
        checkSavedCargoReservations(modelPo, unitPo);

        final Unit unit = unitResolver.apply(unitCreateRequest);
        if (unit instanceof UnitTreasure) {
            ((UnitTreasure) unit).setPickup(unitPo.getTreasurePickupColonyId(),
                    unitPo.getTreasurePickupNet());
        }
        Preconditions.checkArgument(
                unit.getMovementThirds() >= 0
                        && unit.getMovementThirds() <= MovementPoints
                                .ofPoints(unit.getSpeed()),
                "Invalid saved unit movement allowance");
        Preconditions.checkArgument(
                !unitPo.isVeteran() || unit instanceof UnitFreeColonist,
                "Only colonists can be veterans");
        Preconditions.checkArgument(
                unit instanceof UnitFreeColonist
                        ? unitPo.getWorkUpdates() != null
                        : unitPo.getWorkUpdates() == null
                                && unitPo.getWorkSkill() == null,
                "Invalid saved work history owner");
        Preconditions.checkArgument(
                unit instanceof UnitFreeColonist
                        ? unitPo.getMissionary() != null
                        : unitPo.getMissionary() == null,
                "Invalid saved missionary role owner");
        // A convert keeps its nation and lifetime count; nothing else carries
        // the role, so an ordinary colonist must not carry either field.
        final boolean convert = Boolean.TRUE.equals(unitPo.getNativeConvert());
        Preconditions.checkArgument(convert ? unit instanceof UnitFreeColonist
                && unitPo.getNativeConvertNation() != null
                && unitPo.getNativeConvertUnassignedUpdates() != null
                && unitPo.getNativeConvertUnassignedUpdates() >= 0
                && unitPo
                        .getNativeConvertUnassignedUpdates() < NativeConversion.RETURN_AFTER_UNASSIGNED_UPDATES
                : unitPo.getNativeConvert() == null
                        && unitPo.getNativeConvertNation() == null
                        && unitPo.getNativeConvertUnassignedUpdates() == null,
                "Invalid saved native convert");
        Preconditions.checkArgument(unitPo.getMilitaryRole() != null,
                "Missing saved military role");
        Preconditions.checkArgument(
                !unitPo.getMilitaryRole().isSpecial()
                        || unit instanceof UnitFreeColonist
                                && unitPo.getPlaceColonyField() == null
                                && unitPo.getPlaceConstructionSlot() == null,
                "Only a colonist outside a colony job can have a special role");
        if (unit instanceof UnitFreeColonist) {
            ((UnitFreeColonist) unit)
                    .restoreMilitaryRole(unitPo.getMilitaryRole());
            ((UnitFreeColonist) unit).restoreMissionary(unitPo.getMissionary());
            ((UnitFreeColonist) unit)
                    .restoreVeteran(unitCreateRequest.isVeteran());
            ((UnitFreeColonist) unit).restoreWorkExperience(
                    unitPo.getWorkSkill(), unitPo.getWorkUpdates());
            if (convert) {
                ((UnitFreeColonist) unit).restoreNativeConvert(
                        NativeNation.valueOf(unitPo.getNativeConvertNation()),
                        unitPo.getNativeConvertUnassignedUpdates());
            }
        }
        if (unit.getAction().getType() == UnitActionType.fortify) {
            Preconditions.checkArgument((unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit).isHoldingGuns()
                    || unit instanceof UnitArtillery)
                    && unit.isAtPlaceLocation()
                    && model.getMap().getTerrainTypeAt(unit.getLocation())
                            .isLand()
                    && (((UnitActionFortify) unit.getAction()).isFortified()
                            || unit.getMovementThirds() == 0),
                    "Invalid saved fortification");
        }
        final boolean hasLocation = unitPo.getDepartureLocation() != null;
        final boolean hasDirection = unitPo.getDepartureDirection() != null;
        Preconditions.checkArgument(hasLocation == hasDirection,
                "Incomplete ship departure data");
        Preconditions.checkArgument(!hasLocation || unit instanceof Ship,
                "Only a ship can have a departure coast");
        Preconditions.checkArgument(!unit.isAtHighSea() || hasLocation,
                "A ship in transit requires its departure coast");
        if (hasLocation) {
            ((Ship) unit).rememberDeparture(unitPo.getDepartureLocation(),
                    unitPo.getDepartureDirection());
        }
        // Waiting immigrants exist only on the pier; a front choice must also
        // be willing. Such state is rejected rather than repaired.
        Preconditions.checkArgument(
                unitPo.isImmigrant()
                        ? unit.isAtEuropePier() && !(unitPo.isBoardingOptOut()
                                && unitPo.isBoardingPriority())
                        : !unitPo.isBoardingOptOut()
                                && !unitPo.isBoardingPriority(),
                "Invalid saved immigrant boarding state");
        if (unitPo.isImmigrant()) {
            ((AbstractUnit) unit).restoreImmigrant(unitPo.isBoardingOptOut(),
                    unitPo.isBoardingPriority());
        }
        // A saved order is restored only, never advanced: loading moves
        // nothing.
        if (unitPo.getGoToDestination() == null) {
            Preconditions.checkArgument(
                    unitPo.getGoToColonyId() == null
                            && unitPo.getGoToDestinationName() == null
                            && !unitPo.isGoToPaused(),
                    "Incomplete saved go-to order");
        } else {
            ((AbstractUnit) unit).restoreGoTo(unitPo.getGoToDestination(),
                    unitPo.getGoToColonyId(), unitPo.getGoToDestinationName(),
                    unitPo.isGoToPaused());
        }
        // A saved assignment is restored only; loading moves the carrier and
        // processes no stop, which waits for the owner's next turn.
        if (unitPo.getTradeRouteAssignmentRouteId() == null) {
            Preconditions.checkArgument(
                    unitPo.getTradeRouteAssignmentStopIndex() == null
                            && unitPo.getTradeRouteAssignmentPause() == null
                            && unitPo.getTradeRouteAssignmentStops() == null,
                    "Incomplete saved trade route assignment");
        } else {
            Preconditions.checkArgument(
                    unitPo.getTradeRouteAssignmentStopIndex() != null
                            && unitPo.getTradeRouteAssignmentStops() != null,
                    "Incomplete saved trade route assignment");
            ((AbstractUnit) unit).restoreTradeRouteAssignment(
                    unitPo.getTradeRouteAssignmentRouteId(),
                    unitPo.getTradeRouteAssignmentStopIndex(),
                    unitPo.getTradeRouteAssignmentPause(),
                    unitPo.getTradeRouteAssignmentStops());
        }
        // A saved processed stop is restored only; loading processes nothing.
        if (unitPo.getTradeRouteVisitRouteId() == null) {
            Preconditions.checkArgument(
                    unitPo.getTradeRouteVisitStopIndex() == null
                            && unitPo.getTradeRouteVisitLocation() == null,
                    "Incomplete saved trade route visit");
        } else {
            Preconditions.checkArgument(
                    unitPo.getTradeRouteVisitStopIndex() != null,
                    "Incomplete saved trade route visit");
            ((AbstractUnit) unit).restoreProcessedTradeRouteVisit(
                    unitPo.getTradeRouteVisitRouteId(),
                    unitPo.getTradeRouteVisitStopIndex(),
                    unitPo.getTradeRouteVisitLocation());
        }
        return unit;
    }

    public Unit createUnit(final UnitCreateRequest unitCreateRequest) {
        final Unit unit = unitResolver.apply(unitCreateRequest);
        if (unit.isAtHighSea()) {
            final org.microcol.model.Location origin = unitCreateRequest
                    .getModel().getHighSea().getDefaultArrivalLocation()
                    .orElseThrow(() -> new IllegalStateException(
                            "No eastern sea-lane arrival is available"));
            ((Ship) unit).rememberDeparture(origin,
                    org.microcol.model.Direction.east);
        }
        return unit;
    }

}
