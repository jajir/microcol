package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Represents a committed change of native relations or a turn update after
 * which shown native relation text must be read from the model again.
 */
public final class NativeRelationsChangedEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NativeRelationsChangedEvent.class)
                .toString();
    }

}
