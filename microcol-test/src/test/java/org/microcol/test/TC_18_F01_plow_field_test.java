package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.RightPanelUnit;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Test verify that when unit starts to plow field that option disapears from
 * top menu.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_18_F01_plow_field_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location NEAR_LOCATION = Location.of(23, 12);

    private final static File testFileName = new File("src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_new_unit_in_colony() throws Exception {
	// Open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Verify that tile with colony is selected.
	gamePage.verifyThatSelectedTileIs(COLONY_LOCATION);

	// Select second unit from right panel. It's free colonist.
	final RightPanelUnit rpu = gamePage.getRightPanelUnit(2);
	rpu.selectUnit();
	rpu.assertFreeActionPoints(1);

	// Verify there is no unit at location near colony..
	assertEquals(0, getModel().getUnitsAt(NEAR_LOCATION).size());

	// Move ship to far location.
	gamePage.moveMouseAtLocation(COLONY_LOCATION);
	gamePage.dragMouseAtLocation(NEAR_LOCATION);

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Verify that unit move to new place.
	assertEquals(1, getModel().getUnitsAt(NEAR_LOCATION).size());

	// Verify that icon for plow field is visible.
	gamePage.verifyThatPlowFieldButtonIsVisible();

	// Press 'p' to plow field.
	gamePage.pressP();

	// Verify that icon for plow field is invisible.
	gamePage.verifyThatPlowFieldButtonIsHidden();
    }

}
