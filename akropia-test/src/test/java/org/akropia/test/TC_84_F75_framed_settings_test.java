package org.akropia.test;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.gui.screen.setting.ScreenKeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.page.FramedSettingsPage;
import org.akropia.page.GameToolbarPage;
import org.akropia.page.KeyboardShortcutsPage;
import org.akropia.page.DeveloperModePage;
import org.akropia.page.GamePage;
import org.akropia.page.HostedPanelPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.ColonizopediaPage;
import org.akropia.model.Location;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Preserve real Settings actions while applying the chosen frames and rulers.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_84_F75_framed_settings_test extends AbstractAkropiaTest {
    private static final java.util.List<String> SETTINGS_ORDER = java.util.List
            .of("rbCzech", "rbEnglish", "settingGrid", "settingFightAdvisor",
                    "autosaveEnabled", "settingTurnSummary",
                    "settingOpenEuropeOnArrival", "settingVolume",
                    "settingEffectsVolume", "settingAnimationSpeed",
                    "settingMapScrollSpeed", "settingKeyboardShortcuts",
                    "settingDeveloperMode");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_frames_keep_choices_languages_and_mouse_states()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final FramedSettingsPage page = new FramedSettingsPage(getContext());
        page.verifyAppearance();
        page.captureDialog("autosaveEnabled", "framed-settings-initial.png");
        page.verifyChecks(false, true, true);
        // The actual grid checkbox shows hover and press before changing the
        // preference.
        page.hover("settingGrid");
        page.verifyFrameFill("settingGrid", "#efc68f");
        page.holdMouse();
        page.verifyFrameFill("settingGrid", "#c69b6b");
        page.releaseMouse();
        page.verifyChecks(true, true, true);
        page.press("settingFightAdvisor", KeyCode.SPACE);
        page.press("autosaveEnabled", KeyCode.SPACE);
        page.verifyFocused("autosaveEnabled");
        page.verifyChecks(true, false, false);
        // Native radio grouping and translated labels survive repeated skin
        // updates.
        settings.setCzechLanguage();
        page.verifyLanguage(true);
        page.verifyRadioDot("rbCzech", true);
        page.verifyRadioDot("rbEnglish", false);
        page.verifyAppearance();
        page.captureDialog("autosaveEnabled", "framed-settings-czech.png");
        settings.setEnglishLanguage();
        page.verifyLanguage(false);
        page.verifyAppearance();
        page.captureDialog("autosaveEnabled", "framed-settings-english.png");
        // A disabled control stays inert, and leaving Settings retains the
        // values.
        page.setDisabled("settingGrid", true);
        page.click("settingGrid");
        page.verifyChecks(true, false, false);
        page.click("settingGridCaption");
        page.verifyChecks(true, false, false);
        page.captureDialog("autosaveEnabled", "framed-settings-disabled.png");
        page.setDisabled("settingGrid", false);
        settings = settings.goBack().openSetting();
        page.verifyChecks(true, false, false);
        page.verifyAppearance();
        settings.goBack().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_02_rulers_keep_dragging_keyboard_endpoints_and_preferences()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final FramedSettingsPage page = new FramedSettingsPage(getContext());
        // WASD speed keeps its default, endpoints, keyboard steps and saved
        // value.
        page.verifyMapScrollSpeed(2);
        page.press("settingMapScrollSpeed", KeyCode.END);
        page.verifyMapScrollSpeed(9);
        page.press("settingMapScrollSpeed", KeyCode.HOME);
        page.verifyMapScrollSpeed(1);
        page.press("settingMapScrollSpeed", KeyCode.RIGHT);
        page.verifyMapScrollSpeed(2);
        page.dragSlider("settingMapScrollSpeed", 1);
        page.verifyMapScrollSpeed(9);
        settings.goBack().openSetting();
        page.verifyMapScrollSpeed(9);
        // End and Home keep the actual application ranges and stored values.
        page.press("settingVolume", KeyCode.END);
        page.press("settingAnimationSpeed", KeyCode.END);
        page.verifySliders(100, 4);
        page.press("settingVolume", KeyCode.HOME);
        page.press("settingAnimationSpeed", KeyCode.HOME);
        page.verifySliders(0, 0);
        page.press("settingAnimationSpeed", KeyCode.RIGHT);
        // Keep the existing native block increment (10), clamped to the range
        // 0–4.
        page.verifySliders(0, 4);
        page.verifyFocused("settingAnimationSpeed");
        // Drag the native thumb across the ruler; the drawing never intercepts
        // input.
        page.dragSlider("settingAnimationSpeed", 1);
        page.dragSlider("settingVolume", 1);
        page.verifySliders(100, 4);
        page.press("settingAnimationSpeed", KeyCode.HOME);
        page.clickSliderEnd("settingAnimationSpeed");
        page.verifySliders(100, 4);
        page.verifyRulerThumb("settingAnimationSpeed", "#efc68f");
        page.verifyAppearance();
        page.captureDialog("settingVolume",
                "framed-settings-ruler-endpoints.png");
        settings.goBack().openSetting();
        page.verifySliders(100, 4);
    }

    @Test
    @Tag("local")
    void TC_03_developer_checkbox_shortcut_and_translations_stay_synchronized()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        // Explicitly turn off the diagnostic preference used by the shared
        // fixtures.
        developer.setModeInSettings(false);
        developer.verifySettings(false, "Developer mode");
        developer.pressF3();
        developer.verifySettings(true, "Developer mode");
        // A held key toggles exactly once, on release.
        developer.holdF3();
        developer.verifySettings(true, "Developer mode");
        developer.releaseF3();
        developer.verifySettings(false, "Developer mode");
        settings.setCzechLanguage();
        developer.verifySettings(false, "Režim vývojáře");
        new FramedSettingsPage(getContext()).verifyAppearance();
        developer.captureDialog("settingDeveloperMode",
                "developer-settings-czech.png");
        developer.setModeInSettings(true);
        settings.setEnglishLanguage();
        settings.goBack().openSetting();
        developer.verifySettings(true, "Developer mode");
    }

    @Test
    @Tag("local")
    void TC_04_developer_hover_updates_without_mouse_movement_or_game_changes()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.setModeInSettings(false);
        final GamePage game = settings.goBack().loadGame();
        game.waitWhileMoving();
        final Location ship = Location.of(25, 16);
        game.hoverTile(ship);
        final String ordinary = developer.description();
        developer.verifyNoCoordinates();
        developer.verifyNoIndicator();
        developer.rememberGame();
        // F3 refreshes the existing hover and preserves the ordinary
        // description.
        developer.pressF3();
        developer.verifyHover(ship, true, ordinary);
        developer.verifyNoIndicator();
        developer.captureDialog(
                org.akropia.gui.screen.game.components.StatusBarView.STATUS_BAR_LABEL_ID,
                "developer-mode-map.png");
        developer.pressF3();
        developer.verifyHover(ship, false, ordinary);
        developer.verifyNoIndicator();
        developer.verifyGameUnchanged();
        // The in-game Settings checkbox controls the same state.
        developer.openGameSettings();
        developer.verifySettings(false, "Developer mode");
        developer.setModeInSettings(true);
        developer.backToGame();
        game.hoverTile(ship);
        developer.verifyHover(ship, true, ordinary);
        // Toggling diagnostics must not cancel a prepared movement command.
        new FreePlayAuditPage(getContext()).press(KeyCode.M);
        developer.verifyMoveMode(true);
        developer.pressF3();
        developer.verifyMoveMode(true);
        new FreePlayAuditPage(getContext()).press(KeyCode.ESCAPE);
        developer.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_05_developer_coordinates_follow_map_corners_and_respect_dialog_input()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.waitWhileMoving();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.rememberGame();
        // Real camera panning retains the map-file convention at both corners.
        final Location first = Location.of(1, 1);
        game.panToTile(first);
        game.hoverTile(first);
        developer.verifyUnknownTile(first);
        final Location last = Location.of(getModel().getMap().getMaxLocationX(),
                getModel().getMap().getMaxLocationY());
        game.panToTile(last);
        game.hoverTile(last);
        developer.verifyUnknownTile(last);
        developer.leaveMap();
        developer.verifyNoCoordinates();
        // F3 belongs to the map/Settings, not an open modal search field.
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.openWithHKey();
        help.typeSearch("cargo");
        developer.pressF3();
        developer.verifySearchText("cargo");
        developer.verifyState(false);
        help.verifyDialogOpen();
        developer.pressF3();
        developer.verifyState(true);
        developer.verifySearchText("cargo");
        developer.verifyNoIndicator();
        help.verifyDialogOpen();
        help.closeWithEscape();
        help.verifyDialogClosed();
        developer.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_06_developer_preference_survives_application_restart_and_game_load()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        new DeveloperModePage(getContext()).setModeInSettings(false);
        settings.goBack();
        // Recreate the application/injector against the same persisted test
        // profile.
        final Stage restarted = getContext().getPrimaryStage();
        final org.testfx.api.FxRobot robot = getContext().getRobot();
        robot.interact(() -> {
            getAkropia().stop();
            restarted.hide();
        });
        initialize(restarted, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
        getContext().setRobot(robot);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> {
            restarted.toFront();
            restarted.requestFocus();
        });
        final SettingPage reloaded = WelcomePage.of(getContext()).openSetting();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.verifySettings(false, "Developer mode");
        reloaded.goBack().loadGame();
        developer.verifyState(false);
        developer.verifyNoIndicator();
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyNoIndicator();
    }

    @Test
    @Tag("local")
    void TC_07_developer_shortcut_follows_every_screen_and_dialog()
            throws Exception {
        final WelcomePage welcome = WelcomePage.of(getContext());
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        // The welcome page switches the saved preference before a game exists.
        developer.verifyState(true);
        developer.pressF3();
        developer.verifyState(false);
        welcome.verifyMainScreen();
        developer.pressModifiedF3();
        developer.verifyState(false);
        final SettingPage settings = welcome.openSetting();
        developer.verifySettings(false, "Developer mode");
        settings.goBack();
        // Primary-scene text fields keep their content while F3 switches modes.
        final org.akropia.page.FreePlaySetupPage setup = welcome
                .openFreePlaySetup();
        setup.enterName("Map author");
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyText("freePlayPlayerName", "Map author");
        setup.back().verifyMainScreen();
        // The embedded Load page uses the same shortcut as the menu.
        final org.akropia.page.SavedGamesPage saved = new org.akropia.page.SavedGamesPage(
                getContext());
        saved.action("buttonLoad");
        developer.pressF3();
        developer.verifyState(false);
        saved.cancelBrowser();
        welcome.verifyMainScreen();
        final GamePage game = welcome.loadGame();
        game.waitWhileMoving();
        // Compare each F3 press after navigation has set the focused tile.
        final org.akropia.page.EuropePortScreen europe = game.openEuroperPort();
        developer.rememberGame();
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyNoIndicator();
        europe.verifyGold(10000);
        developer.verifyGameUnchanged();
        europe.close();
        final org.akropia.page.ColonyScreen colony = game
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        developer.rememberGame();
        developer.pressF3();
        developer.verifyState(false);
        developer.verifyNoIndicator();
        colony.verifyColonyName("New Amsterdam");
        developer.verifyGameUnchanged();
        colony.close();
        // Re-entering a screen does not install another key handler.
        game.openEuroperPort();
        developer.rememberGame();
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyGameUnchanged();
        europe.close();
        developer.openGameSettings();
        developer.verifySettings(true, "Developer mode");
        developer.backToGame();
        developer.verifyNoIndicator();
        developer.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_08_menu_origin_keeps_settings_in_the_sea_with_a_live_game()
            throws Exception {
        // Keep a loaded game in memory while the main menu is shown.
        WelcomePage.of(getContext()).loadGame();
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        final org.akropia.page.SavedGamesPage saves = new org.akropia.page.SavedGamesPage(
                getContext());
        saves.rememberGame();
        hosted.showMainMenuOverLiveGame();
        // Settings opened from the menu stays in the menu sea, not the game
        // frame.
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        hosted.verifyMenuHosted("Settings", "buttonBack", "Back");
        hosted.verifySettingsOrder(SETTINGS_ORDER);
        // A small window scrolls only the content; title and Back stay.
        new FramedSettingsPage(getContext()).resizeWindow(900, 700);
        new FramedSettingsPage(getContext()).verifyCommonAxis();
        hosted.verifyMenuHosted("Settings", "buttonBack", "Back");
        hosted.verifyScrollBarFollowsOverflow();
        hosted.verifyCompleteTexts();
        hosted.scrollContentToEnd();
        hosted.verifyTitleAndBackVisible("buttonBack");
        hosted.captureDialog("buttonBack",
                "r0377-settings-menu-scrolled-en.png");
        // Czech keeps the same order, full labels and menu return.
        settings.setCzechLanguage();
        hosted.verifyMenuHosted("Nastavení", "buttonBack", "Zpět");
        new FramedSettingsPage(getContext()).verifyCommonAxis();
        hosted.verifySettingsOrder(SETTINGS_ORDER);
        hosted.verifyCompleteTexts();
        hosted.captureDialog("buttonBack", "r0377-settings-menu-cs.png");
        settings.setEnglishLanguage();
        // Escape returns to the menu and leaves the live game untouched.
        hosted.press(KeyCode.ESCAPE);
        WelcomePage.of(getContext()).verifyMainScreen();
        saves.verifyGameUnchanged();
        // Reopening from the menu still uses the sea with its Back control.
        settings = WelcomePage.of(getContext()).openSetting();
        hosted.verifyMenuHosted("Settings", "buttonBack", "Back");
        settings.goBack().verifyMainScreen();
        saves.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_09_music_and_effects_volumes_stay_independent_and_persist()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final FramedSettingsPage page = new FramedSettingsPage(getContext());
        page.verifyVolumeTexts("Music", "Effects", "Silent");
        // Each endpoint changes only its own category.
        page.press("settingEffectsVolume", KeyCode.END);
        page.press("settingVolume", KeyCode.HOME);
        page.verifyVolumes(0, 100, "Silent");
        page.press("settingEffectsVolume", KeyCode.LEFT);
        page.verifyFocused("settingEffectsVolume");
        page.verifyVolumes(0, 90, "Silent");
        // Raising the silent music does not move the effects.
        page.press("settingVolume", KeyCode.RIGHT);
        page.verifyVolumes(10, 90, "10 %");
        page.press("settingEffectsVolume", KeyCode.HOME);
        page.verifyVolumes(10, 0, "10 %");
        page.verifyAppearance();
        page.captureDialog("settingEffectsVolume", "r0227-volumes-en.png");
        // Czech names both categories and the silent state.
        settings.setCzechLanguage();
        page.verifyVolumeTexts("Hudba", "Efekty", "Ticho");
        page.verifyVolumes(10, 0, "10 %");
        page.verifyAppearance();
        page.captureDialog("settingEffectsVolume", "r0227-volumes-cs.png");
        settings.setEnglishLanguage();
        page.verifyVolumeTexts("Music", "Effects", "Silent");
        // Escape and reopening show the saved choices.
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        hosted.press(KeyCode.ESCAPE);
        WelcomePage.of(getContext()).verifyMainScreen();
        settings = WelcomePage.of(getContext()).openSetting();
        page.verifyVolumes(10, 0, "10 %");
        settings.goBack();
        // Recreate the application/injector against the same persisted test
        // profile.
        final Stage restarted = getContext().getPrimaryStage();
        final org.testfx.api.FxRobot robot = getContext().getRobot();
        robot.interact(() -> {
            getAkropia().stop();
            restarted.hide();
        });
        initialize(restarted, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
        getContext().setRobot(robot);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> {
            restarted.toFront();
            restarted.requestFocus();
        });
        final SettingPage reloaded = WelcomePage.of(getContext()).openSetting();
        final FramedSettingsPage reloadedPage = new FramedSettingsPage(
                getContext());
        reloadedPage.verifyVolumes(10, 0, "10 %");
        // In-game Settings shows and changes the same categories.
        reloaded.goBack().loadGame().waitWhileMoving();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.openGameSettings();
        reloadedPage.verifyVolumeTexts("Music", "Effects", "Silent");
        reloadedPage.verifyVolumes(10, 0, "10 %");
        reloadedPage.press("settingEffectsVolume", KeyCode.RIGHT);
        reloadedPage.verifyVolumes(10, 10, "10 %");
        new HostedPanelPage(getContext()).verifyCompleteTexts();
        reloadedPage.captureDialog("settingEffectsVolume",
                "r0227-volumes-game.png");
        developer.backToGame();
        developer.openGameSettings();
        reloadedPage.verifyVolumes(10, 10, "10 %");
        developer.backToGame();
    }

    @Test
    @Tag("local")
    void TC_10_keyboard_shortcuts_capture_conflict_and_apply_after_save()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.waitWhileMoving();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        // In-game Settings opens the editor with the current bindings.
        developer.openGameSettings();
        developer.setModeInSettings(false);
        shortcuts.open();
        shortcuts.verifyEditorOpen(true);
        // The game footer stays fixed while the whole editor body scrolls.
        shortcuts.verifyScrollingLayout(true);
        shortcuts.verifyCompleteLayout();
        shortcuts.verifyBindings(ShortcutAction.HELP, "H");
        shortcuts.verifyStatus("Changes take effect after Save and back.");
        // Global Load cannot navigate away from the unsaved editor.
        shortcuts.pressPlatformShortcut(KeyCode.O);
        shortcuts.verifyEditorOpen(true);
        shortcuts.verifyOnlyMainWindowShowing();
        // Escape cancels a capture without closing the editor.
        shortcuts.startAdd(ShortcutAction.HELP);
        shortcuts.verifyStatus(
                "Press the new combination for Colonizopedia. Escape cancels.");
        shortcuts.verifySaveEnabled(false);
        shortcuts.pressKeys(KeyCode.ESCAPE);
        shortcuts.verifyEditorOpen(true);
        shortcuts.verifyStatus("No combination was changed.");
        shortcuts.verifyFocused(ShortcutAction.HELP, "add");
        shortcuts.verifyBindings(ShortcutAction.HELP, "H");
        // Capturing F3 changes the draft but does not run Developer mode.
        developer.rememberGame();
        shortcuts.startChange(ShortcutAction.HELP, 0);
        shortcuts.pressKeys(KeyCode.F3);
        developer.verifyState(false);
        // F3 is already the global Developer mode key, so saving is blocked.
        shortcuts.verifyBindings(ShortcutAction.HELP, "F3");
        shortcuts.verifyStatus("F3 is used by both Developer mode and"
                + " Colonizopedia. Change one of them before saving.");
        shortcuts.verifySaveEnabled(false);
        // Map actions conflict with each other too.
        shortcuts.startChange(ShortcutAction.HELP, 0);
        shortcuts.pressKeys(KeyCode.K);
        shortcuts.verifyStatus("K is used by both Congress and Colonizopedia."
                + " Change one of them before saving.");
        shortcuts.verifySaveEnabled(false);
        // A free key resolves the conflict; a second binding is kept beside
        // it.
        shortcuts.startChange(ShortcutAction.HELP, 0);
        shortcuts.pressKeys(KeyCode.Y);
        shortcuts.verifyFocused(ShortcutAction.HELP, "binding-0");
        shortcuts.startAdd(ShortcutAction.HELP);
        shortcuts.pressKeys(KeyCode.SHIFT, KeyCode.Y);
        shortcuts.verifyBindings(ShortcutAction.HELP, "Y", "Shift+Y");
        shortcuts.verifyStatus("Changes take effect after Save and back.");
        shortcuts.verifySaveEnabled(true);
        shortcuts.captureDialog(ScreenKeyboardShortcuts.STATUS_ID,
                "r0432-shortcuts-editor-en.png");
        // Save and back stores only the deviation and closes the editor.
        shortcuts.saveAndBack();
        shortcuts.verifyEditorOpen(false);
        shortcuts.verifySavedDeviations(Map.of("map.help", "Y, Shift+Y"));
        developer.verifyGameUnchanged();
        developer.backToGame();
        // The toolbar hint and the map keys change without a restart.
        final GameToolbarPage toolbar = new GameToolbarPage(getContext());
        toolbar.hover("help");
        toolbar.verifyExplanation("Colonizopedia - guide. (Y)", null);
        new FreePlayAuditPage(getContext()).press(KeyCode.H);
        help.verifyDialogClosed();
        new FreePlayAuditPage(getContext()).press(KeyCode.Y);
        help.verifyDialogOpen();
        help.closeWithEscape();
        help.verifyDialogClosed();
        shortcuts.pressKeys(KeyCode.SHIFT, KeyCode.Y);
        help.verifyDialogOpen();
        help.closeWithEscape();
        help.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_11_keyboard_shortcuts_cancel_reset_unbound_and_restart()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        developer.setModeInSettings(false);
        // Cancel discards a removed binding.
        shortcuts.open();
        shortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE, "F3");
        shortcuts.remove(ShortcutAction.DEVELOPER_MODE, 0);
        shortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE);
        shortcuts.verifyFocused(ShortcutAction.DEVELOPER_MODE, "add");
        shortcuts.cancel();
        shortcuts.verifyEditorOpen(false);
        developer.verifySettings(false, "Developer mode");
        shortcuts.verifySavedDeviations(Map.of());
        // Escape also cancels the whole draft.
        shortcuts.open();
        shortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE, "F3");
        shortcuts.remove(ShortcutAction.DEVELOPER_MODE, 0);
        shortcuts.pressKeys(KeyCode.ESCAPE);
        shortcuts.verifyEditorOpen(false);
        // Restore defaults changes only the draft.
        shortcuts.open();
        shortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE, "F3");
        shortcuts.remove(ShortcutAction.DEVELOPER_MODE, 0);
        shortcuts.restoreDefaults();
        shortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE, "F3");
        // An explicitly unbound action is saved and its label updates at
        // once.
        shortcuts.remove(ShortcutAction.DEVELOPER_MODE, 0);
        shortcuts.saveAndBack();
        shortcuts.verifyEditorOpen(false);
        shortcuts.verifySavedDeviations(Map.of("global.developerMode", ""));
        developer.verifySettings(false, "Developer mode");
        developer.pressF3();
        developer.verifyState(false);
        settings.goBack();
        // Recreate the application/injector against the same persisted test
        // profile.
        final Stage restarted = getContext().getPrimaryStage();
        final org.testfx.api.FxRobot robot = getContext().getRobot();
        robot.interact(() -> {
            getAkropia().stop();
            restarted.hide();
        });
        initialize(restarted, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
        getContext().setRobot(robot);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> {
            restarted.toFront();
            restarted.requestFocus();
        });
        // The restart replaced the test context; use pages of the new
        // application, not the stopped one.
        final DeveloperModePage restartedDeveloper = new DeveloperModePage(
                getContext());
        final KeyboardShortcutsPage restartedShortcuts = new KeyboardShortcutsPage(
                getContext());
        settings = WelcomePage.of(getContext()).openSetting();
        restartedShortcuts
                .verifySavedDeviations(Map.of("global.developerMode", ""));
        restartedDeveloper.verifySettings(false, "Developer mode");
        restartedDeveloper.pressF3();
        restartedDeveloper.verifyState(false);
        restartedShortcuts.open();
        restartedShortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE);
        // Restoring and saving the defaults removes every saved deviation.
        restartedShortcuts.restoreDefaults();
        restartedShortcuts.saveAndBack();
        restartedShortcuts.verifySavedDeviations(Map.of());
        restartedDeveloper.verifySettings(false, "Developer mode");
        restartedDeveloper.pressF3();
        restartedDeveloper.verifyState(true);
        settings.goBack().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_12_keyboard_shortcuts_editor_is_complete_in_english_and_czech()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        // A small supported main window keeps the whole editor readable.
        new FramedSettingsPage(getContext()).resizeWindow(1000, 700);
        shortcuts.open();
        shortcuts.verifyEditorTexts("Keyboard shortcuts", "Defaults for Linux",
                "Restore defaults", "Cancel", "Save and back");
        shortcuts.verifyActionTexts(ShortcutAction.HELP, "Colonizopedia",
                "Add");
        shortcuts.verifyBindings(ShortcutAction.SKIP_UNIT, "Space");
        shortcuts.verifyScrollingLayout(false);
        shortcuts.verifyCompleteLayout();
        shortcuts.captureDialog(ScreenKeyboardShortcuts.STATUS_ID,
                "r0432-shortcuts-editor-layout-en.png");
        shortcuts.cancel();
        // Czech names every action, key and button in full.
        settings.setCzechLanguage();
        shortcuts.open();
        shortcuts.verifyEditorTexts("Klávesové zkratky",
                "Výchozí hodnoty pro Linux", "Obnovit výchozí", "Zrušit",
                "Uložit a zpět");
        shortcuts.verifyActionTexts(ShortcutAction.HELP, "Colonizopedie",
                "Přidat");
        shortcuts.verifyActionTexts(ShortcutAction.SECRET_OPTION,
                "Podržet pro vývojářské načtení", "Přidat");
        shortcuts.verifyBindings(ShortcutAction.SKIP_UNIT, "Mezerník");
        shortcuts.verifyStatus("Změny začnou platit po Uložit a zpět.");
        shortcuts.verifyScrollingLayout(false);
        shortcuts.verifyCompleteLayout();
        shortcuts.captureDialog(ScreenKeyboardShortcuts.STATUS_ID,
                "r0432-shortcuts-editor-layout-cs.png");
        // Capture and conflict feedback are Czech too.
        shortcuts.startChange(ShortcutAction.HELP, 0);
        shortcuts.verifyCompleteLayout();
        shortcuts.verifyStatus(
                "Stiskněte novou kombinaci pro Colonizopedie. Escape zruší.");
        shortcuts.pressKeys(KeyCode.K);
        shortcuts.verifyStatus("K používá Kongres i Colonizopedie."
                + " Před uložením jednu z nich změňte.");
        shortcuts.verifyCompleteLayout();
        shortcuts.captureDialog(ScreenKeyboardShortcuts.STATUS_ID,
                "r0432-shortcuts-conflict-cs.png");
        shortcuts.cancel();
        shortcuts.verifySavedDeviations(Map.of());
        settings.setEnglishLanguage();
        settings.goBack().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_13_remapped_developer_letter_leaves_typing_to_text_fields()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        developer.setModeInSettings(false);
        // Give Developer mode the plain letter Y.
        shortcuts.open();
        shortcuts.startChange(ShortcutAction.DEVELOPER_MODE, 0);
        shortcuts.pressKeys(KeyCode.Y);
        shortcuts.verifyBindings(ShortcutAction.DEVELOPER_MODE, "Y");
        developer.verifyState(false);
        shortcuts.saveAndBack();
        developer.verifySettings(false, "Developer mode");
        // Typing Y into a name keeps the letter and leaves the mode alone.
        final WelcomePage welcome = settings.goBack();
        final org.akropia.page.FreePlaySetupPage setup = welcome
                .openFreePlaySetup();
        setup.enterName("Ka");
        shortcuts.pressKeysIn("freePlayPlayerName", KeyCode.Y);
        shortcuts.pressKeysIn("freePlayPlayerName", KeyCode.SHIFT, KeyCode.Y);
        developer.verifyText("freePlayPlayerName", "KayY");
        developer.verifyState(false);
        // Outside the text field the same key switches the mode once.
        setup.back().verifyMainScreen();
        shortcuts.pressKeys(KeyCode.Y);
        developer.verifyState(true);
        // F3 is no longer bound, so it does nothing.
        developer.pressF3();
        developer.verifyState(true);
        // Restore the packaged default for later scenarios.
        settings = welcome.openSetting();
        shortcuts.open();
        shortcuts.restoreDefaults();
        shortcuts.saveAndBack();
        shortcuts.verifySavedDeviations(Map.of());
        developer.verifySettings(true, "Developer mode");
        settings.goBack().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_14_failed_shortcut_save_keeps_draft_and_active_shortcuts()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        shortcuts.open();
        shortcuts.startChange(ShortcutAction.HELP, 0);
        shortcuts.pressKeys(KeyCode.Y);
        // The settings file cannot be written now.
        shortcuts.breakSettingsStorage();
        try {
            shortcuts.saveAndBack();
            shortcuts.verifyEditorOpen(true);
            shortcuts.verifyStatus("Shortcuts could not be saved; your changes"
                    + " are still here and the previous shortcuts stay active."
                    + " Check that the settings folder can be written.");
            shortcuts.verifyBindings(ShortcutAction.HELP, "Y");
            shortcuts.verifyActiveBinding(ShortcutAction.HELP, "H");
            shortcuts.captureDialog(ScreenKeyboardShortcuts.STATUS_ID,
                    "r0432-shortcuts-save-failed-en.png");
        } finally {
            // A failed check must not leave broken storage behind.
            shortcuts.restoreSettingsStorage();
        }
        // Once storage works again, the kept draft is saved and applied.
        shortcuts.saveAndBack();
        shortcuts.verifyEditorOpen(false);
        shortcuts.verifyActiveBinding(ShortcutAction.HELP, "Y");
        shortcuts.verifySavedDeviations(Map.of("map.help", "Y"));
        settings.goBack().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_15_common_axis_keeps_wrapped_captions_and_game_settings_usable()
            throws Exception {
        // Open Settings inside a live game at a small supported window size.
        WelcomePage.of(getContext()).loadGame().waitWhileMoving();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.openGameSettings();
        final FramedSettingsPage page = new FramedSettingsPage(getContext());
        page.resizeWindow(900, 700);
        page.verifyPageBackground(".in-game-page.settings-screen");
        page.verifyPageContours(".in-game-page.settings-screen");
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        shortcuts.open();
        page.verifyPageBackground("#keyboardShortcutsScreen");
        page.verifyPageContours("#keyboardShortcutsScreen");
        // Both contours stay complete when rows scroll and the window grows.
        page.scrollPage("#keyboardShortcutsScreen", 1);
        page.verifyPageContours("#keyboardShortcutsScreen");
        page.resizeWindow(1280, 900);
        page.verifyPageContours("#keyboardShortcutsScreen");
        page.captureDialog("shortcutsTitle", "r0442-shortcuts-paper.png");
        shortcuts.cancel();
        page.verifyPageBackground(".in-game-page.settings-screen");
        page.verifyPageContours(".in-game-page.settings-screen");
        page.resizeWindow(900, 700);
        page.scrollPage(".in-game-page.settings-screen", 1);
        page.verifyPageContours(".in-game-page.settings-screen");
        page.captureDialog("rbCzech", "r0447-settings-contours.png");
        page.scrollPage(".in-game-page.settings-screen", 0);
        page.verifyAppearance();
        page.captureDialog("rbCzech", "r0438-settings-game-en.png");

        // The separate caption still toggles its checkbox; keyboard undoes it.
        page.click("settingGridCaption");
        page.verifyChecks(true, true, true);
        page.press("settingGrid", KeyCode.SPACE);
        page.verifyChecks(false, true, true);

        // Translated long captions wrap on the same axis without changing
        // values.
        page.click("rbCzech");
        page.verifyAppearance();
        page.verifyChecks(false, true, true);
        page.captureDialog("rbCzech", "r0438-settings-game-cs.png");
        page.click("rbEnglish");
        developer.backToGame();
    }

}
