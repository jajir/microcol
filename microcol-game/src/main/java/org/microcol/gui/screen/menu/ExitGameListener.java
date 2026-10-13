package org.microcol.gui.screen.menu;

import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When user choose to exit game than it show correct show main game menu.
 */
@Listener
public final class ExitGameListener {

    private final EventBus eventBus;

    private final GameController gameController;
    private final GameModelController gameModelController;
    private final PersistingTool persistingTool;
    private final GamePreferences gamePreferences;

    @Inject
    ExitGameListener(final EventBus eventBus,
            final GameController gameController,
            final GameModelController gameModelController,
            final PersistingTool persistingTool,
            final GamePreferences gamePreferences) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onExitGame(final ExitGameEvent event) {
        if (gameModelController.isGameModelReady()) {
            gameController.writeModelToFile(persistingTool.getAutoSaveFile());
            gamePreferences.setGameInProgressSaveFile("true");
        }
        eventBus.post(new ShowScreenEvent(Screen.MENU));
        gameController.stopGame();
    }

}
