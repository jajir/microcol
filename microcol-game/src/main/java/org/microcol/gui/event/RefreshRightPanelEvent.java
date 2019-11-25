package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Event should be send when right panel should be repainted because something
 * was changed.
 */
public class RefreshRightPanelEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
