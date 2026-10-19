package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.diagnostics.DiagnosticEvent;
import org.microcol.model.campaign.po.RecoverySlot;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A failed Exit keeps the active game playable, names the local diagnostic
 * folder and records only safe context. The save folder is replaced by a file,
 * not by changing permissions, so the test is independent of the user.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_99_F90_diagnostic_failure_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");
    private static final String PLAYER_TEXT = "New Amsterdam";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_exit_save_failure_keeps_game_and_records_safe_diagnostic()
            throws Exception {
        // Load the fixture and make the save folder unusable as a directory.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentTurn(18);
        audit.rememberActiveGameBeforeSaveFailure();
        final boolean gridShown = audit.isGridShown();
        audit.replaceSaveFolderWithFile();

        // Exit reports the failure and names the diagnostic folder.
        audit.attemptExitToMenu();
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains("The game could not be saved.");
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(audit.diagnosticFolderLine());
        DialogMessagePage.of(getContext()).close();

        // The active game and its settings are unchanged; the record is safe.
        audit.verifyGridShown(gridShown);
        audit.verifyCurrentTurn(18);
        audit.verifyCurrentPlayerIsHuman();
        audit.verifyActiveGameUnchangedAfterSaveFailure();
        audit.verifyDiagnosticRecord(DiagnosticEvent.EXIT_SAVE_FAILED,
                PLAYER_TEXT);

        // After restoring the save folder, Exit saves normally.
        audit.restoreSaveFolder();
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_POSITION, 18);
    }

    @Test
    @Tag("local")
    void TC_02_blocked_diagnostic_folder_keeps_save_failure_feedback()
            throws Exception {
        // Block the log folder as well as the save folder.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentTurn(18);
        audit.rememberActiveGameBeforeSaveFailure();
        audit.blockDiagnosticFolder();
        audit.replaceSaveFolderWithFile();

        // The save failure is still reported with the blocked folder named.
        audit.attemptExitToMenu();
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains("The game could not be saved.");
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(audit.diagnosticFolderLine());
        DialogMessagePage.of(getContext()).close();

        // The game remains playable; after restoring folders Exit saves again.
        audit.verifyCurrentTurn(18);
        audit.verifyActiveGameUnchangedAfterSaveFailure();
        audit.unblockDiagnosticFolder();
        audit.restoreSaveFolder();
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_POSITION, 18);
    }
}
