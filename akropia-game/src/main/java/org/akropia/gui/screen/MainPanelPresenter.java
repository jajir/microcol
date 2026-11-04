package org.akropia.gui.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.event.ChangeLanguageEvent;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.campaign.ScreenCampaign;
import org.akropia.gui.screen.colony.ScreenColony;
import org.akropia.gui.screen.editor.ScreenEditor;
import org.akropia.gui.screen.europe.ScreenEurope;
import org.akropia.gui.screen.game.ScreenGame;
import org.akropia.gui.screen.goals.ScreenGoals;
import org.akropia.gui.screen.colony.DialogTradeRouteCargo;
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
import org.akropia.gui.util.Listener;
import org.akropia.i18n.I18n;
import org.akropia.model.ChainOfCommandStrategy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

/**
 * Akropia's main panel. Based on commands and events just change between main
 * game panel and game menu.
 * <p>
 * WASD control could be in game panel, but sometimes event are not correctly
 * catch by game panel.
 * </p>
 */
@Listener
@Singleton
public final class MainPanelPresenter {

    private final MainPanelView view;

    private final I18n i18n;

    private final ChainOfCommandStrategy<ShowScreenEvent, GameScreen> screenResolver;

    private GameScreen shownScreen;

    /**
     * Screens that opened Settings and Load; <code>null</code> before the first
     * screen.
     */
    private final Map<GameScreen, GameScreen> origins = new HashMap<>();

    private final ScreenGame screenGame;
    private final ScreenMenu screenMenu;
    private final ScreenCampaign screenCampaign;
    private final ScreenFreePlay screenFreePlay;
    private final ScreenSetting screenSetting;
    private final ScreenLoad screenLoad;
    private final ScreenEditor screenEditor;
    private final PersistingService persistingService;
    private final GameModelController gameModelController;

    @Inject
    public MainPanelPresenter(final MainPanelView view, final I18n i18n,
            final ScreenGame screenGame, final ScreenMenu screenMenu,
            final ScreenCampaign screenCampaign,
            final ScreenEurope screenEurope, final ScreenSetting screenSetting,
            final ScreenLoad screenLoad, final ScreenFreePlay screenFreePlay,
            final ScreenColony screenColony,
            final ScreenMarketBuy screenMarketBuy,
            final ScreenMarketSell screenMarketSell,
            final ScreenStatistics screenStatistics,
            final ScreenTurnReport screenTurnReport,
            final ScreenEditor screenEditor, final ScreenGoals screenGoals,
            final ScreenColonizopedia screenHelp,
            final DialogTradeRouteCargo tradeRouteCargo,
            final ScreenCongress screenCongress,
            final PersistingService persistingService,
            final GameModelController gameModelController) {
        this.view = Preconditions.checkNotNull(view);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.screenGame = Preconditions.checkNotNull(screenGame);
        this.screenMenu = Preconditions.checkNotNull(screenMenu);
        this.screenCampaign = Preconditions.checkNotNull(screenCampaign);
        this.screenFreePlay = Preconditions.checkNotNull(screenFreePlay);
        this.screenSetting = Preconditions.checkNotNull(screenSetting);
        this.screenLoad = Preconditions.checkNotNull(screenLoad);
        this.screenEditor = Preconditions.checkNotNull(screenEditor);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);

