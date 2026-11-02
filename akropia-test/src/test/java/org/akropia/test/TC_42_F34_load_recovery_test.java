package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.model.Difficulty;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.page.DialogMessagePage;
import org.akropia.page.HostedPanelPage;
import org.akropia.page.SavedGamesPage;
import org.akropia.page.SettingPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.FreePlaySetupPage;
import org.akropia.page.GamePage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** Failed Load and Continue keep the previous position and allow retry. */
@ExtendWith(ApplicationExtension.class)
public class TC_42_F34_load_recovery_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.akropia");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
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
        audit.verifyLoadPageOpen();
        audit.closeLoadPage();
        WelcomePage.of(getContext()).verifyMainScreen();

        // Choosing a valid save after the failure replaces the current game.
        audit.chooseLoadFile(FIXTURE);
        game = WelcomePage.of(getContext()).loadGame();
        audit.verifyValidRetryReplacedGame();
        game.openColonyAt(Location.of(24, 16), "New Amsterdam")
                .verifyColonyPresent("New Amsterdam", true);
    }

    @Test
    @Tag("local")
    void TC_03_failed_load_keeps_unsaved_progress_and_loading_writes_nothing()
            throws Exception {
        // Without turn checkpoints, play one unsaved turn from the fixture.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        audit.rememberPlaythrough();
        audit.rememberRecoveryBytes();
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // A malformed save is rejected before any leave prompt.
        audit.prepareMalformedLoadFile("{");
        audit.attemptInvalidSavedGameWhilePlaying();
        DialogMessagePage.of(getContext()).close();
        audit.verifyRejectedLoadPreservedGameAndFile();

        // The progress is still unsaved: leaving asks and Cancel keeps it.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.action("saveCancel");
        saves.verifyGameUnchanged();

        // Neither loading nor the rejected load wrote any recovery point.
        audit.verifyRecoveryBytesUnchanged();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyTurnSnapshots();
    }

    @Test
    @Tag("local")
    void TC_04_failed_initial_checkpoint_keeps_the_new_world_unsaved()
            throws Exception {
        // Block the managed save folder, then start a new world with autosave.
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        audit.obstructManagedSaves();
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        final GamePage game = setup.start();

        // The failed first snapshot is reported over the playable new game.
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains("The game could not be saved.");
        DialogMessagePage.of(getContext()).close();
        game.verifyGameInFront();
        audit.verifyCurrentPlayerIsHuman();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);

        // Leaving says the starting world was never saved; Cancel stays.
        saves.rememberGame();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveMessage("This game has never been saved");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();

        // After unblocking, Save and continue opens the manual Save dialog;
        // the named save preserves the starting world before leaving.
        audit.removeManagedSavesObstruction();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveConfirm");
        saves.verifySaveDialogOpen(true);
        saves.saveName("Starting world");
        WelcomePage.of(getContext()).verifyMainScreen();
        saves.verifySavedName("Starting world", 1);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        setup.verifySavedDifficulty(Difficulty.CONQUISTADOR);
        audit.continueGame();
        setup.verifyGameDifficulty(Difficulty.CONQUISTADOR, "Conquistador");
    }

    @Test
    @Tag("local")
    void TC_05_load_replacement_saves_through_the_manual_dialog()
            throws Exception {
        // Keep a named 1508 save, then play an unsaved turn.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        saves.openSave();
        saves.saveName("Older position");
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // Loading the valid older save asks; Save and continue opens the Save
        // dialog, whose Escape keeps the Load page, its choice and the game.
        saves.openLoad();
        saves.select("Older position");
        saves.action("loadSelected");
        saves.action("saveConfirm");
        saves.verifySaveDialogOpen(true);
        hosted.press(KeyCode.ESCAPE);
        saves.verifySaveDialogOpen(false);
        audit.verifyLoadPageOpen();
        hosted.verifySelectedSave("Older position");
        saves.verifyGameUnchanged();

        // A named save of the played turn completes, then the older save
        // loads.
        saves.action("loadSelected");
        saves.action("saveConfirm");
        saves.saveName("Before loading");
        game.verifyGameInFront();
        hosted.verifyLoadClosed();
        audit.verifyCurrentTurn(18);
        saves.verifyLoadedSave("Older position");
        saves.verifySavedName("Before loading", 1);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
    }
}
