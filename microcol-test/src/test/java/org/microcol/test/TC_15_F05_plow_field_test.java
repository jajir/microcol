package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Test verify plow field action. Also verify that action points are correctly
 * shown in right panel.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_15_F05_plow_field_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/F05-moving-goods.microcol");

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location NEAR_COLONY = Location.of(23, 12);

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_05_plow_field_test() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Select first unit. It should be free colonist.
	gamePage.getRightPanelUnit(1).selectUnit();

	// Move free colonist to close location.
	gamePage.moveMouseAtLocation(COLONY_LOCATION);
	gamePage.dragMouseAtLocation(NEAR_COLONY);

	// Press 'tab' to select units in colony.
	gamePage.pressTab();

	// Select second unit. It should be free colonist.
	gamePage.getRightPanelUnit(1).selectUnit();

	// Move free colonist to close location.
	gamePage.moveMouseAtLocation(COLONY_LOCATION);
	gamePage.dragMouseAtLocation(NEAR_COLONY);

	// Start new turn and close all dialogs.
	gamePage.nextTurnAndCloseDialogs();

	// Select tile with moved units and select first.
	gamePage.selectTile(NEAR_COLONY);

	// Press 'p' to plow field.
	gamePage.pressP();

	// Verify that selected unit have '0' action points.
	final RightPanelUnit rpu = gamePage.getRightPanelUnit(1);
	assertTrue(rpu.isSelected());
	rpu.assertFreeActionPoints(0);
    }

}
