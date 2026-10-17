package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Front end event. Triggered when player decides to clear a forest.
 */
public final class ClearForestEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ClearForestEvent.class).toString();
    }

}
