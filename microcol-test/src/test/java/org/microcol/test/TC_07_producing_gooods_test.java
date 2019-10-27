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
public class TC_07_producing_gooods_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T07-producing-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_07_producing_goods() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Open colony Delft at [22,12].
	ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 167.
	colonyScreen.verifyNumberOfGoodsInWrehouse(0, 167);

	// Verify number of tobacco 54.
	colonyScreen.verifyNumberOfGoodsInWrehouse(2, 54);

	// Close colony screen.
	gamePage = colonyScreen.close();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Open colony Delft at [22,12].
	colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 176.
	colonyScreen.verifyNumberOfGoodsInWrehouse(0, 176);

	// Verify number of corn is 63.
	colonyScreen.verifyNumberOfGoodsInWrehouse(2, 63);

	// Close colony screen.
	gamePage = colonyScreen.close();

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Open colony Delft at [22,12].
	colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 185.
	colonyScreen.verifyNumberOfGoodsInWrehouse(0, 185);

	// Verify number of corn is 72.
	colonyScreen.verifyNumberOfGoodsInWrehouse(2, 72);
    }

}
