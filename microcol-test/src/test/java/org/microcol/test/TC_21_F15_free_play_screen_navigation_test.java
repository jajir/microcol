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
    }

    @Test
    @Tag("local")
    void TC_04_colony_click_and_escape_return() throws Exception {
        // Open the actual colony and return through its close button.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.verifyColonyName("New Amsterdam");
        game = colony.close();
        // Reopen it and use Escape; this does not claim a colony-open shortcut.
        game.openColonyAt(COLONY, "New Amsterdam")
                .verifyColonyName("New Amsterdam");
        new FreePlayAuditPage(getContext()).closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_05_menu_records_current_save_boundary() throws Exception {
        // Inspect the unfinished-campaign gate before loading an ordinary game.
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyFreePlayButtonEnabled(false);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        // Save at the existing human-turn boundary, then move the ship.
        game.nextTurnAndCloseDialogs();
        byte[] saved = audit.readAutosave();
        game.moveMouseAtLocation(Location.of(25, 16));
        game.dragMouseAtLocation(COLONY);
        game.verifyNumberOfUnitsAt(Location.of(25, 16), 0);
        // Menu entry currently preserves the old snapshot: a documented gap.
        game.exitToMenu().verifyMainScreen();
        audit.verifyAutosaveUnchanged(saved);
        // The fresh-start menu also fails to refresh Continue availability.
        audit.verifyContinueButtonEnabled(false);
    }
}
