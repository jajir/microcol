package org.microcol.gui.screen;

import org.microcol.gui.screen.campaign.ScreenCampaign;
import org.microcol.gui.screen.colony.ScreenColony;
import org.microcol.gui.screen.europe.ScreenEurope;
import org.microcol.gui.screen.game.ScreenGame;
import org.microcol.gui.screen.menu.ScreenMenu;
import org.microcol.gui.screen.setting.ScreenSetting;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * MicroCol's main panel. Every game screen is shown at this panel. Class is
 * responsible for propagating change language event to other screens.
 */
public final class MainPanelView implements JavaFxComponent, UpdatableLanguage {

    private final VBox mainBox;

    private final ScreenGame screenGame;

    private final ScreenMenu screenMenu;

    private final ScreenCampaign screenCampaign;

    private final ScreenColony screenColony;

    private final ScreenEurope screenEurope;

    private final ScreenSetting screenSetting;

    @Inject
    public MainPanelView(final ScreenGame screenGame, final ScreenMenu screenMenu,
            final ScreenCampaign screenCampaign, final ScreenEurope screenEurope,
            final ScreenSetting screenSetting, final ScreenColony screenColony) {
        mainBox = new VBox();
        this.screenGame = Preconditions.checkNotNull(screenGame);
        this.screenMenu = Preconditions.checkNotNull(screenMenu);
        this.screenCampaign = Preconditions.checkNotNull(screenCampaign);
        this.screenEurope = Preconditions.checkNotNull(screenEurope);
        this.screenSetting = Preconditions.checkNotNull(screenSetting);
        this.screenColony = Preconditions.checkNotNull(screenColony);
        showSceenMenu();
    }

    public void showScreenCampaign() {
        screenCampaign.refresh();
        showBox(screenCampaign.getContent());
    }

    void showScreenGame() {
        showBox(screenGame.getContent());
    }

    void showSceenMenu() {
        showBox(screenMenu.getContent());
    }

    public void showScreenSetting() {
        showBox(screenSetting.getContent());
    }

    void showScreenEurope() {
        showBox(screenEurope.getContent());
        screenEurope.repaint();
    }

    public void showScreenColony(final Colony colony) {
        screenColony.setColony(colony);
        showBox(screenColony.getContent());
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
        screenGame.updateLanguage(i18n);
        screenEurope.updateLanguage(i18n);
        screenSetting.updateLanguage(i18n);

    }

}
