package org.microcol.gui.mainmenu;

import com.google.common.base.MoreObjects;

/**
 * Event object for exit game. Exactly it means leave MicroCol application.
 */
public class QuitGameEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(QuitGameEvent.class).toString();
    }

}
