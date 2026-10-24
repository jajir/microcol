package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.model.Location;
import org.microcol.page.AtlanticAuditPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FramedSettingsPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * R-0204: a ship arriving in the European port. The arrival notice offers Open
 * Europe. Its automatic opening is off by default, and switching the summary
 * off leaves a routine arrival in the manual report. The voyage is the one of
 * TC_28; Open Europe spends no movement and keeps the cargo unchanged.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_178_F204_europe_arrival_test extends AbstractMicroColTest {

    private final static File FIXTURE = new File(
            "src/test/scenarios/F20-western-voyage.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_default_arrival_offers_europe_and_keeps_the_game_in_front()
            throws Exception {
        // Defaults: the summary is on and the arrival does not open Europe.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        final FramedSettingsPage page = new FramedSettingsPage(getContext());
        page.verifyTurnSummary(true);
        page.verifyOpenEuropeOnArrival(false);
        GamePage game = settings.goBack().loadGame();
        final AtlanticAuditPage audit = new AtlanticAuditPage(getContext());

        // Sail west and advance to the arrival turn.
        final int duration = sailWestward(game, audit);
        sailUntilArrivalTurn(game, duration);

        // The arrival turn shows the report; the game stays in front of it.
        final Optional<ScreenTurnReport> report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.get().verifyEventMessageCount("European port", 1);
        report.get().verifyOpenEuropeAction("Open Europe");
        final int movement = audit.getMovement();

        // Open Europe from the report spends nothing and keeps ship and cargo.
        final EuropePortScreen europe = report.get().openEuropeWithMouse();
        audit.verifyInEurope();
        audit.verifyCargo();
        audit.verifyMovement(movement);
        game = europe.close();
        game.verifyTurnReportNotShown();
        game.verifyGameInFront();
    }

    @Test
    @Tag("local")
    void TC_02_summary_off_keeps_routine_arrival_in_the_manual_report()
            throws Exception {
        try {
            // Switch the routine summary off before the game starts.
            final SettingPage settings = WelcomePage.of(getContext())
                    .openSetting();
            final FramedSettingsPage page = new FramedSettingsPage(
                    getContext());
            page.press("settingTurnSummary", KeyCode.SPACE);
            page.verifyTurnSummary(false);
            page.verifyOpenEuropeOnArrival(false);
            GamePage game = settings.goBack().loadGame();
            final AtlanticAuditPage audit = new AtlanticAuditPage(getContext());

            // The arrival is routine, so no report opens by itself.
            final int duration = sailWestward(game, audit);
            sailUntilArrivalTurn(game, duration);
            game = game.nextTurnWithoutOpeningReport();
            game.verifyTurnReportNotShown();
            game.verifyGameInFront();
            final int movement = audit.getMovement();

            // The manual report keeps the arrival and its Open Europe action.
            ScreenTurnReport report = game.openTurnReport();
            report.verifyEventMessageCount("European port", 1);
            report.verifyOpenEuropeAction("Open Europe");
            report.close();

            // Reopening adds no duplicate notice and spends nothing.
            report = game.openTurnReport();
            report.verifyEventMessageCount("European port", 1);
            report.close();
            audit.verifyMovement(movement);

            // Continue neither repeats the notice nor opens the report.
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            game.verifyTurnReportNotShown();
            game.verifyGameInFront();
            audit.verifyInEurope();
            audit.verifyCargo();
            audit.verifyMovement(movement);
        } finally {
            restoreDefaultPreferences();
        }
    }

    @Test
    @Tag("local")
    void TC_03_auto_europe_waits_behind_the_report_and_opens_once()
            throws Exception {
        try {
            // Turn the Europe arrival option on; the summary stays on.
            final SettingPage settings = WelcomePage.of(getContext())
                    .openSetting();
            final FramedSettingsPage page = new FramedSettingsPage(
                    getContext());
            page.press("settingOpenEuropeOnArrival", KeyCode.SPACE);
            page.verifyOpenEuropeOnArrival(true);
            page.verifyTurnSummary(true);
            GamePage game = settings.goBack().loadGame();
            final AtlanticAuditPage audit = new AtlanticAuditPage(getContext());

            // The report opens first and keeps Open Europe.
            final int duration = sailWestward(game, audit);
            sailUntilArrivalTurn(game, duration);
            final Optional<ScreenTurnReport> report = game.nextTurn();
            game.verifyTurnReportPresent(report);
            report.get().verifyOpenEuropeAction("Open Europe");
            final int movement = audit.getMovement();

            // Closing the report shows Europe once; ship and cargo are
            // unchanged.
            final EuropePortScreen europe = report.get().closeIntoEurope();
            audit.verifyInEurope();
            audit.verifyCargo();
            audit.verifyMovement(movement);
            game = europe.close();
            game.verifyGameInFront();
            game.verifyTurnReportNotShown();

            // Continue neither repeats Europe nor the report.
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            game.verifyGameInFront();
            game.verifyTurnReportNotShown();
            audit.verifyMovement(movement);
        } finally {
            restoreDefaultPreferences();
        }
    }

    @Test
    @Tag("local")
    void TC_04_summary_off_arrival_opens_europe_directly_without_a_report()
            throws Exception {
        try {
            // Switch the routine summary off and the Europe arrival option on.
            final SettingPage settings = WelcomePage.of(getContext())
                    .openSetting();
            final FramedSettingsPage page = new FramedSettingsPage(
                    getContext());
            page.press("settingTurnSummary", KeyCode.SPACE);
            page.press("settingOpenEuropeOnArrival", KeyCode.SPACE);
            page.verifyTurnSummary(false);
            page.verifyOpenEuropeOnArrival(true);
            GamePage game = settings.goBack().loadGame();
            final AtlanticAuditPage audit = new AtlanticAuditPage(getContext());

            // No report is due, so the arrival opens Europe by itself and the
            // ship and its cargo are unchanged.
            final int duration = sailWestward(game, audit);
            sailUntilArrivalTurn(game, duration);
            final EuropePortScreen europe = game.nextTurnIntoEurope();
            audit.verifyInEurope();
            audit.verifyCargo();
            final int movement = audit.getMovement();
            game = europe.close();
            game.verifyGameInFront();
            game.verifyTurnReportNotShown();

            // The routine arrival stays in the manual report, once.
            final ScreenTurnReport report = game.openTurnReport();
            report.verifyEventMessageCount("European port", 1);
            report.close();

            // Continue does not open Europe again and spends nothing.
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            game.verifyGameInFront();
            game.verifyTurnReportNotShown();
            audit.verifyMovement(movement);
        } finally {
            restoreDefaultPreferences();
        }
    }

    private void restoreDefaultPreferences() {
        final GamePreferences preferences = getContext()
                .getClassFromGuice(GamePreferences.class);
        preferences
                .setShowTurnSummary(GamePreferences.DEFAULT_SHOW_TURN_SUMMARY);
        preferences.setOpenEuropeOnArrival(
                GamePreferences.DEFAULT_OPEN_EUROPE_ON_ARRIVAL);
    }

    /**
     * Sail the western lane out of Europe as TC_28 does and return how many
     * turns the outward voyage takes.
     */
    private int sailWestward(final GamePage game, final AtlanticAuditPage audit)
            throws Exception {
        // Walk the ship to the departure square of the western lane.
        game.moveMouseAtLocation(Location.of(3, 16));
        game.dragMouseAtLocation(Location.of(2, 16));
        audit.verifyReturnedTo(Location.of(2, 16));
        game.moveMouseAtLocation(Location.of(2, 16));
        game.dragMouseAtLocation(Location.of(2, 17));
        audit.verifyReturnedTo(Location.of(2, 17));
        game.moveMouseAtLocation(Location.of(2, 17));
        game.dragMouseAtLocation(Location.of(3, 17));
        audit.verifyReturnedTo(Location.of(3, 17));
        game.moveMouseAtLocation(Location.of(3, 17));
        game.dragMouseAtLocation(Location.of(2, 17));
        audit.verifyMovement(2);

        // Confirm the outward gesture and keep its sampled duration.
        game.moveMouseAtLocation(Location.of(2, 17));
        game.dragMouseAtLocation(Location.of(1, 17));
        final int outwardDuration = audit.verifyVoyageSample(true, true);
        audit.verifyMovement(1);
        final EuropePortScreen europe = game.openEuroperPort();
        audit.verifyVoyageText("Turns remaining: " + outwardDuration);
        europe.close();
        return outwardDuration;
    }

    /** Advance every turn before the arrival turn, closing their reports. */
    private void sailUntilArrivalTurn(final GamePage game, final int duration)
            throws Exception {
        for (int turn = 1; turn < duration; turn++) {
            game.nextTurnAndCloseDialogs();
        }
    }

}
