package org.microcol.gui.event;

import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Event is triggered when move is over. It's front-end even.
 */
public final class EndMoveEvent {

    private final Unit movedUnit;

    public EndMoveEvent(final Unit movedUnit) {
        this.movedUnit = Preconditions.checkNotNull(movedUnit);
    }

    /**
     * Return moved unit.
     *
     * @return the movedUnit
     */
    public Unit getMovedUnit() {
        return movedUnit;
    }

}
