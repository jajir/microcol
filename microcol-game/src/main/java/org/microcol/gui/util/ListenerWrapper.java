package org.microcol.gui.util;

import org.microcol.gui.event.Listener;

import com.google.common.base.Preconditions;

/**
 * Wrap event listener and store if listener should be called asynchronously.
 *
 * @param <T>
 *            listener event type
 */
public class ListenerWrapper<T> {

    /**
     * It's <code>true</code> when event listeners will be called asynchronously
     * otherwise it's <code>false</code>.
     */
    private final boolean fireEventsAsynchronously;

    /**
     * Wrapped listener.
     */
    private final Listener<T> listener;

    ListenerWrapper(final boolean fireEventsAsynchronously, final Listener<T> listener) {
        this.fireEventsAsynchronously = fireEventsAsynchronously;
        this.listener = Preconditions.checkNotNull(listener);
    }

    /**
     * It's <code>true</code> when wrapped listener will be executed
     * asynchronously otherwise it's <code>false</code> .
     *
     * @return the fireEventsAsynchronously
     */
    boolean isFireEventsAsynchronously() {
        return fireEventsAsynchronously;
    }

    /**
     * Get wrapped listener.
     *
     * @return the listener
     */
    Listener<T> getListener() {
        return listener;
    }

}
