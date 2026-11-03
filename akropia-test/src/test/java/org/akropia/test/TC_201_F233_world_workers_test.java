package org.akropia.test;

import java.io.File;
import org.akropia.model.Location;
import org.akropia.page.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * R-0405, UC-032/114/119: one outdoor worker, one job and one receiving colony.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_201_F233_world_workers_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F233-world-workers.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_and_reassign_at_zero_without_movement_or_immediate_production()
            throws Exception {
        // The map shows the same worker that supplies the colony, even at zero
        // movement.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        final Location field = Location.of(23, 16);
        work.verifyWorker(0, field, "w", 0, 0);
        work.verifyFood(0, 4);
        game.selectMapUnit(0);
        new org.akropia.page.UnitTurnOrdersPage(getContext())
                .verifySelectedUnit(0);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0405-working-on-map.png");

        // A field assigned to the other city hides all detail in this city.
        final ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        colony.verifyFieldPreview(org.akropia.model.Direction.east, true);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0405-other-city-field-grey.png");
        colony.close();

        // Canceling the job leaves the person at the same square and ends its
        // food consumption.
        game.openUnitContextAt(field);
        work.choose("cancelColonyWork");
        work.verifyWorker(0, field, "", null, 0);
        work.verifyFood(0, 2);
        work.verifyStock(0, 100);

        // The menu explicitly identifies the receiving colony; assigning work
        // is free at this square.
        game.openUnitContextAt(field);
        work.choose("mapJob-0-GRAIN");
        work.verifyWorker(0, field, "w", 0, 0);
        work.verifyFood(0, 4);
        work.verifyStock(0, 100);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0405-reassigned-on-map.png");
    }
}
