package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FleetReportPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.VoyageReversalPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The fleet report (R-0202) follows a voyage that the player turns around in
 * Europe. Before the confirmation the report names the voyage to Europe; after
 * it, the report names the turned-around voyage to the colonies with the same
 * turns the Europe screen shows. Save and Continue keep the turned voyage, and
 * the ship arrives back on its map tile with no voyage line.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_173_F202_fleet_reversal_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F35-voyage-reversal.microcol");

    private static final int SHIP_ID = 3;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_report_follows_the_turned_voyage_and_continue()
            throws Exception {
        // The stored voyage has spent one of four updates toward Europe.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FleetReportPage fleet = new FleetReportPage(getContext());
        fleet.open();
        fleet.verifyPlace(SHIP_ID, "On the high seas");
        fleet.verifyVoyage(SHIP_ID, "Sailing to Europe, turns left: 3");
        fleet.verifyShowEnabled(SHIP_ID, false);
        game = fleet.closeWithEscape();

        // The Europe screen offers the reversal; the confirmation is the same
        // one the voyage test shows.
        final EuropePortScreen europe = game.openEuroperPort();
        final VoyageReversalPage voyage = new VoyageReversalPage(getContext());
        voyage.verifyTransit(true, 1, 3, "Turns remaining: 3");
        voyage.openReverse();
        voyage.verifyConfirmation("Reverse course",
                "Send this ship to New World? Turns remaining: 1.");
        voyage.confirmReverse();
        voyage.verifyTransit(false, 1, 1, "Turns remaining: 1");
        game = europe.close();

        // The report names the turned voyage and the turns that Europe shows.
        fleet.open();
        fleet.verifyVoyage(SHIP_ID,
                "Sailing back to the colonies, turns left: 1, turned around");
        game = fleet.closeWithEscape();

        // Save and Continue keep the turned voyage with the same turns.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fleet.open();
        fleet.verifyVoyage(SHIP_ID,
                "Sailing back to the colonies, turns left: 1, turned around");
        game = fleet.closeWithEscape();

        // The next owner turn brings the ship back to the recorded coast.
        game.nextTurnAndCloseDialogs();
        fleet.open();
        fleet.verifyPlace(SHIP_ID, "Map tile 2, 17");
        fleet.verifyVoyageAbsent(SHIP_ID);
        fleet.verifyShowEnabled(SHIP_ID, true);
        fleet.closeWithEscape();
    }
}
