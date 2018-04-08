package org.microcol.gui;

import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.model.BeforeGameStartController;
import org.microcol.gui.event.model.BeforeGameStartEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * MicroCol's main panel. Based on commands and events just change between main
 * game panel and game menu.
 */
public class MainPanelPresenter {

    private final MainPanelView view;

    @Inject
    public MainPanelPresenter(final MainPanelView view, final KeyController keyController,
            final BeforeGameStartController beforeGameStartController) {
        this.view = Preconditions.checkNotNull(view);
        view.getBox().setOnKeyPressed(e -> {
            keyController.fireEvent(e);
        });
        beforeGameStartController.addListener(this::onBeforeGameStartEvent);
    }

    @SuppressWarnings("unused")
    private void onBeforeGameStartEvent(final BeforeGameStartEvent event) {
        view.showGamePanel();
    }

    public void showDefaultCampaignMenu() {
        view.showDefaultCampaignMenu();
    }

    public void showGamePanel() {
        view.showGamePanel();
    }

    public void showGameMenu() {
        view.showGameMenu();
    }

}
