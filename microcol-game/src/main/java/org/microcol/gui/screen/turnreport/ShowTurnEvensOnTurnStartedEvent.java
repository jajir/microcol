package org.microcol.gui.screen.turnreport;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When player is on turn than verify if there are any turn events and when they
 * are than show dialog to show them.
 */
@Listener
public final class ShowTurnEvensOnTurnStartedEvent {

    private final static Logger logger = LoggerFactory
            .getLogger(ShowTurnEvensOnTurnStartedEvent.class);

    private final EventBus eventBus;

    private final GameModelController gameModelController;

    @Inject
    ShowTurnEvensOnTurnStartedEvent(final EventBus eventBus,
            final GameModelController gameModelController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        if (!isEventsEmpty() && event.isFreshStart() && isCorrectPlayer(event)) {
            logger.debug("Turn started and turn event dialog will be shown.");
            eventBus.post(new ShowScreenEvent(Screen.TURN_REPORT));
        }
    }

    private boolean isCorrectPlayer(final TurnStartedEvent event) {
        return gameModelController.getCurrentPlayer().equals(event.getPlayer());
    }

    private boolean isEventsEmpty() {
        return gameModelController.getModel()
                .isTurnEventsMessagesEmpty(gameModelController.getCurrentPlayer());
    }
}
