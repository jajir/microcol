package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;

/**
 * Event is send before endTurn is processed on model. It allows to stop it.
 */
public final class BeforeEndTurnEvent extends AbstractStoppableEvent {

    public BeforeEndTurnEvent(final Model model) {
        super(model);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("stopped", isStopped()).toString();
    }

}
