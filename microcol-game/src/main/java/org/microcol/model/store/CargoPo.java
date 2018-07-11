package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class CargoPo {

    private List<CargoSlotPo> slots = new ArrayList<>();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CargoPo.class).add("slots", slots).toString();
    }

    public boolean containsUnitInCargo(final Integer idUnitInCargo) {
        Preconditions.checkState(idUnitInCargo != null, "IdUnitInCargo is null");
        return slots.stream().filter(slot -> idUnitInCargo.equals(slot.getUnitId())).findAny()
                .isPresent();
    }

    public CargoSlotPo getSlotAt(final int index) {
        if (index < slots.size()) {
            return slots.get(index);
        }
        return null;
    }

    /**
     * @return the slots
     */
    public List<CargoSlotPo> getSlots() {
        return slots;
    }

    /**
     * @param slots
     *            the slots to set
     */
    public void setSlots(List<CargoSlotPo> slots) {
        this.slots = slots;
    }

}
