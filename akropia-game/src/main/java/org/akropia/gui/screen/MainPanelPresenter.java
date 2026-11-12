package org.akropia.gui.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.dialog.SavedGamesDialog;
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

import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;

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
    private final SavedGamesDialog screenSave;
    private final ScreenEditor screenEditor;
    private final PersistingService persistingService;
    private final GameModelController gameModelController;

    private final KeyboardShortcuts shortcuts;

    private final org.akropia.gui.preferences.DeveloperMode developerMode;

    private final com.google.common.eventbus.EventBus eventBus;

    @Inject
    public MainPanelPresenter(final MainPanelView view, final I18n i18n,
            final ScreenGame screenGame, final ScreenMenu screenMenu,
            final ScreenCampaign screenCampaign,
            final ScreenEurope screenEurope, final ScreenSetting screenSetting,
            final ScreenLoad screenLoad, final SavedGamesDialog screenSave,
            final ScreenFreePlay screenFreePlay,
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
            final GameModelController gameModelController,
            final KeyboardShortcuts shortcuts,
            final com.google.common.eventbus.EventBus eventBus,
            final org.akropia.gui.preferences.DeveloperMode developerMode) {
        this.developerMode = Preconditions.checkNotNull(developerMode);
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.view = Preconditions.checkNotNull(view);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.screenGame = Preconditions.checkNotNull(screenGame);
        this.screenMenu = Preconditions.checkNotNull(screenMenu);
        this.screenCampaign = Preconditions.checkNotNull(screenCampaign);
        this.screenFreePlay = Preconditions.checkNotNull(screenFreePlay);
        this.screenSetting = Preconditions.checkNotNull(screenSetting);
        this.screenLoad = Preconditions.checkNotNull(screenLoad);
        this.screenSave = Preconditions.checkNotNull(screenSave);
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
                    if (Screen.SAVE == event.getScreen()) {
                        return screenSave;
                    }
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
                        if (event.getContext() != null) {
                            screenEditor.loadFile(event.getContext());
                        }
                        return screenEditor;
                    }
                    return null;
                }));
    }

    public void showSaveScreen() {
        onShowScreen(new ShowScreenEvent(Screen.SAVE));
    }

    public void closeSaveScreen() {
        if (shownScreen == screenSave) {
            onReturnToOrigin(new ReturnToOriginEvent());
        }
    }

    @Subscribe
    private void onShowScreen(final ShowScreenEvent event) {
        if (Screen.EDITOR == event.getScreen() && !developerMode.isEnabled()) {
            // The map editor exists only in developer mode (R-0435).
            return;
        }
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
            } else if (screen == screenLoad) {
                screenLoad.setMenuHosted(menuHosted);
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
        final boolean returningFromSave = shownScreen == screenSave;
        final GameScreen previous = shownScreen;
        if (!returningFromSave) {
            beforeHide(previous);
        }
        shownScreen = origin;
        if (returningFromSave) {
            // Save-before-leaving may replace the game as soon as its nested
            // choice returns. Refresh only if that decision kept this screen.
            javafx.application.Platform.runLater(() -> {
                if (shownScreen == origin) {
                    origin.beforeReturn();
                    view.showScreen(origin);
                }
            });
        } else {
            origin.beforeReturn();
            view.showScreen(origin);
        }
        if (returningFromSave) {
            // Register the return before releasing the nested save choice loop.
            beforeHide(previous);
        }
    }

    /**
     * Install the configurable Load and Save keys of the main window.
     *
     * @param scene required main window scene
     */
    public void installShortcuts(final Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            final boolean blocked = scene
                    .getFocusOwner() instanceof TextInputControl
                    || shownScreen == screenSetting
                            && screenSetting.isEditingShortcuts()
                    || isOtherWindowShowing(scene.getWindow())
                    || persistingService.isBusy();
            final SessionShortcut.Action action = SessionShortcut.resolve(
                    shortcuts, event, blocked,
                    shownScreen != null && shownScreen != screenEditor,
                    shownScreen == screenGame
                            && gameModelController.isHumanTurnReady());
            if (action == SessionShortcut.Action.LOAD) {
                event.consume();
                persistingService.browse(false);
            } else if (action == SessionShortcut.Action.SAVE) {
                event.consume();
                persistingService.browse(true);
            } else if (!blocked && isSessionMenuScreen()) {
                // The same commands as the menu's and the map's Settings and
                // Quit, including the existing unsaved-game protection.
                if (shortcuts.matches(ShortcutAction.OPEN_SETTINGS, event)) {
                    event.consume();
                    eventBus.post(new ShowScreenEvent(Screen.SETTING));
                } else if (shortcuts.matches(ShortcutAction.QUIT_GAME, event)) {
                    event.consume();
                    eventBus.post(new org.akropia.gui.event.QuitGameEvent());
                }
            }
        });
    }

    /**
     * Settings and Quit are offered by the main menu and the map's session
     * menu.
     */
    private boolean isSessionMenuScreen() {
        return shownScreen == screenMenu || shownScreen == screenGame
                && gameModelController.isHumanTurnReady();
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
        return screen == screenSetting || screen == screenLoad
                || screen == screenSave;
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
