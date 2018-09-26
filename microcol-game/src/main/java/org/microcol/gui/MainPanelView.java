package org.microcol.gui;

import org.microcol.gui.gamemenu.CampaignPanelView;
import org.microcol.gui.gamemenu.GameMenuPanelView;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.VBox;

/**
 * MicroCol's main panel. Every game components are in this panel.
 */
public final class MainPanelView {

    private final VBox mainBox;

    private final MainGamePanelView mainGamePanelView;

    private final GameMenuPanelView gameMenuPanelView;

    private final CampaignPanelView campaignPanelView;

    @Inject
    public MainPanelView(final MainGamePanelView mainGamePanelView,
            final GameMenuPanelView gameMenuPanelView, final CampaignPanelView campaignPanelView) {
        mainBox = new VBox();
        this.mainGamePanelView = Preconditions.checkNotNull(mainGamePanelView);
        this.gameMenuPanelView = Preconditions.checkNotNull(gameMenuPanelView);
        this.campaignPanelView = Preconditions.checkNotNull(campaignPanelView);
        showGameMenu();
    }

    public void showDefaultCampaignMenu() {
        campaignPanelView.refresh();
        showBox(campaignPanelView.getBox());
    }

    public void showGamePanel() {
        showBox(mainGamePanelView.getBox());
    }

    public void showGameMenu() {
        showBox(gameMenuPanelView.getPanel());
    }

    private void showBox(final VBox box) {
        mainBox.getChildren().clear();
        mainBox.getChildren().add(box);
    }

    public VBox getBox() {
        return mainBox;
    }

}
