package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.unit.Ship;

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
