package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.FramedSettingsPage;
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
}
