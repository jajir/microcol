package org.microcol.page;

import org.microcol.model.unit.Ship;

import com.google.common.base.Preconditions;

/**
 * Allows to verify state of unit.
 */
public class ShipWrapper {

    private final Ship ship;

    ShipWrapper(final Ship ship) {
	this.ship = Preconditions.checkNotNull(ship);
    }

    public void verifyNumberOfOccupiedCargoSlots(final int expectedNumberOfOccupiedCargoSlots) {
	final int cx = (int) ship.getCargo().getSlots().stream().filter(slot -> !slot.isEmpty()).count();
	if (cx != expectedNumberOfOccupiedCargoSlots) {
	    throw new IllegalStateException(String.format(
		    "Ship should have %s occupied cargo slots, but there is %s occupied cargo slots. It's %s.", 2, cx,
		    ship));
	}
    }

}
