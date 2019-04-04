package org.microcol.model.campaign;

import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Hold context for processing game over event.
 */
final class GameOverProcessingContext {

    private final GameFinishedEvent event;

    private final EventBus eventBus;

    GameOverProcessingContext(final GameFinishedEvent event, final EventBus eventBus) {
        this.event = Preconditions.checkNotNull(event);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    protected void fireEvent(final Object event) {
        getEventBus().post(event);
    }

    private EventBus getEventBus() {
        return eventBus;
    }

    /**
     * @return the event
     */
    public GameFinishedEvent getEvent() {
        return event;
    }

}