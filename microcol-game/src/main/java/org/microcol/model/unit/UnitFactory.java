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
                if (UnitType.WAGON == request.getUnitType()) {
                    return makeWagon(request);
                }
                return null;
            }));

    private Unit makeFreeColonist(final UnitCreateRequest request) {
        final UnitFreeColonist out = new UnitFreeColonist(request.getModel(), request.getUnitId(),
                request.getPlaceBuilder(), request.getOwner(), request.getAvailableMoves(),
                request.getAction(), request.getTools(), request.isHoldingGuns(),
                request.isMounted());
        return out;
    }

    private Unit makeGalleon(final UnitCreateRequest request) {
        final UnitGalleon out = new UnitGalleon(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
        return out;
    }

    private Unit makeFrigate(final UnitCreateRequest request) {
        final UnitFrigate out = new UnitFrigate(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
        return out;
    }

    private Unit makeWagon(final UnitCreateRequest request) {
        final UnitWagon out = new UnitWagon(request.getCargoBuilder(), request.getModel(),
                request.getUnitId(), request.getPlaceBuilder(), request.getOwner(),
                request.getAvailableMoves(), request.getAction());
        return out;
    }

    public Unit createUnit(final Model model, final ModelPo modelPo, final UnitPo unitPo) {

        final Function<Unit, Place> placeBuilderModelPo = new PlaceBuilderPo(unitPo, modelPo,
                model);

        final UnitCreateRequest unitCreateRequest = new UnitCreateRequest(model,
                placeBuilderModelPo, unitPo.getId(), unitPo.getType(),
                model.getPlayerStore().getPlayerByName(unitPo.getOwnerId()),
                UnitActionConverter.convert(unitPo.getAction()), unitPo.getAvailableMoves());
        unitCreateRequest.setCargoBuilder(
                unit -> new Cargo(unit, unit.getType().getCargoCapacity(), unitPo.getCargo()));

        return unitResolver.apply(unitCreateRequest);
    }

    public Unit createUnit(final UnitCreateRequest unitCreateRequest) {
        return unitResolver.apply(unitCreateRequest);
    }

}
