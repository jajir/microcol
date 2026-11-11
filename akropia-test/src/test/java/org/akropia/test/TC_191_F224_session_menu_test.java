package org.akropia.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.page.ColonyScreen;
import org.akropia.page.KeyboardShortcutsPage;
import org.akropia.page.DeveloperModePage;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.FreePlaySetupPage;
import org.akropia.page.GamePage;
import org.akropia.page.DialogMessagePage;
import org.akropia.page.HostedPanelPage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.SavedGamesPage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** The session menu protects a live game and manages named and quick saves. */
@ExtendWith(ApplicationExtension.class)
public class TC_191_F224_session_menu_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_named_save_cancellation_quick_restore_and_window_close()
            throws Exception {
        // Turn autosaves off, so played turns stay unsaved progress.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);

        // Import a known position and inspect the shared dropdown actions.
        final GamePage game = setting.goBack().loadGame();
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
        saves.verifyFullScreenPaper("Save game");
        saves.saveName("");
        saves.verifyInvalidName();
        saves.captureDialog("savedGamesList", "r0298-saved-games.png");
        saves.saveName("../outside");
        saves.verifyInvalidName();
        saves.saveName("CON");
        saves.verifyInvalidName();
        saves.saveName("  Přístav – před bitvou  ");
        saves.verifySavedName("Přístav – před bitvou", 1);
        saves.verifySavedFilename("Přístav – před bitvou",
                "Přístav – před bitvou.akropia");
        // A same-name new save asks before replacing the existing file.
        saves.openSave();
        saves.saveName("Přístav – před bitvou.akropia");
        saves.action("saveCancel");
        saves.cancelBrowser();
        saves.verifySavedName("Přístav – před bitvou", 1);

        // Play one turn, so the named save no longer holds the live game.
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // Cancelling overwrite and load keeps both the save and the live game.
        saves.openSave();
        saves.select("Přístav – před bitvou");
        saves.action("saveNew");
        new org.akropia.page.PaperButtonsPage(getContext())
                .verifyModalFrame("saveCancel");
        saves.action("saveCancel");
        saves.cancelBrowser();
        saves.verifyGameUnchanged();
        saves.openLoad();
        saves.select("Přístav – před bitvou");
        saves.verifySelectedDetails("Corinth", "1508", "Conquistador");
        saves.captureDialog("savedGamesList", "r0298-saved-games-filled.png");
        saves.action("loadSelected");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();
        // Cancelling protection keeps the Load page until Back.
        saves.cancelBrowser();
        saves.verifyGameUnchanged();

        // Quick load restores the earlier turn, retaining its playthrough
        // identity.
        saves.quickSave();
        saves.verifyQuickSave();
        game.nextTurnAndCloseDialogs();
        saves.quickLoad();
        saves.action("saveDiscard");
        saves.verifyRestoredGame();

        // Play another turn, so leaving the restored game would lose it.
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // Both main-menu exit and the native window-close route can be
        // cancelled.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        // The leave confirmation uses the same paper controls as the map.
        final org.akropia.page.PaperButtonsPage buttons = new org.akropia.page.PaperButtonsPage(
                getContext());
        buttons.verifyPaperButton("saveConfirm");
        buttons.verifyModalFrame("saveConfirm");
        buttons.captureModalOnScreen("r0322-save-on-map.png");
        buttons.verifyPaperButton("saveDiscard");
        buttons.verifyPaperButton("saveCancel");
        buttons.captureDialog("saveConfirm", "r0316-save-confirmation.png");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();
        saves.closeWindow();
        saves.action("saveCancel");
        saves.verifyWindowOpen();
        saves.verifyGameUnchanged();

        // Manual deletion requires explicit confirmation and refreshes the
        // list.
        saves.openLoad();
        saves.select("Přístav – před bitvou");
        saves.deleteFile("Přístav – před bitvou");
        saves.action("saveCancel");
        saves.verifySavedName("Přístav – před bitvou", 1);
        saves.select("Přístav – před bitvou");
        saves.deleteFile("Přístav – před bitvou");
        saves.action("saveConfirm");
        saves.verifySavedName("Přístav – před bitvou", 0);
        saves.cancelBrowser();
    }

    @Test
    @Tag("local")
    void TC_02_campaign_restart_can_be_cancelled_or_start_the_original_mission()
            throws Exception {
        // Turn autosaves off, so the played turn stays unsaved progress.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);

        // Import a campaign position and keep the original mission state.
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.prepareCampaignImport();
        final GamePage game = setting.goBack().loadGame();
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

    @Test
    @Tag("local")
    void TC_03_embedded_load_returns_to_each_exact_origin() throws Exception {
        // Import a known position and remember the map view and selection.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        final InGameLayoutPage layout = new InGameLayoutPage(getContext());
        saves.rememberGame();
        hosted.rememberMapView();

        // The session menu opens Load in the game frame; Escape returns to the
        // same map view.
        saves.openLoad();
        hosted.verifyInGameHosted("Load game");
        layout.verifyBackToGame("savedGamesCancel", "Back to game");
        hosted.verifySingleLoadPage();
        layout.saveEvidence("r0377-load-map-en.png");
        hosted.press(KeyCode.ESCAPE);
        game.verifyGameInFront();
        hosted.verifyMapViewUnchanged();
        saves.verifyGameUnchanged();

        // The shortcut opens the same page from a colony; repeating it keeps
        // one page and the colony as its origin.
        final ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        saves.rememberGame();
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyInGameHosted("Load game");
        hosted.pressShortcut(KeyCode.O);
        hosted.verifySingleLoadPage();
        hosted.click("savedGamesCancel");
        colony.verifyColonyName("New Amsterdam");
        saves.verifyGameUnchanged();
        colony.close();

        // Europe and the selected report tab are restored exactly.
        final EuropePortScreen europe = game.openEuroperPort();
        saves.rememberGame();
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyInGameHosted("Load game");
        hosted.press(KeyCode.ESCAPE);
        europe.verifyEuropePortName("Home port");
        saves.verifyGameUnchanged();
        europe.close();
        layout.open("statistics");
        layout.open("fleetTab");
        saves.rememberGame();
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyInGameHosted("Load game");
        hosted.click("savedGamesCancel");
        layout.verifySelectedTab("fleetTab");
        saves.verifyGameUnchanged();
        layout.closeWithEscape();
        game.verifyGameInFront();

        // Load opened from in-game Settings returns to that Settings frame,
        // whose own return still leads to the map.
        hosted.rememberMapView();
        saves.rememberGame();
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        hosted.verifyInGameHosted("Settings");
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyInGameHosted("Load game");
        hosted.press(KeyCode.ESCAPE);
        hosted.verifyInGameHosted("Settings");
        layout.verifyBackToGame("buttonBack", "Back to game");
        hosted.press(KeyCode.ESCAPE);
        game.verifyGameInFront();
        hosted.verifyMapViewUnchanged();
        saves.verifyGameUnchanged();

        // From the main menu, Load and its Back stay in the menu sea.
        game.exitToMenu().verifyMainScreen();
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyMenuHosted("Load game", "savedGamesCancel", "Back");
        hosted.verifyScrollBarFollowsOverflow();
        hosted.captureDialog("savedGamesList", "r0377-load-menu-en.png");
        hosted.press(KeyCode.ESCAPE);
        WelcomePage.of(getContext()).verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_04_platform_shortcuts_respect_plain_keys_typing_and_dialogs()
            throws Exception {
        // Turn autosaves off, so a played turn stays unsaved progress.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);

        // The session menu names the platform shortcuts.
        final GamePage game = setting.goBack().loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        saves.openMenu();
        hosted.verifyMenuShortcut("menuSave", "Save game", KeyCode.S);
        hosted.verifyMenuShortcut("menuLoad", "Load game", KeyCode.O);
        saves.captureDialog("menuLoad", "r0377-game-menu-shortcuts.png");
        saves.chooseMenu("menuResume");
        saves.rememberGame();

        // Plain I opens the colony list; Shortcut+O remains Load.
        hosted.press(KeyCode.I);
        hosted.verifyLoadClosed();
        hosted.press(KeyCode.ESCAPE);
        game.verifyGameInFront();

        // Shortcut+S opens the Save screen, which keeps the keys.
        hosted.pressShortcut(KeyCode.S);
        saves.verifyFullScreenPaper("Save game");
        hosted.pressShortcut(KeyCode.O);
        saves.verifyFullScreenPaper("Save game");
        saves.cancelBrowser();
        saves.verifyGameUnchanged();

        // Create the load fixture through the platform Save shortcut.
        hosted.pressShortcut(KeyCode.S);
        saves.saveName("Shortcut save");
        saves.verifySavedName("Shortcut save", 1);

        // Play one turn, so loading the save would discard progress.
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // Shortcut+O loads only after confirmation; the shortcut does not
        // interrupt the confirmation and cancel keeps page and selection.
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyInGameHosted("Load game");
        saves.select("Shortcut save");
        saves.action("loadSelected");
        hosted.verifyConfirmationOpen("saveCancel");
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyConfirmationOpen("saveCancel");
        saves.action("saveCancel");
        hosted.verifySelectedSave("Shortcut save");
        saves.verifyGameUnchanged();

        // A completed load shows the loaded game, not the old origin.
        saves.action("loadSelected");
        saves.action("saveDiscard");
        game.verifyGameInFront();
        hosted.verifyLoadClosed();
        saves.verifyLoadedSave("Shortcut save");

        // The main menu names the same Load shortcut.
        game.exitToMenu().verifyMainScreen();
        hosted.verifyMenuShortcut("buttonLoad", "Load saved game", KeyCode.O);
        hosted.captureDialog("buttonLoad", "r0377-main-menu-shortcut.png");

        // Typing in a text field keeps the letter and opens nothing.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.enterName("Ocean");
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyLoadClosed();
        new DeveloperModePage(getContext()).verifyText("freePlayPlayerName",
                "Ocean");
        setup.back().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_05_save_and_load_show_preview_summary_and_safe_choices()
            throws Exception {
        // Import a known position; Save shows the current game apart from any
        // old target.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        saves.rememberGame();
        saves.openSave();
        saves.verifyDetailName("New save of the current game");
        saves.verifySimplifiedSaveControls();
        saves.verifyPreview(true);
        saves.verifySummaryLines("Year: 1508", "Gold: ", "Rebel support: ",
                "People: ", "Colonies: ", "Ships: ");
        saves.verifySuggestedName("1508");
        saves.captureDialog("savedGamesList", "r0382-save-current-en.png");
        saves.saveName("Harbour before the storm");
        saves.verifySavedName("Harbour before the storm", 1);
        saves.verifyStoredSummaryMatchesGame("Harbour before the storm");
        saves.openSave();
        saves.saveName("Future format");
        saves.openSave();
        saves.saveName("Removed later");
        saves.verifyGameUnchanged();

        // A selected old save is only the overwrite target; its confirmation
        // names it with its time.
        saves.openSave();
        saves.select("Harbour before the storm");
        saves.verifyDetailName("Harbour before the storm");
        saves.verifyFilename("Harbour before the storm");
        saves.captureDialog("savedGamesList", "save-dialog-selected.png");
        saves.doubleClickFile("Harbour before the storm");
        saves.verifyOverwriteConfirmation("Harbour before the storm");
        saves.verifyConfirmationOverSaveScreen();
        saves.captureDialog("saveCancel", "save-dialog-confirmation.png");
        saves.action("saveCancel");
        saves.verifyFilename("Harbour before the storm");
        saves.doubleClickFile("Harbour before the storm");
        saves.action("saveConfirm");
        saves.verifyStoredSummaryMatchesGame("Harbour before the storm");
        saves.openSave();
        saves.saveName("Deleted in save browser");
        saves.openSave();
        saves.select("Deleted in save browser");
        saves.deleteFile("Deleted in save browser");
        saves.verifyConfirmationOverSaveScreen();
        saves.action("saveCancel");
        saves.verifySavedName("Deleted in save browser", 1);
        saves.deleteFile("Deleted in save browser");
        saves.action("saveConfirm");
        saves.verifySavedName("Deleted in save browser", 0);
        saves.cancelBrowser();
        saves.verifyGameUnchanged();

        // Load starts without a selection; selecting shows only the detail.
        saves.openLoad();
        hosted.verifyInGameHosted("Load game");
        saves.verifyNoSelectedDetail(
                "Select a saved game to see its map and summary.");
        saves.select("Harbour before the storm");
        saves.verifyLoadLayout("Harbour before the storm");
        saves.verifyPreview(true);
        saves.verifySelectedDetails("Corinth", "1508", "Conquistador");
        saves.verifyLoadEnabled(true);
        saves.captureDialog("savedGamesList", "r0383-load-detail-en.png");
        saves.verifyGameUnchanged();

        // The filename-only list needs no filter; details retain type and time.
        saves.verifyFilenameList();
        saves.verifySummaryLines("Manual", "Saved:");
        hosted.click("savedGamesCancel");

        // An unsupported save names the reason and cannot be confirmed.
        saves.markUnsupported("Future format");
        saves.openLoad();
        saves.selectMarked();
        audit.verifyLoadDisabled(
                "This saved game uses an unsupported format and cannot be loaded.");
        saves.verifyGameUnchanged();

        // A save removed behind the list fails safely and keeps the game.
        saves.select("Removed later");
        saves.deleteBehindList("Removed later");
        saves.action("loadSelected");
        DialogMessagePage.of(getContext()).close();
        audit.verifyLoadPageOpen();
        saves.verifyGameUnchanged();
        hosted.click("savedGamesCancel");

        // A missing or damaged image explains itself; the save still loads.
        saves.removePreview("Harbour before the storm");
        saves.openLoad();
        saves.select("Harbour before the storm");
        saves.verifyPreview(false);
        saves.verifyLoadEnabled(true);
        hosted.click("savedGamesCancel");
        saves.damagePreview("Harbour before the storm");
        saves.openLoad();
        saves.select("Harbour before the storm");
        saves.verifyPreview(false);
        saves.verifyLoadEnabled(true);
        saves.captureDialog("savedGamesList", "r0383-load-no-preview-en.png");
        saves.action("loadSelected");
        saves.action("saveDiscard");
        game.verifyGameInFront();
        saves.verifyRestoredGame();
    }

    @Test
    @Tag("local")
    void TC_06_saved_game_leaves_without_prompt_until_gameplay_changes()
            throws Exception {
        // Turn autosaves off and import a known position.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());

        // Selecting a unit's square and a settings round trip change no
        // gameplay.
        game.selectTile(Location.of(25, 16));
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        saves.action("buttonBack");
        game.verifyGameInFront();

        // Leaving the loaded game needs no prompt and writes no recovery point.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(false);
        WelcomePage.of(getContext()).verifyMainScreen();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);

        // Continue opens the same save; a played turn makes leaving ask.
        final GamePage resumed = audit.continueGame();
        resumed.nextTurnAndCloseDialogs();
        saves.rememberGame();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.verifyLeaveMessage("Save the current game before leaving it?");

        // Escape keeps the game and its screen.
        hosted.press(KeyCode.ESCAPE);
        resumed.verifyGameInFront();
        saves.verifyGameUnchanged();

        // Save and continue opens the manual Save dialog; Escape there stays.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveConfirm");
        saves.verifySaveDialogOpen(true);
        hosted.press(KeyCode.ESCAPE);
        saves.verifySaveDialogOpen(false);
        resumed.verifyGameInFront();
        saves.verifyGameUnchanged();

        // Its Cancel also keeps the game, and no recovery file is written.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveConfirm");
        saves.cancelBrowser();
        resumed.verifyGameInFront();
        saves.verifyGameUnchanged();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);

        // A new named save completes the leave.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveConfirm");
        saves.saveName("Leaving 1509");
        WelcomePage.of(getContext()).verifyMainScreen();
        saves.verifySavedName("Leaving 1509", 1);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);

        // Continue opens that save, which is left without a prompt.
        audit.continueGame();
        audit.verifyCurrentTurn(19);
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(false);
        WelcomePage.of(getContext()).verifyMainScreen();

        // Overwriting the selected save needs its own confirmation; Cancel
        // keeps the browser and then the game.
        audit.continueGame().nextTurnAndCloseDialogs();
        saves.rememberGame();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveConfirm");
        saves.select("Leaving 1509");
        saves.action("saveNew");
        saves.verifyOverwriteConfirmation("Leaving 1509");
        saves.action("saveCancel");
        saves.verifySaveDialogOpen(true);
        saves.cancelBrowser();
        saves.verifyGameUnchanged();

        // A confirmed overwrite saves the played turn, then leaves.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveConfirm");
        saves.select("Leaving 1509");
        saves.action("saveNew");
        saves.action("saveConfirm");
        WelcomePage.of(getContext()).verifyMainScreen();
        saves.verifySavedName("Leaving 1509", 1);
        audit.continueGame();
        audit.verifyCurrentTurn(20);

        // Leaving changed progress without saving keeps every existing save.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveDiscard");
        WelcomePage.of(getContext()).verifyMainScreen();
        saves.verifySavedName("Leaving 1509", 1);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
    }

    @Test
    @Tag("local")
    void TC_08_window_close_save_dialog_cancel_and_failure_keep_the_game()
            throws Exception {
        // Turn autosaves off and play one unsaved turn from the fixture.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        game.nextTurnAndCloseDialogs();
        saves.rememberGame();

        // Closing the window asks; Save and continue opens the Save dialog,
        // whose Cancel keeps the window and the game.
        saves.closeWindow();
        saves.verifyLeaveConfirmation(true);
        saves.action("saveConfirm");
        saves.verifySaveDialogOpen(true);
        saves.cancelBrowser();
        saves.verifyWindowOpen();
        saves.verifyGameUnchanged();

        // A failed manual save is reported, keeps the Save dialog and, after
        // Cancel, the window and the game; nothing is written instead.
        saves.closeWindow();
        saves.action("saveConfirm");
        // Block writing after the catalogue has been read successfully.
        audit.obstructManagedSaves();
        saves.saveName("Cannot be written");
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains("The file operation failed.");
        DialogMessagePage.of(getContext()).close();
        saves.verifySaveDialogOpen(true);
        saves.cancelBrowser();
        saves.verifyWindowOpen();
        saves.verifyGameUnchanged();
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.removeManagedSavesObstruction();
        saves.verifySavedName("Cannot be written", 0);
    }

    @Test
    @Tag("local")
    void TC_07_new_world_initial_snapshot_or_never_saved_prompt()
            throws Exception {
        // With autosave on, a new world is saved before the first move.
        final GamePage game = WelcomePage.of(getContext()).startFreeGame();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.rememberPlaythrough();
        audit.verifyTurnSnapshotCount(1);
        audit.verifyContinueOpensNewestTurnSnapshot();

        // Leaving it at once needs no prompt.
        game.verifyGameInFront();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(false);
        WelcomePage.of(getContext()).verifyMainScreen();

        // With autosave off, a new world is unsaved and keeps the previous
        // game’s slot.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        setting.goBack().startFreeGame();
        audit.rememberPlaythrough();
        audit.verifyTurnSnapshotCount(1);

        // Leaving says the starting world would be lost; Cancel stays.
        saves.rememberGame();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.verifyLeaveMessage("This game has never been saved");
        saves.action("saveCancel");
        saves.verifyGameUnchanged();

        // Leaving without saving returns to the menu and writes nothing.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.action("saveDiscard");
        WelcomePage.of(getContext()).verifyMainScreen();
        audit.verifyTurnSnapshotCount(1);
    }

    @Test
    @Tag("local")
    void TC_09_plain_c_continues_only_from_the_available_welcome_screen()
            throws Exception {
        // A fresh profile has no Continue target; C leaves the menu unchanged.
        final WelcomePage welcome = WelcomePage.of(getContext());
        welcome.verifyContinueAvailable(false);
        welcome.pressKeys(KeyCode.C);
        welcome.verifyMainScreen();
        final SettingPage settings = welcome.openSetting();
        settings.setAutosaveEnabled(false);
        final GamePage game = settings.goBack().loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        saves.quickSave();
        saves.rememberGame();

        // C does not reload the live map or dismiss its modal menu.
        welcome.pressKeys(KeyCode.C);
        saves.verifyGameUnchanged();
        saves.openMenu();
        welcome.pressKeys(KeyCode.C);
        saves.verifyMenu();
        saves.verifyGameUnchanged();
        saves.chooseMenu("menuMain");
        welcome.verifyMainScreen();
        welcome.verifyContinueAvailable(true);

        // A modal opened from Welcome owns the key; Continue stays behind it.
        saves.action("buttonHallOfFame");
        welcome.pressKeys(KeyCode.C);
        welcome.verifyHallOfFameOpen();
        saves.action("hallOfFameClose");
        welcome.verifyMainScreen();

        // Modified C remains inert, even when Continue is available.
        welcome.pressKeys(KeyCode.CONTROL, KeyCode.C);
        welcome.pressKeys(KeyCode.ALT, KeyCode.C);
        welcome.pressKeys(KeyCode.SHIFT, KeyCode.C);
        welcome.verifyMainScreen();
        welcome.tabToMenuButton("buttonSetting");
        welcome.pressKeys(KeyCode.C);
        GamePage.of(getContext()).verifyGameInFront();
        saves.verifyRestoredGame();

        // The mouse path restores the same snapshot as the keyboard path.
        GamePage.of(getContext()).exitToMenu();
        audit.continueGame();
        saves.verifyRestoredGame();
        GamePage.of(getContext()).exitToMenu();

        // Settings do not inherit C, and the Czech welcome screen still uses C.
        final SettingPage czech = welcome.openSetting();
        czech.setCzechLanguage();
        welcome.pressKeys(KeyCode.C);
        czech.goBack();
        welcome.verifyContinueAvailable(true);
        welcome.pressKeys(KeyCode.C);
        GamePage.of(getContext()).verifyGameInFront();
        saves.verifyRestoredGame();
    }

    @Test
    @Tag("local")
    void TC_10_configured_load_shortcut_renames_menus_and_replaces_the_old_key()
            throws Exception {
        // Change Load to the platform shortcut with L in Settings.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        final KeyboardShortcutsPage shortcuts = new KeyboardShortcutsPage(
                getContext());
        shortcuts.open();
        shortcuts.verifyPlatformBinding(ShortcutAction.LOAD_GAME, KeyCode.O);
        shortcuts.startChange(ShortcutAction.LOAD_GAME, 0);
        shortcuts.pressPlatformShortcut(KeyCode.L);
        shortcuts.verifyPlatformBinding(ShortcutAction.LOAD_GAME, KeyCode.L);
        // The editor is a dialog; Load does not open while it captures keys.
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        hosted.verifyLoadClosed();
        shortcuts.saveAndBack();
        shortcuts.verifyEditorOpen(false);

        // The main menu names the new key at once; the old key does nothing.
        setting.goBack().verifyMainScreen();
        hosted.verifyMenuShortcut("buttonLoad", "Load saved game", KeyCode.L);
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyLoadClosed();
        WelcomePage.of(getContext()).verifyMainScreen();
        hosted.pressShortcut(KeyCode.L);
        hosted.verifyMenuHosted("Load game", "savedGamesCancel", "Back");
        hosted.press(KeyCode.ESCAPE);
        WelcomePage.of(getContext()).verifyMainScreen();

        // The in-game session menu uses the same label.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openMenu();
        hosted.verifyMenuShortcut("menuLoad", "Load game", KeyCode.L);
        hosted.verifyMenuShortcut("menuSave", "Save game", KeyCode.S);
        saves.chooseMenu("menuResume");
        game.verifyGameInFront();
    }

    @Test
    @Tag("local")
    void TC_11_load_lists_only_direct_filenames_and_shows_selected_details()
            throws Exception {
        // Create a named save without automatic snapshots.
        final SettingPage setting = WelcomePage.of(getContext()).openSetting();
        setting.setAutosaveEnabled(false);
        final GamePage game = setting.goBack().loadGame();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openSave();
        saves.saveName("Metadata title");
        saves.rememberGame();
        saves.prepareFilenameCatalogue("Metadata title");

        // Only the direct filename appears; opening does not select or load it.
        saves.openLoad();
        saves.verifyCatalogueFilenames("Actual file.akropia");
        saves.verifyFilenameList();
        saves.verifyNoSelectedDetail(
                "Select a saved game to see its map and summary.");

        // Selection exposes the preview, game summary, type and
        // time.
        saves.select("Metadata title");
        saves.verifyLoadLayout("Metadata title");
        saves.verifyPreview(true);
        saves.verifySelectedDetails("Corinth", "1508", "Conquistador");
        saves.verifySummaryLines("Manual", "Saved:");
        saves.verifyLoadEnabled(true);
        saves.captureDialog("savedGamesList", "r0436-load-filenames.png");
        saves.verifyGameUnchanged();
        saves.cancelBrowser();
        game.verifyGameInFront();
        saves.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_12_save_browser_allows_managed_files_and_uses_borderless_details()
            throws Exception {
        // Disable automatic writes while testing explicit file actions.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setAutosaveEnabled(false);
        final GamePage game = settings.goBack().loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.rememberGame();

        // Managed filenames can be saved; the list uses alphabetical order.
        saves.openSave();
        saves.saveName("Zulu");
        saves.openSave();
        saves.saveName("autosave-000");
        saves.openSave();
        saves.saveName("Alpha");
        saves.openSave();
        saves.verifyCatalogueFilenames("Alpha.akropia", "autosave-000.akropia",
                "Zulu.akropia");
        saves.select("autosave-000");
        saves.verifyManagedWarning();
        saves.verifyBorderlessDetails();
        saves.captureDialog("savedGamesList", "save-borderless-autosave.png");

        // Cancel preserves the existing slot; explicit confirmation overwrites
        // it.
        saves.action("saveNew");
        saves.verifyOverwriteConfirmation("autosave-000");
        saves.action("saveCancel");
        saves.verifySavedName("autosave-000", 1);
        saves.action("saveNew");
        saves.action("saveConfirm");
        saves.verifyStoredSummaryMatchesGame("autosave-000");

        // The row delete button supports cancellation and confirmed deletion.
        saves.openSave();
        saves.deleteFile("autosave-000");
        saves.action("saveCancel");
        saves.verifySavedName("autosave-000", 1);
        saves.deleteFile("autosave-000");
        saves.action("saveConfirm");
        saves.verifySavedName("autosave-000", 0);
        saves.verifyCatalogueFilenames("Alpha.akropia", "Zulu.akropia");
        saves.cancelBrowser();
        saves.verifyGameUnchanged();
    }
}
