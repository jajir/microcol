package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogTurnReport;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_10_famine_will_plague_colony_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T10-famine-will-plague.microcol");

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
    void T10_famine_will_plague_colony() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// Verify that in colony is 3 food.
	verifyCornInColony(gamePage, 6);
	
	// Perform next turn and ignore turn events dialog.
	Optional<DialogTurnReport> oTurnDialog = gamePage.nextTurn();
	
	//Verify that turn event dialog is shown.
	assertTrue(oTurnDialog.isPresent());
	
	DialogTurnReport dialogTurnReport = oTurnDialog.get();
	
	dialogTurnReport.verifyThatAtLeastOneEventMessageContains("Famine is coming in colony Delft. In next turn colonist will die.");

    }

    private GamePage verifyCornInColony(final GamePage gamePage, final int expectedFood) {

	// Open colony Delft at [22,12].
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify number of corn is 185.
	colonyScreen.verifyNumberOfGoodsInWrehouse(0, expectedFood);

	// Close colony screen.
	return colonyScreen.close();
    }

}
