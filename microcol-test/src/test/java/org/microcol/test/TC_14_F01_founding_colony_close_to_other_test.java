package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Verify that it's not possible to build colony close to other.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_14_F01_founding_colony_close_to_other_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location CLOSE_TO_COLOY_LOCATION = Location.of(21, 12);

    private final static File testFileName = new File("src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_14_founding_colony_close_to_other_test(final FxRobot robot) throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// verify that there in colony are 3 units
	assertEquals(3, getModel().getUnitsAt(COLONY_LOCATION).size());

	// Select second unit. It should be free colonist.
	gamePage.getRightPanelUnit(1).selectUnit();

	// Verify that second unit is selected.
	assertTrue(gamePage.getRightPanelUnit(1).isSelected());

	// Move free colonist next to colony.
	gamePage.moveMouseAtLocation(COLONY_LOCATION);
	gamePage.dragMouseAtLocation(CLOSE_TO_COLOY_LOCATION);

	// Verify that free colonist move to next location.
	assertEquals(1, getModel().getUnitsAt(CLOSE_TO_COLOY_LOCATION).size());

	// Verify that build colony button is hidden.
	gamePage.verifyThatBuildColonyButtonInHidden();
	
	// Press 'b' to build colony. And verify that there is no new colony.
	robot.press(KeyCode.B).sleep(10).release(KeyCode.B);
	gamePage.verifyThatThereIsNoColonyAt(CLOSE_TO_COLOY_LOCATION);

	// Press next turn.
	gamePage.nextTurnAndCloseDialogs();

	// Verify that build colony button is hidden.
	gamePage.verifyThatBuildColonyButtonInHidden();
	
	// Press 'b' to build colony. And verify that there is no new colony.
	robot.press(KeyCode.B).sleep(10).release(KeyCode.B);
	gamePage.verifyThatThereIsNoColonyAt(CLOSE_TO_COLOY_LOCATION);

	// disembark units
	gamePage.moveMouseAtLocation(CLOSE_TO_COLOY_LOCATION);
    }

}
