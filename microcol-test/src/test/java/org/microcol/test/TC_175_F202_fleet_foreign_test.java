package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FleetReportPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The fleet report (R-0202) lists a foreign galleon only while the Dutch nation
 * sees it. In the naval fixture the French galleon is in sight with five
 * occupied holds: the report names its tile and that count, and nothing about
 * its cargo, passengers or route. Show opens its tile through the map. When the
 * galleon is gone from the world, its row disappears at the next showing.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_175_F202_fleet_foreign_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F55-naval-combat.microcol");

    /** The French galleon in sight of the Dutch frigate. */
    private static final int GALLEON_ID = 6;

    private static final Location GALLEON_TILE = Location.of(21, 10);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_foreign_ship_in_sight_shows_only_its_occupied_holds()
            throws Exception {
        // The galleon is in sight: its tile and its occupied holds are shown,
        // while the report keeps its cargo, passengers and route hidden.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FleetReportPage fleet = new FleetReportPage(getContext());
        fleet.open();
        fleet.verifyForeignShip(GALLEON_ID, "Map tile 21, 10",
                "Occupied holds: 5; contents are not shown");
        fleet.captureDialog("fleet", "fleet-foreign-english.png");

        // Show opens the galleon's tile on the map; reading changes nothing.
        fleet.showForeign(GALLEON_ID);
        game = GamePage.of(getContext());
        game.verifyThatSelectedTileIs(GALLEON_TILE);
        fleet.open();
        fleet.verifyForeignShip(GALLEON_ID, "Map tile 21, 10",
                "Occupied holds: 5; contents are not shown");
        game = fleet.closeWithEscape();

        // Once the galleon leaves the world, no row for it is shown.
        fleet.removeForeignShip(GALLEON_ID);
        fleet.open();
        fleet.verifyForeignShipAbsent(GALLEON_ID);
        fleet.closeWithEscape();
    }
}
