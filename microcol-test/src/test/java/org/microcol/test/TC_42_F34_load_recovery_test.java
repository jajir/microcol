package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Failed Load and Continue keep the previous position and allow retry. */
@ExtendWith(ApplicationExtension.class)
public class TC_42_F34_load_recovery_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_failed_continue_without_game_allows_valid_load()
            throws Exception {
        // A malformed recovery snapshot is offered without an active game.
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.prepareMalformedContinueWithoutGame();
        WelcomePage.of(getContext()).openSetting().goBack();
        audit.verifyContinueButtonEnabled(true);
        audit.verifyAutosaveExists(true);

        // Continue reports the failure and preserves the source bytes.
        audit.attemptInvalidSavedGame(true);
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        DialogMessagePage.of(getContext()).close();
        new FreePlaySetupPage(getContext()).verifyNoActiveGame();
        audit.verifyRejectedLoadPreservedGameAndFile();
        WelcomePage.of(getContext()).verifyMainScreen();

        // A valid Load then starts the ordinary game.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        audit.verifyValidRetryReplacedGame();
        game.openColonyAt(Location.of(24, 16), "New Amsterdam")
                .verifyColonyPresent("New Amsterdam", true);
    }

    @Test
    @Tag("local")
    void TC_02_failed_load_cancel_picker_and_retry_preserve_game()
            throws Exception {
        // Keep a real game in memory while selecting malformed data.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.prepareMalformedLoadFile("{");
        audit.attemptInvalidSavedGameWhilePlaying();
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        DialogMessagePage.of(getContext()).close();
        audit.verifyRejectedLoadPreservedGameAndFile();

        // Leaving the game and canceling the picker do not change the file.
        game.exitToMenu();
        audit.cancelLoadChooser();
        audit.verifyRejectedSourceUnchanged();
        WelcomePage.of(getContext()).verifyMainScreen();

        // Choosing a valid save after the failure replaces the current game.
        audit.chooseLoadFile(FIXTURE);
        game = WelcomePage.of(getContext()).loadGame();
        audit.verifyValidRetryReplacedGame();
        game.openColonyAt(Location.of(24, 16), "New Amsterdam")
                .verifyColonyPresent("New Amsterdam", true);
    }
}
