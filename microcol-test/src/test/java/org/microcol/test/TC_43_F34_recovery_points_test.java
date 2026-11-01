package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.campaign.po.RecoverySlot;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.HostedPanelPage;
import org.microcol.page.InGameLayoutPage;
import org.microcol.page.SavedGamesPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** Recovery points retain their own dates and only selected slots load. */
@ExtendWith(ApplicationExtension.class)
public class TC_43_F34_recovery_points_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        final Path settings = Path.of(System.getProperty("user.home"),
                ".microcol");
        Files.deleteIfExists(settings.resolve("autosave.microcol"));
        Files.deleteIfExists(settings.resolve("latest-turn.microcol"));
        Files.deleteIfExists(settings.resolve("previous-decade.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_decade_checkpoint_keeps_first_crossing() throws Exception {
        // Enable turn checkpoints and load the 1508 fixture.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(true);
        setting.verifyAutosaveEnabled(true);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentTurn(18);

        // Advance through 1509, 1510 and 1511.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(21);
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_TURN, 21);
        audit.verifyRecoveryTurn(RecoverySlot.PREVIOUS_DECADE, 20);

        // Menu exit writes Latest position; the chooser shows actual dates.
        game.exitToMenu().verifyMainScreen();
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_POSITION, 21);
        audit.rememberRecoveryBytes();
        audit.openRecoveryChooser();
        audit.verifyRecoveryOption(RecoverySlot.LATEST_POSITION, true, "1511");
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, true, "1511");
        audit.verifyRecoveryOption(RecoverySlot.PREVIOUS_DECADE, true, "1510");
        audit.cancelRecovery();
        audit.verifyRecoveryBytesUnchanged();

        // Choosing the older decade resumes that exact position.
        audit.openRecoveryChooser();
        audit.chooseRecovery(RecoverySlot.PREVIOUS_DECADE);
        audit.verifyCurrentTurn(20);
        audit.verifyLoadedRecoveryMatchesSlot(RecoverySlot.PREVIOUS_DECADE);
        audit.verifyRecoveryBytesUnchanged();

        // Return to the menu and restore the newer Latest turn point.
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.rememberRecoveryBytes();
        audit.openRecoveryChooser();
        audit.chooseRecovery(RecoverySlot.LATEST_TURN);
        audit.verifyCurrentTurn(21);
        audit.verifyLoadedRecoveryMatchesSlot(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryBytesUnchanged();
    }

    @Test
    @Tag("local")
    void TC_02_autosave_off_preserves_continue_and_corrupt_retry()
            throws Exception {
        // Disable turn checkpoints, then play another human turn.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        setting.verifyAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        game.nextTurnAndCloseDialogs();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);

        // Menu exit still saves Latest position for one-click Continue.
        game.exitToMenu().verifyMainScreen();
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_POSITION, 19);
        audit.openRecoveryChooser();
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, false,
                "Unavailable");
        audit.cancelRecovery();
        audit.continueGame();
        audit.verifyCurrentTurn(19);

        // A corrupt point stays selectable and reports failure; valid retry
        // works.
        GamePage.of(getContext()).exitToMenu();
        audit.corruptRecovery(RecoverySlot.LATEST_TURN);
        audit.openRecoveryChooser();
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, true,
                "Unavailable or invalid saved-game details");
        audit.chooseRecovery(RecoverySlot.LATEST_TURN);
        DialogMessagePage.of(getContext()).close();
        audit.verifyLoadPageOpen();
        audit.closeLoadPage();
        WelcomePage.of(getContext()).verifyMainScreen();
        audit.continueGame();
        audit.verifyCurrentTurn(19);
    }

    @Test
    @Tag("local")
    void TC_03_failed_menu_save_keeps_playable_game() throws Exception {
        // Load the fixture and obstruct the Latest position destination.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentTurn(18);
        audit.rememberActiveGameBeforeSaveFailure();
        audit.obstructLatestPositionSave();

        // Menu exit reports the failure while keeping the game playable.
        audit.attemptExitToMenu();
        DialogMessagePage.of(getContext()).close();
        audit.verifyCurrentTurn(18);
        audit.verifyCurrentPlayerIsHuman();
        audit.verifyActiveGameUnchangedAfterSaveFailure();

        // After clearing the obstruction, menu exit can save normally.
        audit.removeLatestPositionObstruction();
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_POSITION, 18);
    }

    @Test
    @Tag("local")
    void TC_04_czech_recovery_controls_are_visible() throws Exception {
        // Change the language and inspect the autosave setting.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setCzechLanguage();
        setting.verifyAutosaveLabel("Automaticky ukládat body obnovy tahů");
        setting.captureDialog("autosaveEnabled", "recovery-setting-czech.png");

        // Return to the menu and inspect the localized recovery chooser.
        setting.goBack().verifyMainScreen();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyRecoveryMenuLabel("Body obnovy");
        audit.openRecoveryChooser();
        audit.verifyRecoveryChooserLabels("Uložené hry", "Načíst hru", "Zpět");
        audit.captureDialog("savedGamesCancel", "recovery-chooser-czech.png");
        audit.cancelRecovery();
        WelcomePage.of(getContext()).verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_05_load_page_keeps_filters_corrupt_retry_and_protection()
            throws Exception {
        // Load the fixture with recovery points enabled.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(true);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        audit.verifyCurrentTurn(18);

        // The embedded page keeps its types; an empty filter explains itself.
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyInGameHosted("Load game");
        hosted.chooseFilter("saveFilterMANUAL");
        hosted.verifyEmptyListExplained("No saved games in this view.");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0377-load-empty-filter-en.png");
        hosted.click("savedGamesCancel");

        // Keep a quick save of 1508, then play into 1509 and corrupt its point.
        saves.quickSave();
        saves.verifyQuickSave();
        game.nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(19);
        audit.corruptRecovery(RecoverySlot.LATEST_TURN);
        saves.rememberGame();
        audit.rememberRecoveryBytes();

        // Recovery points remain a filter; selecting only shows the details.
        saves.openLoad();
        hosted.chooseFilter("saveFilterAUTOMATIC");
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, true,
                "Unavailable or invalid saved-game details");
        audit.selectRecovery(RecoverySlot.LATEST_TURN);
        saves.verifyGameUnchanged();
        audit.verifyRecoveryBytesUnchanged();

        // The corrupt point fails before any leave prompt and keeps the page.
        saves.action("loadSelected");
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        DialogMessagePage.of(getContext()).close();
        audit.verifyLoadPageOpen();
        saves.verifyGameUnchanged();
        audit.verifyRecoveryBytesUnchanged();

        // Another save asks before leaving; Back keeps page, choice and game.
        hosted.chooseFilter("saveFilterQUICK");
        saves.select("Quick save");
        saves.action("loadSelected");
        hosted.verifyConfirmationOpen("saveCancel");
        saves.action("saveCancel");
        hosted.verifySelectedSave("Quick save");
        audit.verifyLoadPageOpen();
        saves.verifyGameUnchanged();

        // Leaving without saving starts the 1508 save, not the old origin.
        saves.action("loadSelected");
        saves.action("saveDiscard");
        game.verifyGameInFront();
        hosted.verifyLoadClosed();
        audit.verifyCurrentTurn(18);
        audit.verifyRecoveryBytesUnchanged();
    }
}
