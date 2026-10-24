package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.WelcomePage;
import org.microcol.page.WoodenButtonsPage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** Verify ordinary wooden buttons while preserving existing custom controls. */
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
        // The normal menu uses the new font and face without losing its
        // actions.
        final WoodenButtonsPage page = new WoodenButtonsPage(getContext());
        page.verifyWoodenButton("buttonSetting");
        page.verifyWoodenButton("buttonStartFreeGame");
        page.captureDialog("buttonSetting", "wooden-menu.png");
        WelcomePage.of(getContext()).openSetting().goBack().verifyMainScreen();
        page.verifyWoodenButton("buttonSetting");
    }

    @Test
    @Tag("local")
    void TC_02_custom_game_and_ship_buttons_keep_their_appearance()
            throws Exception {
        // Existing image controls retain the image, native skin and player
        // journey.
        final org.microcol.page.GamePage game = WelcomePage.of(getContext())
                .loadGame();
        final WoodenButtonsPage page = new WoodenButtonsPage(getContext());
        page.verifyOriginalImageButton("nextTurn");
        page.verifyOriginalImageButton("europe");
        page.verifyWoodenButton("congress", "Playpen Sans");
        page.captureDialog("europe", "wooden-game.png");
        page.click("congress");
        page.verifyWoodenButton("congressClose");
        page.captureDialog("congressDialog", "wooden-congress.png");
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
        final WoodenButtonsPage page = new WoodenButtonsPage(getContext());
        // A modal fixture uses the production dialog stylesheet and real
        // controls.
        page.showSamples();
        page.captureDialog("woodSamples", "wooden-dialog-first-layout.png");
        page.verifyWoodenButton("woodOk");
        page.verifyWoodenButton("woodLong");
        page.verifyWidthsAndInk();
        page.changeSampleLabel();
        page.verifyWoodenButton("woodLong");
        page.captureDialog("woodSamples", "wooden-dialog-normal.png");

        // Mouse hover and press have distinct colors; release performs one
        // action.
        page.hover("woodOk");
        page.verifyFill("woodOk", "#efc68f", false);
        page.holdMouse();
        page.verifyFill("woodOk", "#c69b6b", true);
        page.captureDialog("woodSamples", "wooden-dialog-pressed.png");
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
        page.verifyFill("woodToggle", "#c69b6b", false);
        page.captureDialog("woodSamples", "wooden-dialog-selected.png");
        page.press(KeyCode.ESCAPE);
        page.verifySamplesClosed();
        page.verifyActionCount(3);
    }
}
