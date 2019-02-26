package org.microcol.gui.screen.turnreport;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * When player is on turn than verify if there are any turn events and when they
 * are than show dialog to show them.
 */
@Listener
public final class ShowTurnEvensOnTurnStartedEvent {

    private final static Logger logger = LoggerFactory
            .getLogger(ShowTurnEvensOnTurnStartedEvent.class);

    private final TurnReportDialog turnReportDialog;

    private final GameModelController gameModelController;

    @Inject
    ShowTurnEvensOnTurnStartedEvent(final TurnReportDialog turnReportDialog,
            final GameModelController gameModelController) {
        this.turnReportDialog = Preconditions.checkNotNull(turnReportDialog);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        if (!isEventsEmpty() && event.isFreshStart() && isCorrectPlayer(event)) {
            logger.debug("Turn started and turn event dialog will be shown.");
            Platform.runLater(() -> turnReportDialog.show());
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