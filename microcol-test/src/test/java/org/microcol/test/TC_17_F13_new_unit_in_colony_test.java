package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that newly created unit is also focused.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_17_F13_new_unit_in_colony_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(24, 16);
    private final static Location NEAR_LOCATION = Location.of(23, 16);

    private final static File testFileName = new File(
            "src/test/scenarios/F13-new-unit-in-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_new_unit_in_colony() throws Exception {
        // Open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Verify that tile with colony is selected.
        gamePage.verifyThatSelectedTileIs(COLONY_LOCATION);

        // Verify there is not unit in colony.
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 0);

        // Starting food 195 plus net 9 reaches 204 and produces one colonist.
        gamePage.nextTurnAndCloseDialogs();

        // Verify new unit in colony.
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 1);

        // Move the automatically focused newborn without selecting it again.
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);

        // Verify that unit move to new place.
        gamePage.verifyNumberOfUnitsAt(NEAR_LOCATION, 1);
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 0);
    }

}