        /*
         * Wasd controlled is used just in game screen. In game screen wasd
         * controlled doesn't obtain key events.
         */
        screenResolver = new ChainOfCommandStrategy<ShowScreenEvent, GameScreen>(
                Lists.newArrayList(event -> {
                    if (Screen.COLONY == event.getScreen()) {
                        screenColony.setColony(event.getContext());
                        return screenColony;
                    }
                    return null;
                }, event -> {
                    if (Screen.EUROPE == event.getScreen()) {
                        return screenEurope;
                    }
                    return null;
                }, event -> {
                    if (Screen.CAMPAIGN == event.getScreen()) {
                        return screenCampaign;
                    }
                    return null;
                }, event -> {
                    if (Screen.SETTING == event.getScreen()) {
                        return screenSetting;
                    }
                    return null;
                }, event -> {
                    if (Screen.LOAD == event.getScreen()) {
                        return screenLoad;
                    }
                    return null;
                }, event -> {
                    if (Screen.FREE_PLAY_SETUP == event.getScreen()) {
                        return screenFreePlay;
                    }
                    return null;
                }, event -> {
                    if (Screen.MENU == event.getScreen()) {
                        return screenMenu;
                    }
                    return null;
                }, event -> {
                    if (Screen.GAME == event.getScreen()) {
                        return screenGame;
                    }
                    return null;
                }, event -> {
                    if (Screen.MARKET_BUY == event.getScreen()) {
                        screenMarketBuy.init(event.getContext());
                        return screenMarketBuy;
                    }
                    return null;
                }, event -> {
                    if (Screen.MARKET_SELL == event.getScreen()) {
                        screenMarketSell.init(event.getContext());
                        return screenMarketSell;
                    }
                    return null;
                }, event -> {
                    if (Screen.STATISTICS == event.getScreen()) {
                        return screenStatistics;
                    }
                    return null;
                }, event -> {
                    if (Screen.TURN_REPORT == event.getScreen()) {
                        return screenTurnReport;
                    }
                    return null;
                }, event -> {
                    if (Screen.TRADE_ROUTE_CARGO == event.getScreen()) {
                        return tradeRouteCargo;
                    }
                    if (Screen.GOALS == event.getScreen()) {
                        return screenGoals;
                    }
                    if (Screen.CONGRESS == event.getScreen()) {
                        return screenCongress;
                    }
                    if (Screen.HELP == event.getScreen()) {
                        return screenHelp;
                    }
                    return null;
                }, event -> {
                    if (Screen.EDITOR == event.getScreen()) {
                        screenEditor.loadFile(event.getContext());
                        return screenEditor;
                    }
                    return null;
                }));
    }

    @Subscribe
    private void onShowScreen(final ShowScreenEvent event) {
        final GameScreen screen = screenResolver.apply(event);
        if (isOriginTracked(screen)) {
            if (screen == shownScreen) {
                // Reopening keeps the shown page and its first origin.
                return;
            }
            origins.put(screen, shownScreen);
            final boolean menuHosted = isMenuScreen(shownScreen);
            if (screen == screenSetting) {
                screenSetting.setMenuHosted(menuHosted);
            } else {
                screenLoad.setMenuHosted(menuHosted);
                screenLoad.setRecoveryOnly(
                        Boolean.TRUE.equals(event.getContext()));
            }
        }
        show(screen);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onReturnToOrigin(final ReturnToOriginEvent event) {
        if (!isOriginTracked(shownScreen)) {
            return;
        }
        final GameScreen origin = Optional.ofNullable(origins.get(shownScreen))
                .orElse(screenMenu);
        beforeHide(shownScreen);
        origin.beforeReturn();
        view.showScreen(origin);
        shownScreen = origin;
    }

    /**
     * Install the platform shortcut+O and shortcut+S keys of the main window.
     *
     * @param scene required main window scene
     */
    public void installShortcuts(final Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            final boolean blocked = scene
                    .getFocusOwner() instanceof TextInputControl
                    || isOtherWindowShowing(scene.getWindow())
                    || persistingService.isBusy();
            final SessionShortcut.Action action = SessionShortcut.resolve(event,
                    blocked, shownScreen != null && shownScreen != screenEditor,
                    shownScreen == screenGame
                            && gameModelController.isHumanTurnReady());
            if (action == SessionShortcut.Action.LOAD) {
                event.consume();
                persistingService.browse(false, false);
            } else if (action == SessionShortcut.Action.SAVE) {
                event.consume();
                persistingService.browse(true, false);
            }
        });
    }

    /**
     * Dialogs, popup menus and file choosers are separate windows; while one is
     * shown it owns the keys.
     */
    private static boolean isOtherWindowShowing(final Window main) {
        return Window.getWindows().stream()
                .anyMatch(window -> window != main && window.isShowing());
    }

    private boolean isOriginTracked(final GameScreen screen) {
        return screen == screenSetting || screen == screenLoad;
    }

    private boolean isMenuScreen(final GameScreen screen) {
        if (isOriginTracked(screen)) {
            return isMenuScreen(origins.get(screen));
        }
        return screen == null || screen == screenMenu
                || screen == screenCampaign || screen == screenFreePlay;
    }

    private void show(final GameScreen screen) {
        beforeHide(shownScreen);
        beforeShow(screen);
        view.showScreen(screen);
        shownScreen = screen;
    }

    public void stop() {
        beforeHide(shownScreen);
        shownScreen = null;
    }

    private void beforeShow(final GameScreen screen) {
        if (screen == null) {
            return;
        }
        screen.beforeShow();
    }

    private void beforeHide(final GameScreen screen) {
        if (screen == null) {
            return;
        }
        screen.beforeHide();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onChangeLanguage(final ChangeLanguageEvent event) {
        view.updateLanguage(i18n);
    }

}
