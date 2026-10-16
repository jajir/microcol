package org.microcol.gui.util;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.model.MicroColException;
import org.microcol.model.TurnPreparationException;
import org.microcol.model.campaign.GameModelDao;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.campaign.po.RecoveryCheckpointPolicy;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;

/**
 * When turn when human player is on turn than it store game state to save file.
 */
@Listener
public final class TurnStartedListener {

    private final GamePreferences gamePreferences;

    private final GameModelDao gameModelDao;

    private final GameModelController gameModelController;

    private final PersistingTool persistingTool;

    @Inject
    TurnStartedListener(final GamePreferences gamePreferences,
            final GameModelDao gameModelDao,
            final GameModelController gameModelController,
            final PersistingTool persistingTool) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.gameModelDao = Preconditions.checkNotNull(gameModelDao);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        final var slots = RecoveryCheckpointPolicy.slotsForTurn(
                event.getModel().getCalendar(), event.isFreshStart(),
                event.getPlayer().isHuman(),
                gamePreferences.isAutosaveEnabled());
        if (!slots.isEmpty()) {
            try {
                gameModelDao.saveRecoverySlots(
                        gameModelController.getGameModel(), persistingTool,
                        slots);
            } catch (TurnPreparationException rejected) {
                showOnFxThread(
                        gameModelController::showTurnPreparationUnavailable);
            } catch (MicroColException failedWrite) {
                showOnFxThread(gameModelController::showRecoverySaveFailure);
            }
        }
    }

    private void showOnFxThread(final Runnable message) {
        if (Platform.isFxApplicationThread()) {
            message.run();
        } else {
            Platform.runLater(message);
        }
    }

}
