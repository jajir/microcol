package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Event object for exit game. Exactly it means leave currently played game and
 * skip to game menu.
 */
public final class ExitGameEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
