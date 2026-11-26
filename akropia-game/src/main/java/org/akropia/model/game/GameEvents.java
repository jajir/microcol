package org.akropia.model.game;

import org.akropia.model.ModelListenerAdapter;
import org.akropia.model.ChainOfCommandStrategy;
import org.akropia.model.event.GameFinishedEvent;
import org.akropia.model.event.IndependenceWasDeclaredEvent;
import com.google.common.eventbus.EventBus;

/** Bridges committed game outcomes to presentation events. */
final class GameEvents extends ModelListenerAdapter {
    private final EventBus events;
    private final ChainOfCommandStrategy<GameOverProcessingContext, String> outcomes = new ChainOfCommandStrategy<>(
            java.util.List.of(GameOverProcessors.OBJECTIVE_PROCESSOR,
                    GameOverProcessors.TIME_IS_UP_PROCESSOR,
                    GameOverProcessors.FREE_PLAY_ENDED_PROCESSOR,
                    GameOverProcessors.RETIRED_PROCESSOR,
                    GameOverProcessors.NO_COLONIES_PROCESSOR,
                    GameOverProcessors.REBELLION_LOST_PROCESSOR,
                    GameOverProcessors.REBELLION_WON_PROCESSOR));

    GameEvents(final EventBus events) {
        this.events = java.util.Objects.requireNonNull(events);
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        outcomes.apply(new GameOverProcessingContext(event, events));
    }

    @Override
    public void onIndependenceWasDeclared(
            final IndependenceWasDeclaredEvent event) {
        events.post(new EventShowMessages(
                GameMessages.independenceWasDeclared_caption));
    }
}
