package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** Verify existing screen journeys before refining R-0016 and R-0018. */
@ExtendWith(ApplicationExtension.class)
public class TC_21_F15_free_play_screen_navigation_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_europe_click_and_keyboard_return() throws Exception {
        // Load the game, open Europe with its button, and return with its
        // button.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        europe.verifyGold(10000);
        game = europe.close();
        // Repeat using E and Escape; the treasury must not change.
        europe = game.openEuroperPortByPressingKey();
        europe.verifyGold(10000);
        new FreePlayAuditPage(getContext()).closeWithEscape();
        new FreePlayAuditPage(getContext()).verifyCalendarAndGold(18, 10000);
    }

    @Test
    @Tag("local")
    void TC_02_turn_report_click_and_keyboard_return() throws Exception {
        // Open and close the report with visible controls.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ScreenTurnReport report = game.openTurnReport();
        report.verifyReady();
        report.close();
        GamePage.of(getContext());
        // T opens the same report and Escape returns to the map.
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.press(KeyCode.T);
        ScreenTurnReport.of(getContext()).verifyReady();
        audit.closeWithEscape();
        audit.verifyCalendarAndGold(18, 10000);
    }

    @Test
    @Tag("local")
    void TC_03_statistics_click_and_keyboard_return() throws Exception {
        // Open statistics with its button and return with its close control.
        WelcomePage.of(getContext()).loadGame();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.openStatistics();
        audit.verifyReportScreen("Gold");
        audit.closeReportWithClick();
        // Repeat the journey using R and Escape.
        audit.press(KeyCode.R);
        audit.verifyReportScreen("Gold");
        audit.closeWithEscape();
        audit.verifyCalendarAndGold(18, 10000);
    }

    @Test
    @Tag("local")
    void TC_04_colony_click_and_escape_return() throws Exception {
        // Open the actual colony and return through its close button.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.verifyColonyName("New Amsterdam");
        game = colony.close();
        // Reopen it and use Escape.
        game.openColonyAt(COLONY, "New Amsterdam")
                .verifyColonyName("New Amsterdam");
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.closeWithEscape();

        // The visible owned-colony menu and O both open the same colony.
        audit.openColonyMenu();
        audit.verifyColonyMenu("New Amsterdam");
        audit.chooseColony("New Amsterdam");
        ColonyScreen.of(getContext(), "New Amsterdam").close();
        audit.press(KeyCode.O);
        audit.verifyColonyMenu("New Amsterdam");
        audit.press(KeyCode.DOWN);
        audit.press(KeyCode.ENTER);
        ColonyScreen.of(getContext(), "New Amsterdam")
                .verifyColonyName("New Amsterdam");
        audit.closeWithEscape();
        audit.verifyCalendarAndGold(18, 10000);
    }

    @Test
    @Tag("local")
    void TC_05_menu_saves_latest_position_for_fresh_profile() throws Exception {
        // Free play is available before completing the campaign.
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyFreePlayButtonEnabled(true);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        // Establish a latest-position save through the ordinary menu path.
        game.exitToMenu().verifyMainScreen();
        byte[] saved = audit.readAutosave();
        game = audit.continueGame();
        game.moveMouseAtLocation(Location.of(25, 16));
        game.dragMouseAtLocation(COLONY);
        game.verifyNumberOfUnitsAt(Location.of(25, 16), 0);
        // Menu entry saves the move and enables Continue for this fresh
        // profile.
        game.exitToMenu().verifyMainScreen();
        audit.verifyAutosaveChanged(saved);
        audit.verifyContinueButtonEnabled(true);
        game = audit.continueGame();
        game.verifyNumberOfUnitsAt(Location.of(25, 16), 0);
        game.verifyNumberOfUnitsAt(COLONY, 2);
        final org.microcol.page.PassengerTransferPage passengers = new org.microcol.page.PassengerTransferPage(
                getContext());
        passengers.verifyAshore(4, COLONY, 1, 80);
        passengers.verifyCargoSlotEmpty(3, 1);
    }

    @Test
    @Tag("local")
    void TC_06_settings_return_without_active_game() throws Exception {
        // Returning from settings must not try to save a nonexistent game.
        WelcomePage.of(getContext()).openSetting().goBack().verifyMainScreen();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyFreePlayButtonEnabled(true);
        audit.verifyContinueButtonEnabled(false);
    }

    @Test
    @Tag("local")
    void TC_07_cancelled_load_preserves_recovery_save() throws Exception {
        // Preserve a playable position through the ordinary menu save path.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.exitToMenu().verifyMainScreen();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        byte[] saved = audit.readAutosave();

        // Cancelling the native chooser leaves the menu and save intact.
        audit.cancelLoadChooser();
        WelcomePage.of(getContext()).verifyMainScreen();
        audit.verifyAutosaveUnchanged(saved);
        audit.verifyContinueButtonEnabled(true);
        game = audit.continueGame();
        game.verifyNumberOfUnitsAt(Location.of(25, 16), 1);
        game.openEuroperPort().verifyGold(10000);
    }

    @Test
    @Tag("local")
    void TC_08_returning_from_a_button_to_the_same_tile_restores_its_description()
            throws Exception {
        // Read an owned colony, then hover a game command without executing it.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.hoverTileDirectly(COLONY);
        game.verifyThatStatusBarContains("New Amsterdam");
        audit.hoverNextTurnDirectly();
        game.verifyThatStatusBarContains("Next turn");

        // Returning directly to the same tile must restore its map description.
        audit.hoverTileDirectly(COLONY);
        game.verifyThatStatusBarContains("New Amsterdam");
        audit.verifyCalendarAndGold(18, 10000);
    }

    @Test
    @Tag("local")
    void TC_09_late_computer_turn_notification_does_not_block_the_current_human()
            throws Exception {
        // A previous computer turn notification arrives after control returned
        // to the human.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyCurrentPlayerIsHuman();
        audit.deliverPreviousComputerTurnNotification();
        audit.verifyCurrentPlayerIsHuman();

        // Return from a command to the map and open the colony with native
        // mouse input.
        audit.hoverNextTurnDirectly();
        game.verifyThatStatusBarContains("Next turn");
        game.moveMouseAtLocation(COLONY);
        game.verifyThatStatusBarContains("New Amsterdam");
        game.openColonyAt(COLONY, "New Amsterdam").close();
        audit.verifyCalendarAndGold(18, 10000);
    }

}
