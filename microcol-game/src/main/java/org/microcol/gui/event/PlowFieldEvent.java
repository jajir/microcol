package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Front end event. Triggered when player decide to plow field.
 */
public final class PlowFieldEvent {
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlowFieldEvent.class).toString();
    }

}
