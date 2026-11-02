package org.akropia.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Event object for exit game. Exactly it means leave Akropia application.
 */
public final class QuitGameEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(QuitGameEvent.class).toString();
    }

}
