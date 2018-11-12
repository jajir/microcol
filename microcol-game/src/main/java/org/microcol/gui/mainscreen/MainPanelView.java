package org.microcol.gui.mainscreen;

import org.microcol.gui.colony.ColonyMenuPanel;
import org.microcol.gui.europe.EuropeMenuPanel;
import org.microcol.gui.gamemenu.CampaignMenuPanel;
import org.microcol.gui.gamemenu.GameMenuPanel;
import org.microcol.gui.gamemenu.SettingMenuPanel;
import org.microcol.gui.gamepanel.GamePanelMain;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * MicroCol's main panel. Every game components are in this panel.
 */
public final class MainPanelView implements JavaFxComponent, UpdatableLanguage {

    private final VBox mainBox;

    private final GamePanelMain gamePanelMain;

    private final GameMenuPanel gameMenuPanelView;

    private final CampaignMenuPanel campaignMenuPanel;

    private final ColonyMenuPanel colonyPanel;

    private final EuropeMenuPanel europeMenuPanel;

    private final SettingMenuPanel settingMenuPanel;

    @Inject
    public MainPanelView(final GamePanelMain gamePanelMain, final GameMenuPanel gameMenuPanelView,
            final CampaignMenuPanel campaignMenuPanel, final EuropeMenuPanel europeMenuPanel,
            final SettingMenuPanel settingMenuPanel, final ColonyMenuPanel colonyPanel) {
        mainBox = new VBox();
        this.gamePanelMain = Preconditions.checkNotNull(gamePanelMain);
        this.gameMenuPanelView = Preconditions.checkNotNull(gameMenuPanelView);
        this.campaignMenuPanel = Preconditions.checkNotNull(campaignMenuPanel);
        this.europeMenuPanel = Preconditions.checkNotNull(europeMenuPanel);
        this.settingMenuPanel = Preconditions.checkNotNull(settingMenuPanel);
        this.colonyPanel = Preconditions.checkNotNull(colonyPanel);
        showGameMenu();
    }

    public void showDefaultCampaignMenu() {
        campaignMenuPanel.refresh();
        showBox(campaignMenuPanel.getContent());
    }

    void showGamePanel() {
        showBox(gamePanelMain.getContent());
    }

    void showGameMenu() {
        showBox(gameMenuPanelView.getContent());
    }

    public void showGameSetting() {
        showBox(settingMenuPanel.getContent());
    }

    void showEurope() {
        showBox(europeMenuPanel.getContent());
        europeMenuPanel.repaint();
    }

    public void showColony(final Colony colony) {
        colonyPanel.setColony(colony);
        showBox(colonyPanel.getContent());
    }

    private void showBox(final Region box) {
        mainBox.getChildren().clear();
        mainBox.getChildren().add(box);
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        // mainGamePanelView.
        // campaignMenuPanel
        gamePanelMain.updateLanguage(i18n);
        europeMenuPanel.updateLanguage(i18n);
        settingMenuPanel.updateLanguage(i18n);

    }

}
