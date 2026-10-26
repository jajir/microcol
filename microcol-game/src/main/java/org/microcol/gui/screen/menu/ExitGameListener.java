package org.microcol.gui.screen.menu;

import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * The common leave guard owns save/discard/cancel, including write failures.
 */
@Listener
public final class ExitGameListener {
    private final PersistingService saves;
    private final GameController game;
    private final EventBus events;

    @Inject
    ExitGameListener(final PersistingService saves, final GameController game,
            final EventBus events) {
        this.saves = saves;
        this.game = game;
        this.events = events;
    }

    @Subscribe
    private void onExitGame(final ExitGameEvent event) {
        if (saves.confirmLeave()) {
            game.stopGame();
            events.post(new ShowScreenEvent(Screen.MENU));
        }
    }
}
