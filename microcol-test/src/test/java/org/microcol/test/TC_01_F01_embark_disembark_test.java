package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_01_F01_embark_disembark_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location SHORE_LAND_LOCATION = Location.of(24, 12);
    private final static Location SHORE_SEE_LOCATION = Location.of(24, 11);

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_embark_disembark() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // verify that there in colony are 3 units
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 3);

        // go at main game panel
        ColonyScreen colonyScreen = gamePage.openColonyAt(COLONY_LOCATION,
                "Delft");

        // Verify that there is just one ship in port
        colonyScreen.verifyNumberOfShipsInPort(1);

        // drag first units to crate 0
        colonyScreen.dragUnitFromPierToShipCargoSlot(0, 0);

        // drag second units to crate 1
        colonyScreen.dragUnitFromPierToShipCargoSlot(0, 1);

        // return back to main game screen.
        gamePage = colonyScreen.close();

        // verify that just on unit is in colony
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 1);

        // move ship few tiles to the right
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(SHORE_SEE_LOCATION);

        // press next turn.
        gamePage.nextTurnAndCloseDialogs();

        // verify that there are no ship
        gamePage.verifyNumberOfUnitsAt(SHORE_LAND_LOCATION, 0);

        // Verify that ship have occupied two cargo slots.
        gamePage.verifyShipPresentAt(SHORE_SEE_LOCATION);
        gamePage.getShipAt(SHORE_SEE_LOCATION)
                .verifyNumberOfOccupiedCargoSlots(2);

        // disembark units
        gamePage.moveMouseAtLocation(SHORE_SEE_LOCATION);
        gamePage.dragMouseAtLocation(SHORE_LAND_LOCATION);
        // Select and confirm each passenger separately.
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        passengers.choose(24);
        passengers.confirm();
        gamePage.waitForNumberOfUnitsAt(SHORE_LAND_LOCATION, 1);
        gamePage.moveMouseAtLocation(SHORE_SEE_LOCATION);
        gamePage.dragMouseAtLocation(SHORE_LAND_LOCATION);
        passengers.choose(25);
        passengers.confirm();
        gamePage.waitForNumberOfUnitsAt(SHORE_LAND_LOCATION, 2);

        // verify that units are at expected location.
        gamePage.verifyNumberOfUnitsAt(SHORE_LAND_LOCATION, 2);

        // press next turn.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that ship have all cargo slots empty.
        gamePage.verifyShipPresentAt(SHORE_SEE_LOCATION);
        gamePage.getShipAt(SHORE_SEE_LOCATION)
                .verifyNumberOfOccupiedCargoSlots(0);

        // embark units
        gamePage.moveMouseAtLocation(SHORE_LAND_LOCATION);
        gamePage.dragMouseAtLocation(SHORE_SEE_LOCATION);
        gamePage.moveMouseAtLocation(SHORE_LAND_LOCATION);
        gamePage.dragMouseAtLocation(SHORE_SEE_LOCATION);

        // Verify that ship have occupied two cargo slots.
        gamePage.verifyShipPresentAt(SHORE_SEE_LOCATION);
        gamePage.getShipAt(SHORE_SEE_LOCATION)
                .verifyNumberOfOccupiedCargoSlots(2);

        // Verify that there are no units at continent.
        gamePage.verifyNumberOfUnitsAt(SHORE_LAND_LOCATION, 0);

        // Go back to town with ship.
        gamePage.moveMouseAtLocation(SHORE_SEE_LOCATION);
        gamePage.dragMouseAtLocation(COLONY_LOCATION);

        // Arrival lands both passengers outside; the carrier's holds are free.
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 3);
        gamePage.getShipAt(COLONY_LOCATION).verifyNumberOfOccupiedCargoSlots(0);
    }

}
