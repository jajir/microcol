package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The carrier enters the colony before its passenger steps ashore.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_13_F12_arriving_to_colony_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(24, 16);
    private final static Location SHIP_LOCATION = Location.of(25, 16);

    private final static File testFileName = new File(
            "src/test/scenarios/F12-arriving-to-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_embark_disembark() throws Exception {
        // Open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Verify that there are no units in colony.
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 0);

        // Verify that ship is at correct location.
        gamePage.verifyNumberOfUnitsAt(SHIP_LOCATION, 1);
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        gamePage.getShipAt(SHIP_LOCATION).verifyNumberOfOccupiedCargoSlots(1);

        // Move ship to colony
        gamePage.moveMouseAtLocation(SHIP_LOCATION);
        gamePage.dragMouseAtLocation(COLONY_LOCATION);

        // The ship arrived and its passenger stepped outside the workforce.
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 2);
        passengers.verifyShip(COLONY_LOCATION, 5);
        passengers.verifyAshore(4, COLONY_LOCATION, 1, 0);
        passengers.verifyCargoSlotEmpty(3, 1);

        // Verify that ship was moved.
        gamePage.verifyNumberOfUnitsAt(SHIP_LOCATION, 0);
    }

}
