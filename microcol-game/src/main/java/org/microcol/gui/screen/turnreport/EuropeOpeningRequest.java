package org.microcol.gui.screen.turnreport;

import org.microcol.gui.screen.Screen;

import com.google.inject.Singleton;

/**
 * Remembers that the turn report opened at the start of a fresh human turn
 * should be followed by Europe once the reader closes it. The report is never
 * replaced by a second screen change: Europe waits behind it. The request is
 * only for that turn, so leaving the report, reopening it, or Continue cannot
 * open Europe again.
 */
@Singleton
public final class EuropeOpeningRequest {

    private boolean requested;

    /** Europe will be shown after the report that is opened right now. */
    public synchronized void request() {
        requested = true;
    }

    /** Forget any pending request, for example when the report is left. */
    public synchronized void clear() {
        requested = false;
    }

    /**
     * Pick the screen for closing the report and consume the request.
     *
     * @return {@link Screen#EUROPE} once when requested, otherwise the game
     */
    public synchronized Screen takeScreenAfterReport() {
        final Screen next = requested ? Screen.EUROPE : Screen.GAME;
        requested = false;
        return next;
    }

}
