package org.microcol.gui.screen.menu;

import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.model.GameController;
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

    @Inject
    ExitGameListener(final EventBus eventBus, final GameController gameController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameController = Preconditions.checkNotNull(gameController);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onExitGame(final ExitGameEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.MENU));
        gameController.stopGame();
    }

}
