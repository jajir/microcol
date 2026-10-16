package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.PlaceBuilderPo;
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
                        .contains(request.getUnitType())) {
                    return makeSpecialist(request);
                }
                return null;
            }));

    private Unit makeFreeColonist(final UnitCreateRequest request) {
        final UnitFreeColonist out = new UnitFreeColonist(request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getAvailableMoves(),
                request.getAction(), request.getTools(),
                request.isHoldingGuns(), request.isMounted());
        return out;
    }

    private Unit makeMasterBlacksmith(final UnitCreateRequest request) {
        return new UnitMasterBlacksmith(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction(),
                request.getTools(), request.isHoldingGuns(),
                request.isMounted());
    }

    private Unit makeSpecialist(final UnitCreateRequest request) {
        return new UnitSpecialist(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction(),
                request.getTools(), request.isHoldingGuns(),
                request.isMounted(), request.getUnitType());
    }

    private Unit makeGalleon(final UnitCreateRequest request) {
        final UnitGalleon out = new UnitGalleon(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
        return out;
    }

    private Unit makeFrigate(final UnitCreateRequest request) {
        final UnitFrigate out = new UnitFrigate(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
        return out;
    }

    private Unit makeCaravel(final UnitCreateRequest request) {
        return new UnitCaravel(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getAvailableMoves(),
                request.getAction());
    }

    private Unit makeMerchantman(final UnitCreateRequest request) {
        return new UnitMerchantman(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
    }

    private Unit makePrivateer(final UnitCreateRequest request) {
        return new UnitPrivateer(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getAvailableMoves(),
                request.getAction());
    }

    private Unit makeManOfWar(final UnitCreateRequest request) {
        return new UnitManOfWar(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(),
                request.getOwner(), request.getAvailableMoves(),
                request.getAction());
    }

    private Unit makeWagon(final UnitCreateRequest request) {
        final UnitWagon out = new UnitWagon(request.getCargoBuilder(),
                request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
        return out;
    }

    public Unit createUnit(final Model model, final ModelPo modelPo,
            final UnitPo unitPo) {

        final Function<Unit, Place> placeBuilderModelPo = new PlaceBuilderPo(
                unitPo, modelPo, model);

        final UnitCreateRequest unitCreateRequest = new UnitCreateRequest(model,
                placeBuilderModelPo, unitPo.getId(), unitPo.getType(),
                model.getPlayerStore().getPlayerByName(unitPo.getOwnerId()),
                UnitActionConverter.convert(unitPo.getAction()),
                unitPo.getAvailableMoves());
        unitCreateRequest.setCargoBuilder(unit -> new Cargo(unit,
                unit.getType().getCargoCapacity(), unitPo.getCargo()));
        unitCreateRequest.setHoldingGuns(unitPo.isHoldingGuns());
        unitCreateRequest.setMounted(unitPo.isMounted());
        unitCreateRequest.setTools(unitPo.getTools());

        final Unit unit = unitResolver.apply(unitCreateRequest);
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
