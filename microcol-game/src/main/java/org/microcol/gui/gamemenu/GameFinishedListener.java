package org.microcol.gui.gamemenu;

import org.microcol.gui.util.Listener;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When game finished, it set up visibility of free mission.
 */
@Listener
public final class GameFinishedListener {

    private final GameMenuPanelPresenter gameMenuPanelPresenter;

    @Inject
    GameFinishedListener(final GameMenuPanelPresenter gameMenuPanelPresenter) {
        this.gameMenuPanelPresenter = Preconditions.checkNotNull(gameMenuPanelPresenter);
    }

    @Subscribe
    private void onGameFinished(@SuppressWarnings("unused") final GameFinishedEvent event) {
        gameMenuPanelPresenter.refresh();
    }
}
