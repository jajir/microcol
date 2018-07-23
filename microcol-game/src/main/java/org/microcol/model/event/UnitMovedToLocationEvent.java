package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is raised when unit move to map location from other place.
 */
public final class UnitMovedToLocationEvent extends AbstractModelEvent {

    /**
     * Unit which moved to some location.
     */
    private final Unit unit;

    /**
     * Default constructor.
     *
     * @param model
     *            required model
     * @param unit
     *            required moving unit
     */
    public UnitMovedToLocationEvent(final Model model, final Unit unit) {
        super(model);

        this.unit = Preconditions.checkNotNull(unit);
    }

    /**
     * Return moving unit.
     *
     * @return moving unit
     */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("unit", unit).toString();
    }

}
