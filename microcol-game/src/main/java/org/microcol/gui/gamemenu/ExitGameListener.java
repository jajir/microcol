package org.microcol.gui.gamemenu;

import org.microcol.gui.MainPanelPresenter;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.mainmenu.ExitGameController;
import org.microcol.gui.mainmenu.ExitGameEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When user choose to exit game than it show correct show main game menu.
 */
public final class ExitGameListener {

    private final MainPanelPresenter mainPanelPresenter;

    private final GameController gameController;

    @Inject
    ExitGameListener(final ExitGameController exitGameController,
            final MainPanelPresenter mainPanelPresenter, final GameController gameController) {
        this.mainPanelPresenter = Preconditions.checkNotNull(mainPanelPresenter);
        this.gameController = Preconditions.checkNotNull(gameController);
        exitGameController.addListener(this::onExitGame);
    }

    @SuppressWarnings("unused")
    private void onExitGame(final ExitGameEvent event) {
        mainPanelPresenter.showGameMenu();
        gameController.stopGame();
    }

}
