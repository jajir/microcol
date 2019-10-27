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

@ExtendWith(ApplicationExtension.class)
public class TC_11_skip_to_next_unit_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);

    private final static File testFileName = new File("src/test/scenarios/T11-ship_to_next_unit.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_embark_disembark() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// verify that there in colony are 3 units
	assertEquals(3, getModel().getUnitsAt(COLONY_LOCATION).size());

	//verify that first unit is selected
	gamePage.assertThatUnitIsSelected(0);
	
	gamePage.pressTab();
	
	//verify that second unit is selected
	gamePage.assertThatUnitIsSelected(1);

	gamePage.pressTab();
	
	//verify that third unit is selected
	gamePage.assertThatUnitIsSelected(2);

	gamePage.pressTab();
	
	//verify that first unit is selected
	gamePage.assertThatUnitIsSelected(0);
    }

}
