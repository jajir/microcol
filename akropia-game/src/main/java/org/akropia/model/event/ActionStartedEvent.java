package org.akropia.model.event;

import org.akropia.model.Model;

import com.google.common.base.MoreObjects;

/**
 * Event that some action was started.
 */
public class ActionStartedEvent extends AbstractModelEvent {

    /**
     * Default constructor.
     *
     * @param model required game model
     */
    public ActionStartedEvent(final Model model) {
        super(model);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
