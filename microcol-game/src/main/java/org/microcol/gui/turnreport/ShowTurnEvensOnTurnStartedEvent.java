package org.microcol.gui.turnreport;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * When player is on turn than verify if there are any turn events and when they
 * are than show dialog to show them.
 */
public class ShowTurnEvensOnTurnStartedEvent {

    private final TurnReportDialog turnReportDialog;

    private final GameModelController gameModelController;

    @Inject
    ShowTurnEvensOnTurnStartedEvent(final TurnStartedController turnStartedController,
            final TurnReportDialog turnReportDialog,
            final GameModelController gameModelController) {
        turnStartedController.addListener(this::onTurnStarted);
        this.turnReportDialog = Preconditions.checkNotNull(turnReportDialog);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @SuppressWarnings("unused")
    private void onTurnStarted(final TurnStartedEvent event) {
        if (!isEventsEmpty()) {
            Platform.runLater(() -> turnReportDialog.show());
        }
    }

    private boolean isEventsEmpty() {
        return gameModelController.getModel()
                .isTurnEventsMessagesEmpty(gameModelController.getCurrentPlayer());
    }
}
