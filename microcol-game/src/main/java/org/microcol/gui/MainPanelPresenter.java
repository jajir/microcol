package org.microcol.gui;

import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.model.BeforeGameStartEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * MicroCol's main panel. Based on commands and events just change between main
 * game panel and game menu.
 */
@Listener
public final class MainPanelPresenter {

    private final MainPanelView view;

    @Inject
    public MainPanelPresenter(final MainPanelView view, final KeyController keyController) {
        this.view = Preconditions.checkNotNull(view);
        view.getBox().setOnKeyPressed(e -> {
            keyController.fireEvent(e);
        });
    }

    @SuppressWarnings("unused")
    @Subscribe
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
