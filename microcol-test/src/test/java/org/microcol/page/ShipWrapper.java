package org.microcol.page;

import org.junit.jupiter.api.Assertions;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * Allows to verify state of unit.
 */
public class ShipWrapper {

    private final UnitWithCargo unitWithCargo;

    public ShipWrapper(final UnitWithCargo unitWithCargo) {
        this.unitWithCargo = Preconditions.checkNotNull(unitWithCargo);
    }

    public void verifyNumberOfOccupiedCargoSlots(
            final int expectedNumberOfOccupiedCargoSlots) {
        final int actualCount = (int) unitWithCargo.getCargo().getSlots()
                .stream().filter(slot -> !slot.isEmpty()).count();
        Assertions.assertEquals(expectedNumberOfOccupiedCargoSlots, actualCount,
                String.format(
                        "Ship should have %s occupied cargo slots, but there is %s occupied cargo slots.",
                        expectedNumberOfOccupiedCargoSlots, actualCount));
    }

    public void verifyCargoGoods(final int slotIndex,
            final Goods expectedGoods) {
        final CargoSlot slot = unitWithCargo.getCargo()
                .getSlotByIndex(slotIndex);
        if (expectedGoods.isZero()) {
            Assertions.assertFalse(slot.getGoods().isPresent(),
                    "Cargo slot should be empty");
        } else {
            Assertions.assertTrue(slot.getGoods().isPresent(),
                    "Cargo slot should not be empty");
            Assertions.assertEquals(expectedGoods.getType(),
                    slot.getGoods().get().getType());
            Assertions.assertEquals(expectedGoods.getAmount(),
                    slot.getGoods().get().getAmount());
        }
    }

    public void verifyCargoSlotEmpty(final int slotIndex) {
        Assertions.assertTrue(
                unitWithCargo.getCargo().getSlotByIndex(slotIndex).isEmpty());
    }

    public void verifyCargoOwner(final int slotIndex,
            final Player expectedOwner) {
        Assertions.assertEquals(expectedOwner, unitWithCargo.getCargo()
                .getSlotByIndex(slotIndex).getOwnerPlayer());
    }

    public void verifyLocation(final Location expectedLocation) {
        Assertions.assertEquals(expectedLocation, unitWithCargo.getLocation());
    }

    public void verifyAtHighSea(final boolean expectedAtHighSea) {
        Assertions.assertEquals(expectedAtHighSea, unitWithCargo.isAtHighSea());
    }
}
