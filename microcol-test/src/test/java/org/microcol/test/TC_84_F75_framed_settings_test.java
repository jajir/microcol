package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.FramedSettingsPage;
import org.microcol.page.DeveloperModePage;
import org.microcol.page.GamePage;
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
        developer.verifyIndicator(false);
        developer.rememberGame();
        // F3 refreshes the existing hover and preserves the ordinary
        // description.
        developer.pressF3();
        developer.verifyHover(ship, true, ordinary);
        developer.verifyIndicator(true);
        developer.captureDialog("developerModeIndicator",
                "developer-mode-map.png");
        developer.pressF3();
        developer.verifyHover(ship, false, ordinary);
        developer.verifyIndicator(false);
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
        developer.verifyState(true);
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
        developer.verifyIndicator(false);
        developer.pressF3();
        developer.verifyState(true);
        developer.verifyIndicator(true);
    }
}
