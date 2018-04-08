package org.microcol.gui.gamemenu;

import org.microcol.gui.MainPanelPresenter;
import org.microcol.gui.mainmenu.ExitGameController;

import com.google.inject.Inject;

/**
 * When user choose to exit game than it show correct show main game menu.
 */
public class ExitGameListener {

    @Inject
    ExitGameListener(final ExitGameController exitGameController,
            final MainPanelPresenter mainPanelPresenter) {
        exitGameController.addListener(event -> {
            mainPanelPresenter.showGameMenu();
        });
    }

}
