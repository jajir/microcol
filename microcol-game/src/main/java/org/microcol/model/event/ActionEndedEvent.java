package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;

/**
 * Event that some action was ended.
 *
 * TODO consider renaming. Action is special operation and this is about unit movement.
 *
 */
public class ActionEndedEvent extends AbstractModelEvent {

    /**
     * Default constructor.
     *
     * @param model
     *            required game model
     */
    public ActionEndedEvent(final Model model) {
        super(model);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
