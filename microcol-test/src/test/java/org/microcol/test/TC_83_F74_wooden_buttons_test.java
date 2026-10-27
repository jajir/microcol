package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.WelcomePage;
import org.microcol.page.PaperButtonsPage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Stable TC83 scenarios verify the shared paper controls and their input
 * behavior.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_83_F74_wooden_buttons_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_menu_navigation_uses_wooden_buttons() throws Exception {
        // Each opening-menu button fits its label and icon in the shared
        // toolbar style, preserving existing menu actions.
        final PaperButtonsPage page = new PaperButtonsPage(getContext());
        final WelcomePage welcome = WelcomePage.of(getContext());
        welcome.resizeWindow(1000, 800);
        welcome.verifyOpeningControls(false, false);
        welcome.verifyMenuPalette();
        page.captureDialog("buttonSetting", "R-0308-menu-english.png");

        // Changing window size preserves the column and continuous background.
        welcome.resizeWindow(1400, 1000);
        welcome.verifyOpeningControls(false, false);
        welcome.verifyMenuPalette();
        page.captureDialog("buttonSetting", "R-0308-menu-wide.png");

        // Keyboard activation still opens Settings; Czech labels fit on return.
        page.focus("buttonSetting");
        page.verifyFocused("buttonSetting");
        page.press(KeyCode.SPACE);
        final org.microcol.page.SettingPage settings = org.microcol.page.SettingPage
                .of(getContext());
        settings.verifyLanguageOptionsShown();
        settings.setCzechLanguage();
        settings.goBack().verifyMainScreen();
        welcome.resizeWindow(1000, 700);
        welcome.verifyOpeningControls(true, false);
        page.captureDialog("buttonSetting", "R-0308-menu-czech-small.png");

        // A narrow column hides whole labels but keeps names, icons and height.
        welcome.setMenuColumnWidth(58);
        welcome.verifyOpeningControls(true, true);
        page.captureDialog("buttonSetting", "R-0308-menu-icons.png");
        welcome.setMenuColumnWidth(300);
        welcome.verifyOpeningControls(true, false);

        // Mouse navigation and switching back to English remain available.
        final org.microcol.page.SettingPage english = welcome.openSetting();
        english.verifyLanguageOptionsShown();
        english.setEnglishLanguage();
        english.goBack().verifyMainScreen();
        welcome.verifyOpeningControls(false, false);
    }

    @Test
    @Tag("local")
    void TC_02_custom_game_and_ship_buttons_keep_their_appearance()
            throws Exception {
        // Map controls keep the shared frame, icon geometry and player journey.
        final org.microcol.page.GamePage game = WelcomePage.of(getContext())
                .loadGame();
        final PaperButtonsPage page = new PaperButtonsPage(getContext());
        page.verifyToolbarButton("nextTurn");
        page.verifyToolbarButton("europe");
        page.verifyToolbarButton("congress");
        page.captureDialog("europe", "paper-game.png");
        page.click("congress");
        page.verifyModalFrame("congressClose");
        page.verifyPaperButton("congressClose");
        page.verifyVisibleButtonStyles();
        page.captureDialog("congressDialog", "paper-congress.png");
        page.click("congressClose");
        final org.microcol.page.EuropePortScreen europe = game
                .openEuroperPort();
        europe.verifyGold(10000);
        page.verifyOriginalShipButtons();
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_03_modal_buttons_keep_widths_states_and_keyboard_behavior()
            throws Exception {
        final PaperButtonsPage page = new PaperButtonsPage(getContext());
        // A modal fixture uses the production dialog stylesheet and real
        // controls.
        page.showSamples();
        page.verifyModalFrame("woodSamples");
        page.captureModalOnScreen("r0322-modal-on-menu.png");
        page.verifyTransparentSurroundings("woodSamples");
        page.captureDialog("woodSamples", "paper-dialog-first-layout.png");
        page.verifyPaperButton("woodOk");
        page.verifyPaperButton("woodLong");
        page.verifyWidthsAndInk();
        page.changeSampleLabel();
        page.verifyPaperButton("woodLong");
        page.captureDialog("woodSamples", "paper-dialog-normal.png");

        // Mouse hover and press have distinct colors; release performs one
        // action.
        page.hover("woodOk");
        page.verifyFill("woodOk", "#E7E7D0", false);
        page.holdMouse();
        page.verifyFill("woodOk", "#DCDCC5", true);
        page.captureDialog("woodSamples", "paper-dialog-pressed.png");
        page.releaseMouse();
        page.verifyActionCount(1);

        // Keyboard focus, Space and default Enter retain standard button
        // semantics.
        page.focus("woodOk");
        page.verifyFocused("woodOk");
        page.press(KeyCode.SPACE);
        page.verifyActionCount(2);
        page.focus("woodSamples");
        page.verifyFocused("woodSamples");
        page.press(KeyCode.ENTER);
        page.verifyActionCount(3);

        // Disabled actions stay inert, while selection persists independently
        // of hover.
        page.click("woodDisabled");
        page.verifyActionCount(3);
        page.click("woodToggle");
        page.verifySelected(true);
        page.verifyFill("woodToggle", "#E7E7D0", false);
        page.captureDialog("woodSamples", "paper-dialog-selected.png");

        // The same thin silhouette follows a larger window and returns intact.
        page.resizeSamples(780, 320);
        page.verifyModalFrame("woodSamples");
        page.verifyPaperButton("woodOk");
        page.captureDialog("woodSamples", "r0322-modal-wide.png");
        page.resizeSamples(576, 196);
        page.verifyModalFrame("woodSamples");
        page.press(KeyCode.ESCAPE);
        page.verifySamplesClosed();
        page.verifyActionCount(3);

        // Reopening also uses the shared window lifecycle and cancel button.
        page.showSamples();
        page.verifyModalFrame("woodSamples");
        page.click("woodBack");
        page.verifySamplesClosed();
    }
}
