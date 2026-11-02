package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.diagnostics.DiagnosticEvent;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.page.DialogMessagePage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A failed Exit keeps the active game playable, names the local diagnostic
 * folder and records only safe context. The save folder is replaced by a file,
 * not by changing permissions, so the test is independent of the user.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_99_F90_diagnostic_failure_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.akropia");
    private static final String PLAYER_TEXT = "New Amsterdam";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_exit_save_failure_keeps_game_and_records_safe_diagnostic()
            throws Exception {
        // Without turn checkpoints, play one unsaved turn from the fixture.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        setting.goBack().loadGame().nextTurnAndCloseDialogs();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentTurn(19);

        // Make the save folder unusable as a directory.
        audit.rememberActiveGameBeforeSaveFailure();
        final boolean gridShown = audit.isGridShown();
        audit.replaceSaveFolderWithFile();

        // Save and continue reports the unusable folder before any Save
        // dialog and names the diagnostic folder.
        audit.attemptExitToMenu();
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains("The save folder could not be used.");
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(audit.diagnosticFolderLine());
        DialogMessagePage.of(getContext()).close();

        // The active game and its settings are unchanged; the record is safe.
        audit.verifyGridShown(gridShown);
        audit.verifyCurrentTurn(19);
        audit.verifyCurrentPlayerIsHuman();
        audit.verifyActiveGameUnchangedAfterSaveFailure();
        audit.verifyDiagnosticRecord(DiagnosticEvent.EXIT_SAVE_FAILED,
                PLAYER_TEXT);

        // After restoring the save folder, Exit saves normally.
        audit.restoreSaveFolder();
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.continueGame();
        audit.verifyCurrentTurn(19);
    }

    @Test
    @Tag("local")
    void TC_02_blocked_diagnostic_folder_keeps_save_failure_feedback()
            throws Exception {
        // Without turn checkpoints, play one unsaved turn from the fixture.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        setting.goBack().loadGame().nextTurnAndCloseDialogs();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentTurn(19);

        // Block the log folder as well as the save folder.
        audit.rememberActiveGameBeforeSaveFailure();
        audit.blockDiagnosticFolder();
        audit.replaceSaveFolderWithFile();

        // The save failure is still reported with the blocked folder named.
        audit.attemptExitToMenu();
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains("The save folder could not be used.");
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(audit.diagnosticFolderLine());
        DialogMessagePage.of(getContext()).close();

        // The game remains playable; after restoring folders Exit saves again.
        audit.verifyCurrentTurn(19);
        audit.verifyActiveGameUnchangedAfterSaveFailure();
        audit.unblockDiagnosticFolder();
        audit.restoreSaveFolder();
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.continueGame();
        audit.verifyCurrentTurn(19);
    }
}
