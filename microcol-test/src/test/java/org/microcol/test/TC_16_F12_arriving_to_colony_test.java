	package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that going to colony is correctly counted as normal move.
 * <p>
 * In test ship is moved outside of city. In next turn is verified that ship
 * can't reach city in one move.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_16_F12_arriving_to_colony_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(24, 16);
    private final static Location SHIP_LOCATION = Location.of(25, 16);
    private final static Location FAR_LOCATION = Location.of(31, 15);

    private final static File testFileName = new File("src/test/scenarios/F12-arriving-to-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_embark_disembark() throws Exception {
	// Open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Verify that ship is at correct location.
	assertEquals(1, getModel().getUnitsAt(SHIP_LOCATION).size());

	// Move ship to far location.
	gamePage.moveMouseAtLocation(SHIP_LOCATION);
	gamePage.dragMouseAtLocation(FAR_LOCATION);

	// Verify that ship is at far location.
	assertEquals(1, getModel().getUnitsAt(FAR_LOCATION).size());

	// Perform next turn and ignore turn events dialog.
	gamePage.nextTurnAndCloseDialogs();

	// Move ship to far location.
	gamePage.moveMouseAtLocation(FAR_LOCATION);
	gamePage.dragMouseAtLocationAndClosePossibleDialog(COLONY_LOCATION);
	
	// Verify that ship doesn't moved.
	assertEquals(1, getModel().getUnitsAt(FAR_LOCATION).size());
    }

}
