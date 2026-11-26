package org.akropia.gui.screen.menu;

import org.akropia.gui.util.Listener;
import org.akropia.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Refreshes available main-menu actions after a game finishes.
 */
@Listener
public final class GameFinishedListener {

    private final ButtonsPanelPresenter gameMenuPanelPresenter;

    @Inject
    GameFinishedListener(final ButtonsPanelPresenter gameMenuPanelPresenter) {
        this.gameMenuPanelPresenter = Preconditions
                .checkNotNull(gameMenuPanelPresenter);
    }

    @Subscribe
    private void onGameFinished(
            @SuppressWarnings("unused") final GameFinishedEvent event) {
        gameMenuPanelPresenter.refresh();
    }
}
