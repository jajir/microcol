package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;

/**
 * Event is send when game start.
 */
public final class GameStartedEvent extends AbstractModelEvent {

    /**
     * Constructor just set model.
     *
     * @param model required game model
     */
    public GameStartedEvent(final Model model) {
        super(model);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
