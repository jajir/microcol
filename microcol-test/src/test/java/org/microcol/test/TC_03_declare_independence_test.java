package org.microcol.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_03_declare_independence_test extends AbstractMicroColTest {

    private final Logger logger = LoggerFactory.getLogger(TC_03_declare_independence_test.class);

    private final static File testFileName = new File("src/test/scenarios/T03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass());
    }

    @Override
    protected void bind(Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(testFileName);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
    }

    @Test
    void TC_03_declare_independence() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// declare independence
	final DialogMessagePage dialogMessagePage = gamePage.declareIndependence();

	// close first dialog
	dialogMessagePage.close();

	// close second dialog
	dialogMessagePage.close();

	gamePage = GamePage.of(getContext());
	gamePage.nextTurnCloseDialogs();
	gamePage.nextTurnCloseDialogs();
	gamePage.nextTurnCloseDialogs();
	gamePage.nextTurnCloseDialogs();
	gamePage.nextTurnCloseDialogs();
	gamePage.nextTurnCloseDialogs();
	gamePage.nextTurnCloseDialogs();

	assertThatThereAreKingsUnitAtMap();
    }

    private void assertThatThereAreKingsUnitAtMap() {
	final long count = getModel().getAllUnits().stream().filter(unit -> unit.getOwner().getName().contains("King"))
		.filter(unit -> unit.isAtPlaceLocation()).count();
	logger.info("Number of enemy ships at map: " + count);
	assertTrue(count >= 3);
    }

}
