package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_01_skip_to_next_unit_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);

    private final static File testFileName = new File("src/test/scenarios/T01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass());
    }

    @Override
    protected void bind(Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(testFileName);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
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
