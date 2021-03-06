package org.microcol.model.unit;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Cargo;
import org.microcol.model.CargoSlot;
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

    public UnitWithCargo(final Function<Unit, Cargo> cargoBuilder, final Model model,
            final Integer id, final Function<Unit, Place> placeBuilder, final Player owner,
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
        return getType().getCargoCapacity() > 0 && getCargo().getSlots().stream()
                .filter(cargoSlot -> cargoSlot.isEmpty()).findAny().isPresent();
    }

    /**
     * Unit can disembark unit from cargo slot to location. Location should be:
     * <ul>
     * <li>No enemy units should be at location.</li>
     * <li>Location should be at neighbor of location of this unit.</li>
     * <li>There should be at least one cargo that could disembark.</li>
     * <li>There should not be colony at location.</li>
     * </ul>
     * 
     * @param targetLocation
     *            required target location
     * @param inCurrentTurn
     *            required if it should be done in current turn
     * @return Return <code>true</code> when unit could disembark at given
     *         location otherwise return <code>false</code>.
     */
    public boolean isPossibleToDisembarkAt(final Location targetLocation, boolean inCurrentTurn) {
        Preconditions.checkNotNull(targetLocation);
        Preconditions.checkState(isAtPlaceLocation(), "Unit %s should by at map.", this);
        return getLocation().isNeighbor(targetLocation)
                && getType().getCargoCapacity() > 0
                && model.getColonyAt(targetLocation).isEmpty()
                && model.getColonyAt(getLocation()).isEmpty()
                && getCargo().getSlots().stream().filter(
                        cargoSlot -> canCargoDisembark(cargoSlot, targetLocation, inCurrentTurn))
                        .findAny().isPresent();
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

    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.noAction);
    }

}
