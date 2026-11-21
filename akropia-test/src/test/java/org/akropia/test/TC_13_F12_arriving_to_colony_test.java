package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.GamePage;
import org.akropia.page.ColonyScreen;
import org.akropia.page.PassengerTransferPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The carrier enters the colony with its passenger aboard until manual landing.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_13_F12_arriving_to_colony_test extends AbstractAkropiaTest {

    private final static Location COLONY_LOCATION = Location.of(24, 16);
    private final static Location SHIP_LOCATION = Location.of(25, 16);

    private final static File testFileName = new File(
            "src/test/scenarios/F12-arriving-to-colony.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_embark_disembark() throws Exception {
        // Open Akropia and load defined game
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

        // Arrival keeps the passenger aboard until an explicit transfer.
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 1);
        passengers.verifyShip(COLONY_LOCATION, 5);
        gamePage.getShipAt(COLONY_LOCATION).verifyNumberOfOccupiedCargoSlots(1);
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION,
                "New Amsterdam");
        passengers.disembarkInPort(4, 1, false);
        colony.verifyNumberOfUnitsAtPier(1);
        gamePage = colony.close();
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 2);
        passengers.verifyAshore(4, COLONY_LOCATION, 1, 0);
        passengers.verifyCargoSlotEmpty(3, 1);

        // Verify that ship was moved.
        gamePage.verifyNumberOfUnitsAt(SHIP_LOCATION, 0);
    }

}
