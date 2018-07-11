package org.microcol.gui.event.model;

import com.google.common.base.MoreObjects;

/**
 * Event is triggered before new mission or any other form game is started.
 */
public final class BeforeGameStartEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
