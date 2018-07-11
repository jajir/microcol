package org.microcol.gui.gamemenu;

import org.microcol.gui.event.model.GameFinishedController;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When game finished, it set up visibility of free mission.
 */
public final class GameFinishedListener {

    private final GameMenuPanelPresenter gameMenuPanelPresenter;

    @Inject
    GameFinishedListener(final GameFinishedController gameFinishedController,
            final GameMenuPanelPresenter gameMenuPanelPresenter) {
        this.gameMenuPanelPresenter = Preconditions.checkNotNull(gameMenuPanelPresenter);
        gameFinishedController.addListener(this::onGameFinished);
    }

    @SuppressWarnings("unused")
    private void onGameFinished(final GameFinishedEvent event) {
        gameMenuPanelPresenter.refresh();
    }
}
