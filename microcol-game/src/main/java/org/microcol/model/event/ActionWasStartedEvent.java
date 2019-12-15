package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is raised when unit starts same action like plow field.
 */
public class ActionWasStartedEvent extends AbstractModelEvent {

    private final Unit unit;

    /**
     * Default constructor.
     *
     * @param model
     *            required game model
     * @param unit
     *            required unit that started action
     */
    public ActionWasStartedEvent(final Model model, final Unit unit) {
        super(model);
        this.unit = Preconditions.checkNotNull(unit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

    public Unit getUnit() {
        return unit;
    }
}
