package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.FramedSettingsPage;
import org.microcol.page.DeveloperModePage;
import org.microcol.page.GamePage;
import org.microcol.page.HostedPanelPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.ColonizopediaPage;
import org.microcol.model.Location;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Preserve real Settings actions while applying the chosen frames and rulers.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_84_F75_framed_settings_test extends AbstractMicroColTest {
    private static final java.util.List<String> SETTINGS_ORDER = java.util.List
            .of("rbCzech", "rbEnglish", "settingGrid", "settingFightAdvisor",
                    "autosaveEnabled", "settingTurnSummary",
                    "settingOpenEuropeOnArrival", "settingVolume",
                    "settingEffectsVolume", "settingAnimationSpeed",
                    "settingDeveloperMode");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.microcol"));
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
        developer.verifySettings(false, "Developer mode (F3)");
        developer.pressF3();
        developer.verifySettings(true, "Developer mode (F3)");
        // A held key toggles exactly once, on release.
        developer.holdF3();
        developer.verifySettings(true, "Developer mode (F3)");
        developer.releaseF3();
        developer.verifySettings(false, "Developer mode (F3)");
        settings.setCzechLanguage();
        developer.verifySettings(false, "Režim vývojáře (F3)");
        new FramedSettingsPage(getContext()).verifyAppearance();
        developer.captureDialog("settingDeveloperMode",
                "developer-settings-czech.png");
        developer.setModeInSettings(true);
        settings.setEnglishLanguage();
        settings.goBack().openSetting();
        developer.verifySettings(true, "Developer mode (F3)");
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
                org.microcol.gui.screen.game.components.StatusBarView.STATUS_BAR_LABEL_ID,
                "developer-mode-map.png");
        developer.pressF3();
        developer.verifyHover(ship, false, ordinary);
        developer.verifyNoIndicator();
        developer.verifyGameUnchanged();
        // The in-game Settings checkbox controls the same state.
        developer.openGameSettings();
        developer.verifySettings(false, "Developer mode (F3)");
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
            getMicroCol().stop();
            restarted.hide();
        });
        initialize(restarted, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.microcol"));
        getContext().setRobot(robot);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> {
            restarted.toFront();
            restarted.requestFocus();
        });
        final SettingPage reloaded = WelcomePage.of(getContext()).openSetting();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.verifySettings(false, "Developer mode (F3)");
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
        developer.verifySettings(false, "Developer mode (F3)");
        settings.goBack();
        // Primary-scene text fields keep their content while F3 switches modes.
        final org.microcol.page.FreePlaySetupPage setup = welcome
                .openFreePlaySetup();
        setup.enterName("Map author");
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyText("freePlayPlayerName", "Map author");
        setup.back().verifyMainScreen();
        // The embedded Load page uses the same shortcut as the menu.
        final org.microcol.page.SavedGamesPage saved = new org.microcol.page.SavedGamesPage(
                getContext());
        saved.action("buttonLoad");
        developer.pressF3();
        developer.verifyState(false);
        saved.cancelBrowser();
        welcome.verifyMainScreen();
        final GamePage game = welcome.loadGame();
        game.waitWhileMoving();
        // Compare each F3 press after navigation has set the focused tile.
        final org.microcol.page.EuropePortScreen europe = game
                .openEuroperPort();
        developer.rememberGame();
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyNoIndicator();
        europe.verifyGold(10000);
        developer.verifyGameUnchanged();
        europe.close();
        final org.microcol.page.ColonyScreen colony = game
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
        developer.verifySettings(true, "Developer mode (F3)");
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
        final org.microcol.page.SavedGamesPage saves = new org.microcol.page.SavedGamesPage(
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
        page.verifyVolumes(0, 100, "Silent", "100 %");
        page.press("settingEffectsVolume", KeyCode.LEFT);
        page.verifyFocused("settingEffectsVolume");
        page.verifyVolumes(0, 90, "Silent", "90 %");
        // Raising the silent music does not move the effects.
        page.press("settingVolume", KeyCode.RIGHT);
        page.verifyVolumes(10, 90, "10 %", "90 %");
        page.press("settingEffectsVolume", KeyCode.HOME);
        page.verifyVolumes(10, 0, "10 %", "Silent");
        page.verifyAppearance();
        page.captureDialog("settingEffectsVolume", "r0227-volumes-en.png");
        // Czech names both categories and the silent state.
        settings.setCzechLanguage();
        page.verifyVolumeTexts("Hudba", "Efekty", "Ticho");
        page.verifyVolumes(10, 0, "10 %", "Ticho");
        page.verifyAppearance();
        page.captureDialog("settingEffectsVolume", "r0227-volumes-cs.png");
        settings.setEnglishLanguage();
        page.verifyVolumeTexts("Music", "Effects", "Silent");
        // Escape and reopening show the saved choices.
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        hosted.press(KeyCode.ESCAPE);
        WelcomePage.of(getContext()).verifyMainScreen();
        settings = WelcomePage.of(getContext()).openSetting();
        page.verifyVolumes(10, 0, "10 %", "Silent");
        settings.goBack();
        // Recreate the application/injector against the same persisted test
        // profile.
        final Stage restarted = getContext().getPrimaryStage();
        final org.testfx.api.FxRobot robot = getContext().getRobot();
        robot.interact(() -> {
            getMicroCol().stop();
            restarted.hide();
        });
        initialize(restarted, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.microcol"));
        getContext().setRobot(robot);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> {
            restarted.toFront();
            restarted.requestFocus();
        });
        final SettingPage reloaded = WelcomePage.of(getContext()).openSetting();
        final FramedSettingsPage reloadedPage = new FramedSettingsPage(
                getContext());
        reloadedPage.verifyVolumes(10, 0, "10 %", "Silent");
        // In-game Settings shows and changes the same categories.
        reloaded.goBack().loadGame().waitWhileMoving();
        final DeveloperModePage developer = new DeveloperModePage(getContext());
        developer.openGameSettings();
        reloadedPage.verifyVolumeTexts("Music", "Effects", "Silent");
        reloadedPage.verifyVolumes(10, 0, "10 %", "Silent");
        reloadedPage.press("settingEffectsVolume", KeyCode.RIGHT);
        reloadedPage.verifyVolumes(10, 10, "10 %", "10 %");
        new HostedPanelPage(getContext()).verifyCompleteTexts();
        reloadedPage.captureDialog("settingEffectsVolume",
                "r0227-volumes-game.png");
        developer.backToGame();
        developer.openGameSettings();
        reloadedPage.verifyVolumes(10, 10, "10 %", "10 %");
        developer.backToGame();
    }

}
