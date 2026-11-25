package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** Verify existing screen journeys before refining R-0016 and R-0018. */
@ExtendWith(ApplicationExtension.class)
public class TC_21_F15_free_play_screen_navigation_test
        extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.akropia");
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_15_demo_studio_seeks_reloads_and_selects_scenes() throws Exception {
        // Open the studio with two editable scenes.
        DemoStudioPage studio = new DemoStudioPage(getContext());
        studio.open();
        try {
            studio.verifyAvailableScenes(2);
            studio.verifyReloadError(false);
            // Move beyond colony founding, then reconstruct the initial state.
            studio.seek(25000);
            studio.verifyPosition(25000, 1);
            studio.captureEvidence();
            studio.seek(2100);
            studio.verifyPosition(2100, 0);
            // Invalid edits preserve the last valid preview; repaired edits
            // reload.
            studio.corruptConfiguration();
            studio.click("Reload");
            studio.verifyReloadError(true);
            studio.verifyPosition(2100, 0);
            studio.restoreConfiguration();
            studio.click("Reload");
            studio.verifyReloadError(false);
            studio.verifyPosition(2100, 0);
            // Switching scenes and stopping reset presentation time.
            studio.selectSecond();
            studio.verifyPosition(0, 0);
            studio.seek(1000);
            studio.click("Stop");
            studio.verifyPosition(0, 0);
        } finally {
            studio.close();
        }
    }

    @Test
    @Tag("local")
    void TC_14_demo_shows_landing_and_the_separate_field_walk()
            throws Exception {
        // Restart the real preview clock so captures show each movement
        // mid-step.
        WelcomePage welcome = WelcomePage.of(getContext());
        welcome.restartDemo();
        welcome.observeDemo(11000);
        welcome.verifyDemoVisible();
        welcome.captureDialog("menu-demo", "demo-founder-landing.png");
        // The pioneer lands at the colony before walking to the field.
        welcome.observeDemo(4200);
        welcome.verifyDemoVisible();
        welcome.captureDialog("menu-demo", "demo-pioneer-landing.png");
        welcome.observeDemo(2500);
        welcome.verifyDemoVisible();
        welcome.captureDialog("menu-demo", "demo-pioneer-walk.png");
        // Finish the complete sequence and keep the normal menu usable.
        welcome.observeDemo(16000);
        welcome.verifyDemoVisible();
        welcome.loadGame().verifyGameInFront();
    }

    @Test
    @Tag("local")
    void TC_13_menu_demo_is_visible_and_menu_remains_usable() throws Exception {
        // Inspect the isolated map and pause its presentation.
        WelcomePage welcome = WelcomePage.of(getContext());
        welcome.observeDemo(1000);
        welcome.verifyDemoVisible();
        welcome.captureDialog("menu-demo", "demo-start.png");
        // Let the whole authored sequence finish and loop without input.
        welcome.observeDemo(18000);
        welcome.captureDialog("menu-demo", "demo-colony.png");
        welcome.observeDemo(16000);
        welcome.verifyDemoVisible();
        welcome.captureDialog("menu-demo", "demo-loop.png");
        welcome.pauseDemo();
        welcome.resizeWindow(1280, 850);
        welcome.verifyDemoVisible();
        welcome.captureDialog("menu-demo", "demo-wide.png");
        // Settings and return preserve a usable main menu and map preview.
        welcome = welcome.openSetting().goBack();
        welcome.verifyDemoVisible();
        welcome.loadGame().verifyGameInFront();
    }

    @Test
    @Tag("local")
    void TC_01_europe_click_and_keyboard_return() throws Exception {
        // Load the game, open Europe with its button, and return with its
        // button.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        europe.verifyGold(10000);
        MainScreenLayoutPage layout = new MainScreenLayoutPage(getContext());
        layout.verifyStatusBar();
        layout.saveEvidence("R-0267-status-Home port.png");
        game = europe.close();
        layout.verifyStatusBar();
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
        MainScreenLayoutPage layout = new MainScreenLayoutPage(getContext());
        layout.verifyStatusBar();
        layout.saveEvidence("R-0267-status-colony.png");
        game = colony.close();
        layout.verifyStatusBar();
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
        audit.press(KeyCode.I);
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
        // Only the ship is on the colony tile; its passenger stays aboard.
        game.verifyNumberOfUnitsAt(COLONY, 1);
        final org.akropia.page.PassengerTransferPage passengers = new org.akropia.page.PassengerTransferPage(
                getContext());
        passengers.verifyPassengerAboard(4, 1, 1, 80);
        passengers.verifyCargoSlotEmpty(3, 0);
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

        // Opening Load without a selection keeps the save intact; Back
        // returns to the menu.
        audit.openLoadWithoutSelection();
        audit.verifyLoadPageOpen();
        audit.closeLoadPage();
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
        game.verifyThatStatusBarContains(
                "Finish this turn and start the next one. (Ctrl+Enter)");

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
        game.verifyThatStatusBarContains(
                "Finish this turn and start the next one. (Ctrl+Enter)");
        game.moveMouseAtLocation(COLONY);
        game.verifyThatStatusBarContains("New Amsterdam");
        game.openColonyAt(COLONY, "New Amsterdam").close();
        audit.verifyCalendarAndGold(18, 10000);
    }

    @Test
    @Tag("local")
    void TC_10_main_screen_panels_remain_readable_when_resized()
            throws Exception {
        // The wider English screen retains all three status fields.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        MainScreenLayoutPage layout = new MainScreenLayoutPage(getContext());
        String english = "New Amsterdam: a fortified coastal colony with workers, stored goods and a ship waiting for its next voyage.";
        layout.resize(1280, 850);
        layout.showContextMessage(english);
        layout.verifyLayout(english);
        layout.verifyStatusBar();
        layout.verifyMainScreenSeparators();
        layout.verifyStatusValues("10\u00a0000", "Summer 19 · Year 1",
                "Gold 10\u00a0000");
        layout.saveEvidence("R-0267-main-English.png");
        layout.verifyMapTouchesSeparators();
        layout.verifyMainScreenTypography();
        layout.verifyMapPalette();
        new FreePlayAuditPage(getContext()).verifyCalendarAndGold(18, 10000);

        // Change the actual language setting and return to the same saved game.
        game.exitToMenu();
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        new FreePlayAuditPage(getContext()).continueGame();
        String czech = "Nový Amsterdam: opevněná pobřežní kolonie s pracovníky, uloženým zbožím a lodí čekající na další plavbu.";
        layout.resize(900, 700);
        layout.showContextMessage(czech);
        layout.verifyLayout(czech);
        layout.verifyStatusBar();
        layout.verifyMainScreenSeparators();
        layout.verifyStatusValues("10\u00a0000", "Léto 19 · Rok 1",
                "Zlato 10\u00a0000");
        layout.saveEvidence("R-0267-main-Czech-small.png");
        layout.verifyMapTouchesSeparators();
        layout.verifyMainScreenTypography();
        layout.verifyMapPalette();
        new FreePlayAuditPage(getContext()).verifyCalendarAndGold(18, 10000);
        final FreePlayAuditPage calendar = new FreePlayAuditPage(getContext());
        calendar.hoverCalendar();
        calendar.verifyCalendarDescription(
                "5. den Boedromionu · Podzim začne za 3 tahy.");
        layout.saveEvidence("expedition-calendar-czech.png");

    }
}
