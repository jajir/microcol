package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_02_F02_founding_colony_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/F02-founding-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_02_embark_disembark(final FxRobot robot) throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Verify that location is visible at screen
	gamePage.verifyThatTileIsVisible(Location.of(19, 11));

	// verify that there are two units
	gamePage.verifyNumberOfUnitInRightPanel(2);

	// verify that both units have 1 free action point
	gamePage.getRightPanelUnit(0).assertFreeActionPoints(1);
	gamePage.getRightPanelUnit(1).assertFreeActionPoints(1);

	// found colony by pressing key 'b'
	robot.press(KeyCode.B).sleep(10).release(KeyCode.B);
	WaitForAsyncUtils.waitForFxEvents();

	// verify that option build colony is not here
	gamePage.verifyThatBuildColonyButtonIsHidden();

	// verify that one unit was moved into city
	gamePage.verifyNumberOfUnitInRightPanel(1);
	gamePage.getRightPanelUnit(0).assertFreeActionPoints(1);

	// open colony and verify city name
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(19, 11), "Leiden");

	// verify that on filed is exactly one unit, the one that founded colony
	colonyScreen.verifyNumberOfUnitsOnFields(1);

	// verify that action net production is.
	colonyScreen.verifyThatProductionIs(GoodsType.CORN, 2);

	colonyScreen.verifyNumberOfUnitsAtPier(1);

	int cornProduction = colonyScreen.moveUnitFromPietToEmptyField(0);

	// unit will placed at field with production 4 or 3 randomly.
	assertTrue(cornProduction >= 3 && cornProduction <= 4,
		String.format("Unexpected corn production '%s'", cornProduction));
    }

}
