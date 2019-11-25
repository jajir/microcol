package org.microcol.gui.screen;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.screen.campaign.ScreenCampaign;
import org.microcol.gui.screen.colony.ScreenColony;
import org.microcol.gui.screen.editor.ScreenEditor;
import org.microcol.gui.screen.europe.ScreenEurope;
import org.microcol.gui.screen.game.ScreenGame;
import org.microcol.gui.screen.goals.ScreenGoals;
import org.microcol.gui.screen.market.ScreenMarketBuy;
import org.microcol.gui.screen.market.ScreenMarketSell;
import org.microcol.gui.screen.menu.ScreenMenu;
import org.microcol.gui.screen.setting.ScreenSetting;
import org.microcol.gui.screen.statistics.ScreenStatistics;
import org.microcol.gui.screen.turnreport.ScreenTurnReport;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * MicroCol's main panel. Every game screen is shown at this panel. Class is
 * responsible for propagating change language event to other screens.
 * <p>
 * Here are stored references to all game screens.
 * </p>
 */
public final class MainPanelView implements JavaFxComponent, UpdatableLanguage {

    private final VBox mainBox;

    private List<GameScreen> screens = new ArrayList<>();

    @Inject
    public MainPanelView(final ScreenGame screenGame, final ScreenMenu screenMenu,
            final ScreenCampaign screenCampaign, final ScreenEurope screenEurope,
            final ScreenSetting screenSetting, final ScreenColony screenColony,
            final ScreenMarketBuy screenMarketBuy, final ScreenMarketSell screenMarketSell,
            final ScreenStatistics screenStatistics, final ScreenTurnReport screenTurnReport,
            final ScreenGoals screenGoals, final ScreenEditor screenEditor, final I18n i18n) {
        mainBox = new VBox();
        screens.add(Preconditions.checkNotNull(screenGame));
        screens.add(Preconditions.checkNotNull(screenMenu));
        screens.add(Preconditions.checkNotNull(screenCampaign));
        screens.add(Preconditions.checkNotNull(screenEurope));
        screens.add(Preconditions.checkNotNull(screenSetting));
        screens.add(Preconditions.checkNotNull(screenColony));
        screens.add(Preconditions.checkNotNull(screenMarketBuy));
        screens.add(Preconditions.checkNotNull(screenMarketSell));
        screens.add(Preconditions.checkNotNull(screenStatistics));
        screens.add(Preconditions.checkNotNull(screenTurnReport));
        screens.add(Preconditions.checkNotNull(screenGoals));
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
