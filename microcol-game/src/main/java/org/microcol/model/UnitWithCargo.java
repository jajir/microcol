package org.microcol.model;

import java.util.function.Function;

import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.CargoHolder;
import org.microcol.model.unit.UnitAction;

import com.google.common.base.Preconditions;

/**
 * Abstract class for unit with cargo.
 */
public abstract class UnitWithCargo extends Unit implements CargoHolder {

    private final Cargo cargo;

    public UnitWithCargo(final Function<Unit, Cargo> cargoBuilder, final Model model,
            final Integer id, final Function<Unit, Place> placeBuilder, final UnitType unitType,
            final Player owner, final int actionPoints, final UnitAction unitAction) {
        super(model, id, placeBuilder, unitType, owner, actionPoints, unitAction);
        Preconditions.checkNotNull(cargoBuilder, "CargoBuilder is null");
        this.cargo = Preconditions.checkNotNull(cargoBuilder.apply(this),
                "Cargo builder didn't created cargo");
    }

    @Override
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
        return getType().getCargoCapacity() > 0 && getCargo().getSlots().stream()
                .filter(cargoSlot -> cargoSlot.isEmpty()).findAny().isPresent();
    }

    public boolean isPossibleToDisembarkAt(final Location targetLocation, boolean inCurrentTurn) {
        Preconditions.checkNotNull(targetLocation);
        if (getLocation().isNeighbor(targetLocation) && getType().getCargoCapacity() > 0) {
            return getCargo().getSlots().stream().filter(
                    cargoSlot -> canCargoDisembark(cargoSlot, targetLocation, inCurrentTurn))
                    .findAny().isPresent();
        } else {
            return false;
        }
    }

    private boolean canCargoDisembark(final CargoSlot slot, final Location moveToLocation,
            boolean inCurrentTurn) {
        if (slot.isEmpty() || slot.isLoadedGood()) {
            return false;
        } else {
            final Unit holdedUnit = slot.getUnit().get();
            return (!inCurrentTurn || holdedUnit.getActionPoints() > 0)
                    && holdedUnit.canUnitDisembarkAt(moveToLocation);
        }
    }

}
