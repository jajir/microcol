package org.microcol.gui.util;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When turn when human player is on turn than it store game state to save file.
 */
public final class TurnStartedListener {

    private final GamePreferences gamePreferences;

    private final GameController gameController;

    private final PersistingTool persistingTool;

    @Inject
    TurnStartedListener(final GamePreferences gamePreferences, final GameController gameController,
            final PersistingTool persistingTool,
            final TurnStartedController turnStartedController) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        turnStartedController.addListener(this::onTurnStarted);
    }

    private void onTurnStarted(final TurnStartedEvent event) {
        if (event.getPlayer().isHuman()) {
            gameController.writeModelToFile(persistingTool.getAutoSaveFile());
            gamePreferences.setGameInProgressSaveFile("true");
        }
    }

}
