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

/** R-0405: local fishing is managed through its receiving colony. */
@ExtendWith(ApplicationExtension.class)
public class TC_202_F234_local_fishing_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F234-local-fishing.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_map_fisherman_opens_its_colony_and_cancelled_boat_can_return()
            throws Exception {
        // Clicking the boat opens its home city and selects this fisherman.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        work.verifyFishing(0, 0, true);
        final ColonyScreen colony = game.openColonyAt(Location.of(23, 16),
                "New Amsterdam");
        work.verifySelectedFieldWorker();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0405-fisherman-selected.png");

        // Canceling leaves the local boat visible, with no production or food
        // consumption.
        colony.openCropMenu(0);
        work.choose("cancelColonyWork");
        work.verifyFishing(0, 0, false);
        work.verifyFood(0, 2);
        work.verifyStock(0, 100);

        // The idle boat remains manageable; returning costs the one available
        // point.
        colony.openCropMenu(0);
        work.choose("returnToColony");
        work.verifyReturned(0, Location.of(24, 16));
        work.verifyFood(0, 2);
    }
}
