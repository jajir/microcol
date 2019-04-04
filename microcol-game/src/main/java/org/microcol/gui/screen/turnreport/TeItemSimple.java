package org.microcol.gui.screen.turnreport;

import com.google.common.base.Preconditions;

/**
 * Simple turn event item that just hold message.
 */
public class TeItemSimple implements TeItem {

    private final String message;

    TeItemSimple(final String message) {
        this.message = Preconditions.checkNotNull(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
