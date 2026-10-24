package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AtlanticAuditPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FleetReportPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The fleet report (R-0202) follows a real departure on the western sea lane.
 * The galleon is dragged out with the ordinary Atlantic route, and its report
 * line names the voyage to Europe with the same turns that Europe shows. Each
 * turn lowers both by one, Save and Continue keep them, and the arrival puts
 * the galleon in the European port with no voyage line.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_172_F202_fleet_voyage_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F20-western-voyage.microcol");

    private static final int SHIP_ID = 3;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_report_follows_the_departure_until_arrival_in_europe()
            throws Exception {
        // The galleon is moved along the sea lane, as the western voyage test
        // does, and then sent out to Europe through the ordinary drag.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        AtlanticAuditPage audit = new AtlanticAuditPage(getContext());
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
        game.moveMouseAtLocation(Location.of(2, 17));
        game.dragMouseAtLocation(Location.of(1, 17));
        final int outwardDuration = audit.verifyVoyageSample(true, true);
        audit.verifyMovement(1);

        // The report names the sea and the voyage to Europe with the same turns
        // the Europe screen shows; the galleon offers no navigation at sea.
        final FleetReportPage fleet = new FleetReportPage(getContext());
        fleet.open();
        fleet.verifyPlace(SHIP_ID, "On the high seas");
        fleet.verifyVoyage(SHIP_ID,
                "Sailing to Europe, turns left: " + outwardDuration);
        fleet.verifyRepairAbsent(SHIP_ID);
        fleet.verifyShowEnabled(SHIP_ID, false);
        game = fleet.closeWithEscape();
        final EuropePortScreen europe = game.openEuroperPort();
        audit.verifyVoyageText("Turns remaining: " + outwardDuration);
        game = europe.close();

        // Save and Continue keep the voyage line with the same turns.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fleet.open();
        fleet.verifyVoyage(SHIP_ID,
                "Sailing to Europe, turns left: " + outwardDuration);
        game = fleet.closeWithEscape();

        // Each turn lowers the turns left, and the last one reaches Europe.
        for (int remaining = outwardDuration - 1; remaining > 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            fleet.open();
            fleet.verifyVoyage(SHIP_ID,
                    "Sailing to Europe, turns left: " + remaining);
            game = fleet.closeWithEscape();
        }
        game.nextTurnAndCloseDialogs();
        audit.verifyInEurope();
        fleet.open();
        fleet.verifyPlace(SHIP_ID, "European port or pier");
        fleet.verifyVoyageAbsent(SHIP_ID);
        fleet.verifyShowEnabled(SHIP_ID, true);
        game = fleet.closeWithEscape();
    }
}
