package org.microcol.gui;

import org.microcol.gui.gamemenu.CampaignPanelView;
import org.microcol.gui.gamemenu.GameMenuPanelView;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.VBox;

/**
 * MicroCol's main panel. Every game components are in this panel.
 */
public class MainPanelView {

    private final static String DEFAULT_PANEL_NAME = MainPanelPresenter.PANEL_GAME_MENU;

    private final VBox box;

    private final MainGamePanelView mainGamePanelView;

    private final GameMenuPanelView gameMenuPanelView;

    private final CampaignPanelView campaignPanelView;

    @Inject
    public MainPanelView(final MainGamePanelView mainGamePanelView,
            final GameMenuPanelView gameMenuPanelView, final CampaignPanelView campaignPanelView) {
        box = new VBox();
        this.mainGamePanelView = Preconditions.checkNotNull(mainGamePanelView);
        this.gameMenuPanelView = Preconditions.checkNotNull(gameMenuPanelView);
        this.campaignPanelView = Preconditions.checkNotNull(campaignPanelView);
        showPanel(DEFAULT_PANEL_NAME);
    }

    public void showPanel(final String panelName) {
        box.getChildren().clear();
        if (MainPanelPresenter.PANEL_MAIN_GAME.equals(panelName)) {
            box.getChildren().add(mainGamePanelView.getBox());
        } else if (MainPanelPresenter.PANEL_GAME_MENU.equals(panelName)) {
            box.getChildren().add(gameMenuPanelView.getBox());
        } else if (MainPanelPresenter.PANEL_CAMPAIGN.equals(panelName)) {
            campaignPanelView.refresh();
            box.getChildren().add(campaignPanelView.getBox());
        } else {
            throw new IllegalArgumentException(String.format("Invalid panel name (%s)", panelName));
        }
    }

    public VBox getBox() {
        return box;
    }

}
