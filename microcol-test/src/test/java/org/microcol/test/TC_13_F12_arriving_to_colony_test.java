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
 * Verify that ship arriving to colony. When user drag ship to colony unit
 * should not disembark before colony.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_13_F12_arriving_to_colony_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(24, 16);
    private final static Location SHIP_LOCATION = Location.of(25, 16);

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

	// Verify that there are no units in colony.
	assertEquals(0, getModel().getUnitsAt(COLONY_LOCATION).size());

	// Verify that ship is at correct location.
	assertEquals(1, getModel().getUnitsAt(SHIP_LOCATION).size());

	// Move ship to colony
	gamePage.moveMouseAtLocation(SHIP_LOCATION);
	gamePage.dragMouseAtLocation(COLONY_LOCATION);

	// Verify that one more unit is in colony.
	assertEquals(1, getModel().getUnitsAt(COLONY_LOCATION).size());

	// Verify that ship was moved.
	assertEquals(0, getModel().getUnitsAt(SHIP_LOCATION).size());
    }

}
