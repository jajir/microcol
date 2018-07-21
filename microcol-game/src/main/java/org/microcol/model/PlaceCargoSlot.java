package org.microcol.model;

import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Describe state when unit is stored in cargo slot of other unit.
 */
public final class PlaceCargoSlot extends AbstractPlace {

    private final CargoSlot cargoSlot;

    /**
     * Default constructor.
     *
     * @param unitToCargo
     *            required unti stored int cargo slot
     * @param cargoSlot
     *            required cargo slot where is unti stored
     */
    PlaceCargoSlot(final Unit unitToCargo, final CargoSlot cargoSlot) {
        super(unitToCargo);
        this.cargoSlot = Preconditions.checkNotNull(cargoSlot);
        cargoSlot.unsafeStore(this);
    }

    /**
     * Get player owning cargo slot.
     *
     * @return cargo slot owner
     */
    public Player getCargoSlotOwner() {
        return cargoSlot.getOwnerPlayer();
    }

    @Override
    public void destroy() {
        cargoSlot.empty();
    }

    @Override
    public String getName() {
        return "Cargo slot";
    }

    /**
     * Get cargo slot where is unit stored.
     *
     * @return cargo slot object
     */
    public CargoSlot getCargoSlot() {
        return cargoSlot;
    }

    /**
     * Get unit where this cargo slot belongs.
     *
     * @return return owner unit
     */
    public Unit getOwnerUnit() {
        return cargoSlot.getOwnerUnit();
    }

    /**
     * Get information if unit owning cargo slot is in Europe port.
     *
     * @return return <code>true</code> when unit owning cargo slot is in Europe
     *         port otherwise return <code>false</code>
     */
    public boolean isOwnerAtEuropePort() {
        return cargoSlot.getHold().getOwner().isAtEuropePort();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceCargoSlot.class).add("unit id", getUnit().getId())
                .add("owner", cargoSlot.getOwnerPlayer()).toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceCargoSlotPo placeCargoSlotPo = new PlaceCargoSlotPo();
        unitPo.setPlaceCargoSlot(placeCargoSlotPo);
        return placeCargoSlotPo;
    }

}
