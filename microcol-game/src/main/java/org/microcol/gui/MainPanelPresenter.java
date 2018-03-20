package org.microcol.gui;

import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.model.BeforeGameStartController;
import org.microcol.gui.event.model.BeforeGameStartEvent;
import org.microcol.gui.event.model.GameFinishedController;
import org.microcol.gui.mainmenu.ExitGameController;
import org.microcol.gui.mainmenu.ExitGameEvent;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * MicroCol's main panel. Based on commands and events just change between main
 * game panel and game menu.
 */
public class MainPanelPresenter {

    public static final String PANEL_GAME_MENU = "Game menu panel";

    public static final String PANEL_MAIN_GAME = "Main game panel";

    public static final String PANEL_CAMPAIGN = "Campaign panel";

    private final MainPanelView view;

    @Inject
    public MainPanelPresenter(final MainPanelView view, final KeyController keyController,
            final ExitGameController exitGameController,
            final BeforeGameStartController beforeGameStartController,
            final GameFinishedController gameFinishedController) {
        this.view = Preconditions.checkNotNull(view);
        view.getBox().setOnKeyPressed(e -> {
            keyController.fireEvent(e);
            System.out.println("\nHura klavesa\n");
        });
        exitGameController.addListener(this::onGameExit);
        beforeGameStartController.addListener(this::onBeforeGameStartEvent);
        gameFinishedController.addListener(this::onGameFinished);
    }

    @SuppressWarnings("unused")
    private void onBeforeGameStartEvent(final BeforeGameStartEvent event) {
        view.showPanel(PANEL_MAIN_GAME);
    }

    @SuppressWarnings("unused")
    private void onGameExit(final ExitGameEvent event) {
        view.showPanel(PANEL_GAME_MENU);
    }

    @SuppressWarnings("unused")
    private void onGameFinished(final GameFinishedEvent event) {
        view.showPanel(PANEL_GAME_MENU);
    }

    // TODO it should not be called directly. Just via listeners.
    // TODO separate game menu both panels to separate parent and switch between them in separate view
    @Deprecated
    public void showPanel(final String panelName) {
        view.showPanel(panelName);
    }

}
