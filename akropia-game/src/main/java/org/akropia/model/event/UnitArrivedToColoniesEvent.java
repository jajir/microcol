package org.akropia.model.event;

import org.akropia.model.Model;
import org.akropia.model.unit.Ship;

import com.google.common.base.Preconditions;

/**
 * This event is fired when ship arrives from Europe to colonies.
 */
public class UnitArrivedToColoniesEvent extends AbstractModelEvent {

    private final Ship ship;

    public UnitArrivedToColoniesEvent(final Model model, final Ship ship) {
        super(model);
        this.ship = Preconditions.checkNotNull(ship);
    }

    public Ship getShip() {
        return ship;
    }

}
