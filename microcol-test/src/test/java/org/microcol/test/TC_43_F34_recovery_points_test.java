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
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

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
                "Date unavailable");
        audit.chooseRecovery(RecoverySlot.LATEST_TURN);
        DialogMessagePage.of(getContext()).close();
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
        audit.verifyRecoveryChooserLabels("Vyberte bod obnovy", "Nahrát hru",
                "Zrušit");
        audit.captureDialog("recoveryCancel", "recovery-chooser-czech.png");
        audit.cancelRecovery();
        WelcomePage.of(getContext()).verifyMainScreen();
    }
}
