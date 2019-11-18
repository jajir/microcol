package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_01_embark_disembark_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location SHORE_LAND_LOCATION = Location.of(24, 12);
    private final static Location SHORE_SEE_LOCATION = Location.of(24, 11);

    private final static File testFileName = new File("src/test/scenarios/T01-embark-disembark.microcol");

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
	assertEquals(3, getModel().getUnitsAt(COLONY_LOCATION).size());

	// go at main game panel
	ColonyScreen colonyScreen = gamePage.openColonyAt(COLONY_LOCATION, "Delft");

	// Verify that there is just one ship in port
	colonyScreen.verifyNumberOfShipsInPort(1);

	// drag first units to crate 0
	colonyScreen.dragUnitFromPierToShipCargoSlot(0, 0);

	// drag second units to crate 1
	colonyScreen.dragUnitFromPierToShipCargoSlot(0, 1);

	// return back to main game screen.
	gamePage = colonyScreen.close();

	// verify that just on unit is in colony
	assertEquals(1, getModel().getUnitsAt(COLONY_LOCATION).size());

	// move ship few tiles to the right
	gamePage.moveMouseAtLocation(COLONY_LOCATION);
	gamePage.dragMouseAtLocation(SHORE_SEE_LOCATION);

	// press next turn.
	gamePage.nextTurnAndCloseDialogs();

	// verify that there are no ship
	assertEquals(0, getModel().getUnitsAt(SHORE_LAND_LOCATION).size());

	// disembark units
	gamePage.moveMouseAtLocation(SHORE_SEE_LOCATION);
	gamePage.dragMouseAtLocation(SHORE_LAND_LOCATION);

	// verify that units are at expected location.
	assertEquals(2, getModel().getUnitsAt(SHORE_LAND_LOCATION).size());

	// press next turn.
	gamePage.nextTurnAndCloseDialogs();

	// embark units
	gamePage.moveMouseAtLocation(SHORE_LAND_LOCATION);
	gamePage.dragMouseAtLocation(SHORE_SEE_LOCATION);
	gamePage.moveMouseAtLocation(SHORE_LAND_LOCATION);
	gamePage.dragMouseAtLocation(SHORE_SEE_LOCATION);

	// Verify that there are no units at continent. Units was embarked.
	assertEquals(0, getModel().getUnitsAt(SHORE_LAND_LOCATION).size());

	// Go back to town with ship.
	gamePage.moveMouseAtLocation(SHORE_SEE_LOCATION);
	gamePage.dragMouseAtLocation(COLONY_LOCATION);

	// Verify that just one unit is in colony.
	assertEquals(1, getModel().getUnitsAt(COLONY_LOCATION).size());
    }

}
