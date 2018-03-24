package org.microcol.model.event;

/**
 * When event class implement it than it could be stopped.
 *
 */
public interface StoppableEvent {

    /**
     * When it's called than event processing will be stopped.
     */
    void stopEventExecution();

}
