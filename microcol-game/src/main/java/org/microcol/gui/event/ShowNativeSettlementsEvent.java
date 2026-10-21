package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Represents event when user wants to see details of known native settlements.
 */
public final class ShowNativeSettlementsEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ShowNativeSettlementsEvent.class)
                .toString();
    }

}
