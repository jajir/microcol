package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_01_embark_disembark_test extends AbstractMicroColTest {

    private final static File verifyLoadingUnloading = new File(
	    "src/test/scenarios/test-verify-loading-unloading.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass());
    }

    @Override
    protected void bind(Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(verifyLoadingUnloading);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
    }

    @Test
    void TC_01_embark_disembark(final FxRobot robot) throws Exception {
	//open MicroCol and load defined game 
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// go at main game panel
	ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify that there is just one ship in port
	colonyScreen.verifyNumberOfShipsInPort(1);

	// Select first ship in port.
	colonyScreen.selectUnitFromPort(0);

	// drag first units to crate 0
	colonyScreen.dragUnitFromPierToShipCargoSlot(0, 0);

	// drag second units to crate 1
	colonyScreen.dragUnitFromPierToShipCargoSlot(0, 1);

	// return back to main game screen.
	gamePage = colonyScreen.buttonCloseClick();

	// move ship few tiles to the right
	gamePage.moveMouseAtLocation(Location.of(22, 12));
	gamePage.dragMouseAtLocation(Location.of(24, 11));

	// press next turn.
	gamePage.nextTurn();

	// verify that there are no ship
	assertEquals(0, getModel().getUnitsAt(Location.of(24, 12)).size());

	// disembark units
	gamePage.moveMouseAtLocation(Location.of(24, 11));
	gamePage.dragMouseAtLocation(Location.of(24, 12));

	// verify that units are at expected location.
	assertEquals(2, getModel().getUnitsAt(Location.of(24, 12)).size());

	// press next turn.
	gamePage.nextTurn();
	
	//embark units
	gamePage.moveMouseAtLocation(Location.of(24, 12));
	gamePage.dragMouseAtLocation(Location.of(24, 11));
	gamePage.moveMouseAtLocation(Location.of(24, 12));
	gamePage.dragMouseAtLocation(Location.of(24, 11));
	
	// verify that there are no units at map.
	assertEquals(0, getModel().getUnitsAt(Location.of(24, 12)).size());
    }

}
