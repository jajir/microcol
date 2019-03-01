package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.page.DialogTurnReport;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.collect.Lists;
import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_04_loosing_battle_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/test-verify-loosing-battles.microcol");

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
    void TC_04_loosing_battle(final FxRobot robot) throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	gamePage.nextTurn();
	gamePage.nextTurn();

	WaitForAsyncUtils.waitForFxEvents();

	// turn report is shown and contain info about lost colony
	final DialogTurnReport dialogTurnReport = DialogTurnReport.of(getContext());

	// verify that this event is in turn report.
	dialogTurnReport.verifyNumberOfEvents(1);

	// Verify that at least one event contains information about lost colony.
	dialogTurnReport.verifyThatAtLeastOneEventMessageContains("Colony Delft was lost.");

	// close turn report and show game screen
	dialogTurnReport.close();

	// Verify that it's not possible to open colony. Colony was lost just now.
	gamePage.verifyThatItsNotPossibleToOpenColonyAt(Location.of(22, 12));

	// Verify that ship escape lost colony and is at ocean.
	verifyThatEscapedShipIsAtExpectedPlace();

	// open turn dialog again
	final DialogTurnReport dialogTurnReport2 = gamePage.openTurnReport();

	// verify that this event is in turn report.
	dialogTurnReport2.verifyNumberOfEvents(1);

	// Verify that at least one event contains information about lost colony.
	dialogTurnReport2.verifyThatAtLeastOneEventMessageContains("was lost.");

	// close turn report and show game screen
	dialogTurnReport2.close();
    }

    private void verifyThatEscapedShipIsAtExpectedPlace() {
	final List<Location> locations = Lists.newArrayList(Location.of(21, 11), Location.of(22, 11),
		Location.of(23, 11));
	final List<Unit> units = Lists.newArrayList();
	locations.forEach(loc -> units.addAll(getModel().getUnitsAt(loc)));
	final List<Unit> dutchUnits = units.stream().filter(unit -> unit.getOwner().isHuman())
		.collect(Collectors.toList());
	assertEquals(1, dutchUnits.size(), "There is expected exactli one escaped unit");
    }

}
