package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Front end event. Triggered when player decide to build a road.
 */
public final class BuildRoadEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(BuildRoadEvent.class).toString();
    }

}
