package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.GamePage;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.SavedGamesPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** The session menu protects a live game and manages named and quick saves. */
@ExtendWith(ApplicationExtension.class)
public class TC_191_F224_session_menu_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_named_save_cancellation_quick_restore_and_window_close()
            throws Exception {
        // Import a known position and inspect the shared dropdown actions.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openMenu();
        saves.verifyMenu();
        saves.verifyRestartAvailable(false);
        saves.captureDialog("menuSave", "r0298-game-menu.png");
        saves.chooseMenu("menuResume");
        saves.rememberGame();

        // Settings returns to the same live position.
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        saves.action("buttonBack");
        saves.verifyGameUnchanged();

        // Empty names fail; Unicode punctuation is preserved independently of
        // filenames.
        saves.openSave();
        saves.saveName("");
        saves.verifyInvalidName();
        saves.captureDialog("savedGamesList", "r0298-saved-games.png");
        saves.saveName("  Přístav / před bitvou  ");
        saves.verifySavedName("Přístav / před bitvou", 1);

        // Cancelling overwrite and load keeps both the save and the live game.
        saves.openSave();
        saves.select("Přístav / před bitvou");
        saves.action("overwriteSave");
        saves.action("saveCancel");
        saves.cancelBrowser();
        saves.verifyGameUnchanged();
        saves.openLoad();
        saves.select("Přístav / před bitvou");
        saves.verifySelectedDetails("Dutch", "1508", "Conquistador");
        saves.captureDialog("savedGamesList", "r0298-saved-games-filled.png");
        saves.action("loadSelected");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();

        // Quick load restores the earlier turn, retaining its playthrough
        // identity.
        saves.quickSave();
        saves.verifyQuickSave();
        game.nextTurnAndCloseDialogs();
        saves.quickLoad();
        saves.action("saveDiscard");
        saves.verifyRestoredGame();
        saves.rememberGame();

        // Both main-menu exit and the native window-close route can be
        // cancelled.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();
        saves.closeWindow();
        saves.action("saveCancel");
        saves.verifyWindowOpen();
        saves.verifyGameUnchanged();

        // Manual deletion requires explicit confirmation and refreshes the
        // list.
        saves.openLoad();
        saves.select("Přístav / před bitvou");
        saves.action("deleteSave");
        saves.action("saveCancel");
        saves.verifySavedName("Přístav / před bitvou", 1);
        saves.select("Přístav / před bitvou");
        saves.action("deleteSave");
        saves.action("saveConfirm");
        saves.verifySavedName("Přístav / před bitvou", 0);
        saves.cancelBrowser();
    }

    @Test
    @Tag("local")
    void TC_02_campaign_restart_can_be_cancelled_or_start_the_original_mission()
            throws Exception {
        // Import a campaign position and keep the original mission state.
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.prepareCampaignImport();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        // A new campaign mission presents its two introductory messages.
        DialogMessagePage.of(getContext()).close();
        DialogMessagePage.of(getContext()).close();
        saves.rememberMissionStart();
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // Cancelling a restart preserves progress in the active mission.
        saves.openMenu();
        saves.verifyRestartAvailable(true);
        saves.chooseMenu("menuRestart");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();

        // Explicitly discarding progress starts the authored mission again.
        saves.openMenu();
        saves.chooseMenu("menuRestart");
        saves.action("saveDiscard");
        DialogMessagePage.of(getContext()).close();
        DialogMessagePage.of(getContext()).close();
        saves.verifyRestartedMission();
    }
}
