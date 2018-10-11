package org.microcol.gui;

import org.microcol.gui.europe.EuropeScrollPanel;
import org.microcol.gui.gamemenu.CampaignMenuPanelView;
import org.microcol.gui.gamemenu.GameMenuPanelView;
import org.microcol.gui.util.JavaFxComponent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * MicroCol's main panel. Every game components are in this panel.
 */
public final class MainPanelView implements JavaFxComponent {

    private final VBox mainBox;

    private final MainGamePanelView mainGamePanelView;

    private final GameMenuPanelView gameMenuPanelView;

    private final CampaignMenuPanelView campaignMenuPanelView;

    private final EuropeScrollPanel europeScrollPanel;

    @Inject
    public MainPanelView(final MainGamePanelView mainGamePanelView,
            final GameMenuPanelView gameMenuPanelView,
            final CampaignMenuPanelView campaignMenuPanelView,
            final EuropeScrollPanel europeScrollPanel) {
        mainBox = new VBox();
        this.mainGamePanelView = Preconditions.checkNotNull(mainGamePanelView);
        this.gameMenuPanelView = Preconditions.checkNotNull(gameMenuPanelView);
        this.campaignMenuPanelView = Preconditions.checkNotNull(campaignMenuPanelView);
        this.europeScrollPanel = Preconditions.checkNotNull(europeScrollPanel);
        showGameMenu();
    }

    public void showDefaultCampaignMenu() {
        campaignMenuPanelView.refresh();
        showBox(campaignMenuPanelView.getContent());
    }

    public void showGamePanel() {
        showBox(mainGamePanelView.getContent());
    }

    public void showGameMenu() {
        showBox(gameMenuPanelView.getContent());
    }

    public void showEurope() {
        showBox(europeScrollPanel.getContent());
    }

    private void showBox(final Region box) {
        mainBox.getChildren().clear();
        mainBox.getChildren().add(box);
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

    public VBox getBox() {
        return mainBox;
    }

}
