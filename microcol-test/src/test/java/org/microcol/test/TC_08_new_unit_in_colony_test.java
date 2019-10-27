package org.microcol.test;

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
public class TC_08_new_unit_in_colony_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T08-new-unit-in-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_08_new_unit_in_colony() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Verify that in colony is 167 food.
	verifyCornInColony(gamePage, 167);

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Verify that in colony is 176 food.
	verifyCornInColony(gamePage, 176);

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Verify that in colony is 185 food.
	verifyCornInColony(gamePage, 185);

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Verify that in colony is 194 food.
	verifyCornInColony(gamePage, 194);

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Verify that in colony is 3 food.
	verifyCornInColony(gamePage, 3);

	// Verify that new unit was born and placed outside colony.
	verifyNumberOfUnitsOutsideColony(gamePage, 3);

    }

    private GamePage verifyCornInColony(final GamePage gamePage, final int expectedFood) {

	// Open colony Delft at [22,12].
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 185.
	colonyScreen.verifyNumberOfGoodsInWrehouse(0, expectedFood);

	// Close colony screen.
	return colonyScreen.close();
    }

    private GamePage verifyNumberOfUnitsOutsideColony(final GamePage gamePage, final int expectedNumberOfUnits) {

	// Open colony Delft at [22,12].
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 'expectedNumberOfUnits'.
	colonyScreen.verifyNumberOfUnitsAtPier(expectedNumberOfUnits);

	// Close colony screen.
	return colonyScreen.close();
    }

}
