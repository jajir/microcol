package org.akropia.test;

import java.io.File;

import org.akropia.model.Location;
import org.akropia.page.ColonyScreen;
import org.akropia.page.GamePage;
import org.akropia.page.WelcomePage;
import org.akropia.page.WorldWorkersPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0414: map selection preserves work; opening a city needs a double click.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_204_F236_map_selection_work_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F236-map-selection-work.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_single_click_selects_city_stack_double_click_opens_city() {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        final Location city = Location.of(24, 16);

        // A single click keeps the map and selects the idle unit on the city.
        game.selectTile(city);
        work.verifyMapSelection(5, city, "", null, 15);

        // A double click opens the city despite the unit; neither spends moves.
        final ColonyScreen colony = game.openColonyAt(city, "New Amsterdam");
        colony.close();
        work.verifyWorker(5, city, "", null, 15);
    }

    @Test
    @Tag("local")
    void TC_02_land_work_survives_click_drag_and_keyboard_until_cancelled()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        final Location field = Location.of(23, 16);
        final Location target = Location.of(23, 15);

        // Selecting and dragging a working farmer must preserve the job.
        game.selectTile(field);
        work.verifyMapSelection(0, field, "w", 0, 3);
        work.verifyRecipientInPanel(0, "Works for New Amsterdam");
        game.dragToLocation(target);
        work.verifyMapSelection(0, field, "w", 0, 3);

        // The movement shortcut and Enter cannot bypass the same lock.
        work.pressMovementShortcut();
        game.hoverTile(target);
        work.confirmMovement();
        work.verifyMapSelection(0, field, "w", 0, 3);

        // Explicit cancellation leaves position and movement intact.
        game.openUnitContextAt(field);
        work.verifyMenuAction("cancelColonyWork", "Cancel work", false);
        work.choose("cancelColonyWork");
        work.verifyWorker(0, field, "", null, 3);
        work.verifyRecipientNotInPanel(0, true);

        // The same ordinary drag now moves the farmer and pays terrain cost.
        game.hoverTile(field);
        game.dragMouseAtLocation(target);
        work.verifyWorker(0, target, "", null, 0);
    }

    @Test
    @Tag("local")
    void TC_03_pioneer_work_survives_selection_and_only_context_cancels_it()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        final Location field = Location.of(22, 16);
        final Location target = Location.of(22, 15);

        // A pioneer with refreshed movement still retains its plowing order.
        game.selectTile(field);
        game.dragToLocation(target);
        work.pressMovementShortcut();
        work.verifyMapSelection(6, field, "p", null, 3);

        // Canceling is explicit, named consistently, and does not refund moves.
        game.openUnitContextAt(field);
        work.verifyMenuAction("cancelPlowField", "Cancel work", false);
        work.choose("cancelPlowField");
        work.verifyWorker(6, field, "", null, 3);
        game.hoverTile(field);
        game.dragMouseAtLocation(target);
        work.verifyWorker(6, target, "", null, 0);
    }
}
