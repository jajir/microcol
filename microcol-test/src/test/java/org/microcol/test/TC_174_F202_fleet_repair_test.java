package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.FleetReportPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The fleet report (R-0202) shows a damaged galleon in the Europe repair of the
 * saved fixture. Its place is the repair, its line names the owner updates left
 * and the destination, and its cargo stays as it was. Each real owner turn
 * lowers the updates left, and Save and Continue keep them. The Show button is
 * unavailable because a ship under repair has no destination to open.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_174_F202_fleet_repair_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F54-ship-repair.microcol");

    private static final int SHIP_ID = 3;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_repair_report_counts_owner_updates_and_keeps_cargo()
            throws Exception {
        // The galleon is in its transfer to the Europe repair with three owner
        // updates left; its cargo remains aboard.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FleetReportPage fleet = new FleetReportPage(getContext());
        fleet.open();
        fleet.verifyPlace(SHIP_ID, "Ship under repair");
        fleet.verifyRepair(SHIP_ID,
                "Under repair, updates left: 3, destination: Europe");
        fleet.verifyCargo(SHIP_ID,
                "Passengers: 1 (Free Colonist: 1); goods: Ore 7");
        fleet.verifyVoyageAbsent(SHIP_ID);
        fleet.verifyShowEnabled(SHIP_ID, false);
        fleet.captureDialog("fleet", "fleet-repair-english.png");
        game = fleet.closeWithEscape();

        // The first owner turn completes the transfer; the repair then has two
        // updates left, as the repair page shows.
        game.nextTurnAndCloseDialogs();
        fleet.open();
        fleet.verifyRepair(SHIP_ID,
                "Under repair, updates left: 2, destination: Europe");
        game = fleet.closeWithEscape();

        // Save and Continue keep the remaining update; the next turn lowers it
        // to one.
        game.nextTurnAndCloseDialogs();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fleet.open();
        fleet.verifyRepair(SHIP_ID,
                "Under repair, updates left: 1, destination: Europe");
        fleet.verifyCargo(SHIP_ID,
                "Passengers: 1 (Free Colonist: 1); goods: Ore 7");
        fleet.closeWithEscape();
    }
}
