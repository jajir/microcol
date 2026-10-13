package org.microcol.model.unit;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Cargo;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Abstract class for unit with cargo.
 */
public abstract class UnitWithCargo extends AbstractUnit {

    private final Cargo cargo;

    public UnitWithCargo(final Function<Unit, Cargo> cargoBuilder,
            final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int actionPoints, final UnitAction unitAction) {
        super(model, id, placeBuilder, owner, actionPoints, unitAction);
        Preconditions.checkNotNull(cargoBuilder, "CargoBuilder is null");
        this.cargo = Preconditions.checkNotNull(cargoBuilder.apply(this),
                "Cargo builder didn't created cargo");
    }

    public Cargo getCargo() {
        return cargo;
    }

    @Override
    public UnitPo save() {
        UnitPo unitPo = super.save();
        unitPo.setCargo(cargo.save());
        return unitPo;
    }

    /**
     * Return true when given unit have free cargo slot for unit.
     *
     * @return return <code>true</code> when at least one cargo slot is empty
     *         and could be loaded otherwise return <code>false</code>
     */
    public boolean isAtLeastOneCargoSlotEmpty() {
        return getType().getCargoCapacity() > 0 && getCargo().getSlots()
                .stream().filter(cargoSlot -> cargoSlot.isEmpty()).findAny()
                .isPresent();
    }

    /**
     * Check whether at least one passenger can land through the shared rules.
     *
     * @param targetLocation required destination
     * @param inCurrentTurn  whether passenger movement points are required
     * @return whether an eligible passenger is aboard
     */
    public boolean isPossibleToDisembarkAt(final Location targetLocation,
            boolean inCurrentTurn) {
        Preconditions.checkNotNull(targetLocation);
        return isShip() && isAtPlaceLocation() && getCargo().getSlots().stream()
                .flatMap(slot -> slot.getUnit().stream())
                .anyMatch(passenger -> passenger
                        .canUnitDisembarkAt(targetLocation, inCurrentTurn));
    }

    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.noAction);
    }

}
