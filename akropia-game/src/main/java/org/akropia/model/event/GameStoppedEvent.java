package org.akropia.model.event;

import org.akropia.model.Model;

import com.google.common.base.MoreObjects;

/**
 * Event is send when game is stopped.
 */
public final class GameStoppedEvent extends AbstractModelEvent {

    /**
     * Constructor just set model.
     *
     * @param model required game model
     */
    public GameStoppedEvent(final Model model) {
        super(model);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
