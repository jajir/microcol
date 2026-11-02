package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.page.DialogMessagePage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.HostedPanelPage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.SavedGamesPage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** Recovery points retain their own dates and only selected slots load. */
@ExtendWith(ApplicationExtension.class)
public class TC_43_F34_recovery_points_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.akropia");

    @Start
    private void start(final Stage stage) throws Exception {
        final Path settings = Path.of(System.getProperty("user.home"),
                ".akropia");
        Files.deleteIfExists(settings.resolve("autosave.akropia"));
        Files.deleteIfExists(settings.resolve("latest-turn.akropia"));
        Files.deleteIfExists(settings.resolve("previous-decade.akropia"));
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
        audit.rememberPlaythrough();
        audit.verifyCurrentTurn(18);

        // Advance through 1509, 1510 and 1511.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(21);
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_TURN, 21);
        audit.verifyRecoveryTurn(RecoverySlot.PREVIOUS_DECADE, 20);
        audit.verifyTurnSnapshots(19, 20, 21);

        // The saved turn is left without a prompt, so no Latest position is
        // written; the chooser shows actual dates.
        game.exitToMenu().verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.rememberRecoveryBytes();
        audit.openRecoveryChooser();
        audit.verifyRecoveryOption(RecoverySlot.LATEST_POSITION, false,
                "Unavailable");
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
        audit.verifyTurnSnapshots(19, 20, 21);

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
        audit.rememberPlaythrough();
        game.nextTurnAndCloseDialogs();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
        audit.verifyTurnSnapshots();

        // Menu exit of the unsaved turn saves it through the manual Save
        // dialog for one-click Continue; no Latest position is written.
        game.exitToMenu().verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.openRecoveryChooser();
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, false,
                "Unavailable");
        audit.cancelRecovery();
        audit.continueGame();
        audit.verifyCurrentTurn(19);

        // A corrupt point stays listed with its reason but cannot be
        // confirmed; valid retry works.
        GamePage.of(getContext()).exitToMenu();
        audit.corruptRecovery(RecoverySlot.LATEST_TURN);
        audit.openRecoveryChooser();
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, true,
                "Unavailable or invalid saved-game details");
        audit.verifyLoadDisabled("Unavailable or invalid saved-game details");
        audit.verifyLoadPageOpen();
        audit.closeLoadPage();
        WelcomePage.of(getContext()).verifyMainScreen();
        audit.continueGame();
        audit.verifyCurrentTurn(19);
    }

    @Test
    @Tag("local")
    void TC_03_failed_menu_save_keeps_playable_game() throws Exception {
        // Without turn checkpoints, play one unsaved turn from the fixture.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        game.nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(19);

        // Obstruct the managed save folder.
        audit.rememberActiveGameBeforeSaveFailure();
        audit.obstructManagedSaves();

        // Menu exit opens the manual Save dialog; the failed save is reported
        // and the dialog stays for another choice.
        audit.attemptExitToMenu();
        saves.saveName("Cannot be written");
        DialogMessagePage.of(getContext()).close();
        saves.verifySaveDialogOpen(true);

        // Cancel keeps the game playable and writes no recovery file.
        saves.cancelBrowser();
        audit.verifyCurrentTurn(19);
        audit.verifyCurrentPlayerIsHuman();
        audit.verifyActiveGameUnchangedAfterSaveFailure();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);

        // The failed save keeps the progress unsaved: leaving still asks.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.action("saveCancel");

        // After clearing the obstruction, menu exit saves a named save.
        audit.removeManagedSavesObstruction();
        audit.attemptExitToMenu();
        saves.saveName("After the failure");
        WelcomePage.of(getContext()).verifyMainScreen();
        saves.verifySavedName("After the failure", 1);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.continueGame();
        audit.verifyCurrentTurn(19);
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
        audit.openRecoveryChooser();
        new HostedPanelPage(getContext()).verifyFilterLabel("Body obnovy");
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
        audit.rememberPlaythrough();
        saves.quickSave();
        saves.verifyQuickSave();
        game.nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(19);
        // Its readable metadata hide the damaged game until it is loaded.
        audit.corruptRecoveryGame(RecoverySlot.LATEST_TURN);
        // The damaged turn snapshot no longer protects the live game.
        audit.corruptNewestTurnSnapshotGame();
        saves.rememberGame();
        audit.rememberRecoveryBytes();

        // Recovery points remain a filter; selecting only shows the details.
        saves.openLoad();
        hosted.chooseFilter("saveFilterAUTOMATIC");
        audit.verifyRecoveryOption(RecoverySlot.LATEST_TURN, true, "1509");
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

    @Test
    @Tag("local")
    void TC_06_load_game_replaces_recovery_menu_entry() throws Exception {
        // The English menu keeps its order without Recovery points.
        final WelcomePage welcome = WelcomePage.of(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        welcome.verifyMenuEntries(List.of("Continue", "Load saved game",
                "Play campaign", "New free play game", "Hall of Fame",
                "Setting", "Quit Akropia"));

        // Tab to Load game and open it with Space; all save types are shown.
        welcome.tabToMenuButton("buttonLoad");
        welcome.verifyMenuButtonFocused("buttonLoad");
        hosted.press(KeyCode.SPACE);
        audit.verifyLoadPageOpen();
        hosted.verifyFilterLabel("All saves");

        // Choose Recovery points with the keyboard; the empty view explains
        // itself.
        hosted.chooseFilterWithKeyboard("saveFilterAUTOMATIC");
        hosted.verifyFilterLabel("Recovery points");
        hosted.verifyEmptyListExplained("No saved games in this view.");

        // Back returns to the menu without writing any recovery point.
        audit.cancelRecovery();
        welcome.verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);

        // The Czech menu has the same order without Body obnovy.
        final SettingPage setting = welcome.openSetting();
        setting.setCzechLanguage();
        setting.goBack().verifyMainScreen();
        welcome.verifyMenuEntries(List.of("Pokračovat", "Nahrát hru",
                "Spustit kampaň", "Nová neomezená hra", "Síň slávy",
                "Nastavení", "Ukončit hru"));

        // The platform shortcut opens Load game with the Czech recovery
        // filter.
        hosted.pressShortcut(KeyCode.O);
        audit.verifyLoadPageOpen();
        hosted.chooseFilter("saveFilterAUTOMATIC");
        hosted.verifyFilterLabel("Body obnovy");
        hosted.verifyEmptyListExplained(
                "V tomto přehledu nejsou žádné uložené hry.");

        // Escape returns to the menu and still writes nothing.
        hosted.press(KeyCode.ESCAPE);
        welcome.verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
    }

    @Test
    @Tag("local")
    void TC_07_rolling_history_keeps_the_five_newest_turn_snapshots()
            throws Exception {
        // Enable turn autosaves and load the 1508 fixture; loading writes none.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(true);
        final GamePage game = setting.goBack().loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.rememberPlaythrough();
        audit.verifyCurrentTurn(18);
        audit.verifyTurnSnapshots();

        // Six prepared turns keep the five newest snapshots, 1510 to 1514.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(24);
        audit.verifyTurnSnapshots(20, 21, 22, 23, 24);
        audit.verifyContinueOpensNewestTurnSnapshot();

        // The saved turn is left without a prompt; Load lists the history
        // newest first with actual years.
        game.exitToMenu().verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.openRecoveryChooser();
        audit.verifyTurnSnapshotOptions("1514", "1513", "1512", "1511", "1510");

        // Loading the oldest snapshot rewrites and rotates nothing.
        audit.chooseOldestTurnSnapshot();
        audit.verifyCurrentTurn(20);
        audit.verifyTurnSnapshots(20, 21, 22, 23, 24);

        // Its next turn is a new write: it is appended and the oldest goes.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(21);
        audit.verifyTurnSnapshots(21, 22, 23, 24, 21);
        audit.verifyContinueOpensNewestTurnSnapshot();

        // With autosave off a played turn adds and removes no snapshot.
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        final SettingPage off = WelcomePage.of(getContext()).openSetting();
        off.setAutosaveEnabled(false);
        off.goBack().verifyMainScreen();
        audit.continueGame().nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(22);
        audit.verifyTurnSnapshots(21, 22, 23, 24, 21);

        // Enabling it again resumes at the next turn start without backfill.
        GamePage.of(getContext()).exitToMenu().verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        final SettingPage on = WelcomePage.of(getContext()).openSetting();
        on.setAutosaveEnabled(true);
        on.goBack().verifyMainScreen();
        audit.continueGame();
        audit.verifyTurnSnapshots(21, 22, 23, 24, 21);
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        audit.verifyCurrentTurn(23);
        audit.verifyTurnSnapshots(22, 23, 24, 21, 23);

        // A repeated notice of the same turn start writes nothing new.
        audit.deliverTurnStartedNotification(true, true);
        audit.verifyTurnSnapshots(22, 23, 24, 21, 23);
    }
}
