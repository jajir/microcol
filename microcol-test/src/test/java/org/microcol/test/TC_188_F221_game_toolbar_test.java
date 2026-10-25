package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Calendar.Season;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.GameToolbarPage;
import org.microcol.page.MainScreenLayoutPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;

/** Horizontal labels, freehand frames, seasonal art and colony navigation. */
@ExtendWith(ApplicationExtension.class)
public class TC_188_F221_game_toolbar_test extends AbstractMicroColTest {
    private StageStyle expectedWindowStyle;

    @Start
    private void start(final Stage stage) throws Exception {
        expectedWindowStyle = Platform.isSupported(
                ConditionalFeature.UNIFIED_WINDOW) ? StageStyle.UNIFIED
                        : stage.getStyle();
        initialize(stage, getClass(),
                new File("src/test/scenarios/F221-game-toolbar.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_labels_focus_seasons_and_continue() throws Exception {
        // The native window keeps its paper fill through screen changes.
        final MainScreenLayoutPage layout = new MainScreenLayoutPage(
                getContext());
        layout.verifyPaperWindowConfiguration(expectedWindowStyle);
        // Read all names and discover actions by hover and keyboard focus.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final GameToolbarPage toolbar = new GameToolbarPage(getContext());
        layout.verifyPaperWindowConfiguration(expectedWindowStyle);
        layout.resize(1200, 900);
        toolbar.verifyControls(false, false);
        final double namedWidth = toolbar.getCurrentRowWindowWidth();
        // Crossing the measured boundary hides all names together and restores
        // them, with no second row or oscillation on another layout pulse.
        layout.resize(namedWidth - 2, 900);
        toolbar.verifyControls(false, true);
        layout.showContextMessage("Resize boundary");
        toolbar.verifyControls(false, true);
        layout.resize(namedWidth + 2, 900);
        toolbar.verifyControls(false, false);
        layout.resize(900, 900);
        toolbar.verifyControls(false, true);
        toolbar.verifySeason(Season.autumn, 0xff71864b);
        toolbar.hover("europe");
        toolbar.verifyExplanation("Europe port. (E)", null);
        toolbar.focusCenterWithTab();
        toolbar.verifyExplanation("Find focused place. (C)", "center");
        toolbar.verifyUnitUnchanged(3, 3, 2);
        layout.saveEvidence("toolbar-one-row-keyboard-focus.png");
        toolbar.activateFocusedButton();
        toolbar.verifyUnitUnchanged(3, 3, 2);
        layout.saveEvidence("toolbar-one-row-icons.png");
        // Even an extreme resize leaves every icon accessible in one row.
        layout.resize(300, 900);
        toolbar.verifyControls(false, true);
        layout.resize(1200, 900);
        toolbar.verifyControls(false, false);
        layout.saveEvidence("toolbar-one-row-labels.png");

        // A real turn changes only the accent to winter blue; Continue keeps
        // it.
        game.nextTurnAndCloseDialogs();
        toolbar.verifySeason(Season.winter, 0xff568b9b);
        layout.saveEvidence("toolbar-compact-winter.png");
        game.exitToMenu();
        layout.verifyPaperWindowConfiguration(expectedWindowStyle);
        game = new FreePlayAuditPage(getContext()).continueGame();
        layout.verifyPaperWindowConfiguration(expectedWindowStyle);
        toolbar.verifySeason(Season.winter, 0xff568b9b);
        game.nextTurnAndCloseDialogs();
        toolbar.verifySeason(Season.spring, 0xff71864b);
    }

    @Test
    @Tag("local")
    void TC_02_czech_narrow_window_and_empty_colonies() throws Exception {
        // Start an ordinary annual game in Czech; all names fit a narrow
        // window.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final MainScreenLayoutPage layout = new MainScreenLayoutPage(
                getContext());
        layout.verifyPaperWindowConfiguration(expectedWindowStyle);
        settings.setCzechLanguage();
        settings.goBack().startFreeGame();
        layout.verifyPaperWindowConfiguration(expectedWindowStyle);
        final GameToolbarPage toolbar = new GameToolbarPage(getContext());
        layout.resize(900, 700);
        toolbar.verifyControls(true, true);
        toolbar.verifySeason(null, 0xff71864b);
        toolbar.hover("coloniesMenu");
        toolbar.verifyExplanation("Vyberte kolonii, kterou chcete otevřít. (O)",
                null);
        layout.saveEvidence("toolbar-one-row-czech.png");

        // Empty navigation explains itself and Escape leaves the game
        // untouched.
        toolbar.openColoniesWithKeyboard();
        toolbar.verifyColonies(true, true, "Zatím nemáte žádnou kolonii");
        toolbar.dismissColonies(false);
        toolbar.verifyColonies(false, true, "Zatím nemáte žádnou kolonii");
        toolbar.verifySeason(null, 0xff71864b);
        layout.resize(1500, 700);
        toolbar.verifyControls(true, false);
        layout.resize(900, 700);
        toolbar.verifyControls(true, true);
    }

    @Test
    @Tag("local")
    void TC_03_colony_dropdown_cancel_and_keyboard_choice() throws Exception {
        // The styled dropdown lists real owned colonies; outside click cancels.
        WelcomePage.of(getContext()).loadGame();
        final GameToolbarPage toolbar = new GameToolbarPage(getContext());
        new MainScreenLayoutPage(getContext()).resize(900, 700);
        toolbar.verifyControls(false, true);
        toolbar.openColoniesWithKeyboard();
        toolbar.verifyColonies(true, false, "New Amsterdam");
        final org.microcol.page.ContextMenuPage menu = new org.microcol.page.ContextMenuPage(
                getContext());
        menu.hoverItem(".menu-item");
        menu.verifyDrawnSelection(".menu-item:focused");
        menu.captureMenu("r0276-colony-focus.png");
        toolbar.dismissColonies(true);
        toolbar.verifyColonies(false, false, "New Amsterdam");

        // Keyboard selection opens the named colony and normal return preserves
        // it.
        toolbar.openColoniesWithKeyboard();
        toolbar.chooseFirstColony();
        final ColonyScreen colony = ColonyScreen.of(getContext(),
                "New Amsterdam");
        colony.verifyColonyName("New Amsterdam");
        colony.close();
        toolbar.verifySeason(Season.autumn, 0xff71864b);
    }
}
