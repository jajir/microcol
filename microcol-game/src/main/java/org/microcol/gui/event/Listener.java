package org.microcol.gui.event;

/**
 * Generic event listener.
 * 
 * @param <E>
 *            event object type
 */
public interface Listener<E> {

    /**
     * Method is called when event is raised.
     * 
     * @param event
     *            required event object
     */
    void onEvent(E event);
}
