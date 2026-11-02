package org.akropia.gui.screen;

import java.util.ArrayList;
import java.util.List;

import org.akropia.gui.event.ChangeLanguageEvent;
import org.akropia.gui.screen.campaign.ScreenCampaign;
import org.akropia.gui.screen.colony.ScreenColony;
import org.akropia.gui.screen.editor.ScreenEditor;
import org.akropia.gui.screen.europe.ScreenEurope;
import org.akropia.gui.screen.game.ScreenGame;
import org.akropia.gui.screen.goals.ScreenGoals;
import org.akropia.gui.screen.load.ScreenLoad;
import org.akropia.gui.screen.colonizopedia.ScreenColonizopedia;
import org.akropia.gui.screen.congress.ScreenCongress;
import org.akropia.gui.screen.market.ScreenMarketBuy;
import org.akropia.gui.screen.market.ScreenMarketSell;
import org.akropia.gui.screen.menu.ScreenMenu;
import org.akropia.gui.screen.menu.ScreenFreePlay;
import org.akropia.gui.screen.setting.ScreenSetting;
import org.akropia.gui.screen.statistics.ScreenStatistics;
import org.akropia.gui.screen.turnreport.ScreenTurnReport;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Akropia's main panel. Every game screen is shown at this panel. Class is
 * responsible for propagating change language event to other screens.
 * <p>
 * Here are stored references to all game screens.
 * </p>
 */
public final class MainPanelView implements JavaFxComponent, UpdatableLanguage {

    private final VBox mainBox;

    private List<GameScreen> screens = new ArrayList<>();

    @Inject
    public MainPanelView(final ScreenGame screenGame,
            final ScreenMenu screenMenu, final ScreenCampaign screenCampaign,
            final ScreenEurope screenEurope, final ScreenSetting screenSetting,
            final ScreenLoad screenLoad, final ScreenFreePlay screenFreePlay,
            final ScreenColony screenColony,
            final ScreenMarketBuy screenMarketBuy,
            final ScreenMarketSell screenMarketSell,
            final ScreenStatistics screenStatistics,
            final ScreenTurnReport screenTurnReport,
            final ScreenGoals screenGoals, final ScreenEditor screenEditor,
            final ScreenColonizopedia screenHelp,
            final ScreenCongress screenCongress, final I18n i18n) {
        mainBox = new VBox();
        mainBox.setMinHeight(0);
        VBox.setVgrow(mainBox, javafx.scene.layout.Priority.ALWAYS);
        screens.add(Preconditions.checkNotNull(screenGame));
        screens.add(Preconditions.checkNotNull(screenMenu));
        screens.add(Preconditions.checkNotNull(screenFreePlay));
        screens.add(Preconditions.checkNotNull(screenCampaign));
        screens.add(Preconditions.checkNotNull(screenEurope));
        screens.add(Preconditions.checkNotNull(screenSetting));
        screens.add(Preconditions.checkNotNull(screenLoad));
        screens.add(Preconditions.checkNotNull(screenColony));
        screens.add(Preconditions.checkNotNull(screenMarketBuy));
        screens.add(Preconditions.checkNotNull(screenMarketSell));
        screens.add(Preconditions.checkNotNull(screenStatistics));
        screens.add(Preconditions.checkNotNull(screenTurnReport));
        screens.add(Preconditions.checkNotNull(screenGoals));
        screens.add(Preconditions.checkNotNull(screenHelp));
        screens.add(Preconditions.checkNotNull(screenCongress));
        screens.add(Preconditions.checkNotNull(screenEditor));
        updateLanguage(i18n);
    }

    void showScreen(final GameScreen screen) {
        showBox(screen.getContent());
    }

    private void showBox(final Region box) {
        Platform.runLater(() -> {
            mainBox.getChildren().clear();
            mainBox.getChildren().add(box);
            VBox.setVgrow(box, javafx.scene.layout.Priority.ALWAYS);
            box.setMinHeight(0);
        });
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

    /**
     * Here should be called update in all game screens. It allows from one
     * place to set correct UI language.
     * <p>
     * In most cases is not necessary to listen {@link ChangeLanguageEvent}.
     * </p>
     */
    @Override
    public void updateLanguage(final I18n i18n) {
        screens.forEach(screen -> screen.updateLanguage(i18n));
    }

}
