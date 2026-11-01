package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DeveloperModePage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.HostedPanelPage;
import org.microcol.page.InGameLayoutPage;
import org.microcol.page.SavedGamesPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
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
        new org.microcol.page.PaperButtonsPage(getContext())
                .verifyModalFrame("savedGamesList");
        saves.saveName("");
        saves.verifyInvalidName();
        saves.captureDialog("savedGamesList", "r0298-saved-games.png");
        saves.saveName("  Přístav / před bitvou  ");
        saves.verifySavedName("Přístav / před bitvou", 1);

        // Cancelling overwrite and load keeps both the save and the live game.
        saves.openSave();
        saves.select("Přístav / před bitvou");
        saves.action("overwriteSave");
        new org.microcol.page.PaperButtonsPage(getContext())
                .verifyModalFrame("saveCancel");
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
        saves.rememberGame();

        // Both main-menu exit and the native window-close route can be
        // cancelled.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        // The leave confirmation uses the same paper controls as the map.
        final org.microcol.page.PaperButtonsPage buttons = new org.microcol.page.PaperButtonsPage(
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
        europe.verifyEuropePortName("Europe");
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
        // The session menu names the platform shortcuts.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.waitWhileMoving();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        final HostedPanelPage hosted = new HostedPanelPage(getContext());
        saves.openMenu();
        hosted.verifyMenuShortcut("menuSave", "Save game", KeyCode.S);
        hosted.verifyMenuShortcut("menuLoad", "Load game", KeyCode.O);
        saves.captureDialog("menuLoad", "r0377-game-menu-shortcuts.png");
        saves.chooseMenu("menuResume");
        saves.rememberGame();

        // Plain O still opens the colony list instead of Load.
        hosted.press(KeyCode.O);
        hosted.verifyLoadClosed();
        hosted.press(KeyCode.ESCAPE);
        game.verifyGameInFront();

        // Shortcut+S opens the existing Save dialog, which keeps the keys.
        hosted.pressShortcut(KeyCode.S);
        new org.microcol.page.PaperButtonsPage(getContext())
                .verifyModalFrame("savedGamesList");
        hosted.pressShortcut(KeyCode.O);
        hosted.verifyLoadClosed();
        saves.cancelBrowser();
        saves.verifyGameUnchanged();

        // Create the load fixture through the platform Save shortcut.
        hosted.pressShortcut(KeyCode.S);
        saves.saveName("Shortcut save");
        saves.verifySavedName("Shortcut save", 1);

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
        saves.verifyRestoredGame();

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
}
