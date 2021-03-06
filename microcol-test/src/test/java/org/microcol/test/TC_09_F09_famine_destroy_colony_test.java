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
public class TC_09_F09_famine_destroy_colony_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/F09-famine-destroy-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_09_famine_destroy_colony() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Verify that in colony is 3 food.
	verifyCornInColony(gamePage, 3);

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	/*
	 * From now on, can't verify number of corn in colony. Unit whom die because of
	 * famine is random. It could be unit from field and even from factory.
	 */
	
	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();
	
	// Verify colony doesn't exists anymore.
	gamePage.verifyThatItsNotPossibleToOpenColonyAt(Location.of(22, 12));

    }

    private GamePage verifyCornInColony(final GamePage gamePage, final int expectedFood) {

	// Open colony Delft at [22,12].
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 185.
	colonyScreen.verifyNumberOfGoodsInWrehouse(0, expectedFood);

	// Close colony screen.
	return colonyScreen.close();
    }

}
