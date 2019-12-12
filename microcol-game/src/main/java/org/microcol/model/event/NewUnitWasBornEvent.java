package org.microcol.model.event;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Event is raised when new unit born in colony.
 */
public class NewUnitWasBornEvent extends AbstractModelEvent {

    private final Unit unit;

    public NewUnitWasBornEvent(final Model model, final Unit unit) {
        super(model);
        this.unit = Preconditions.checkNotNull(unit);
    }

    public Location getLocation() {
        return unit.getLocation();
    }

    public Unit getUnit() {
        return unit;
    }

}
