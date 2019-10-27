package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.common.collect.Lists;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_04_loosing_battle_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T04-loosing-battles.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_04_loosing_battle() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	gamePage.nextTurnAndCloseDialogs();

	final Optional<ScreenTurnReport> oDialog = gamePage.nextTurn();

	// verify that turn report is shown
	assertTrue(oDialog.isPresent());
	final ScreenTurnReport dialogTurnReport = oDialog.get();

	// verify that this event is in turn report.
	dialogTurnReport.verifyNumberOfEvents(2);

	// Verify that at least one event contains information about lost colony.
	dialogTurnReport.verifyThatAtLeastOneEventMessageContains("Colony Delft was lost.");

	// close turn report and show game screen
	dialogTurnReport.close();

	// Verify that it's not possible to open colony. Colony was lost just now.
	gamePage.verifyThatItsNotPossibleToOpenColonyAt(Location.of(22, 12));

	// Verify that ship escape lost colony and is at ocean.
	verifyThatEscapedShipIsAtExpectedPlace();

	// open turn dialog again
	final ScreenTurnReport dialogTurnReport2 = gamePage.openTurnReport();

	// verify that this event is in turn report.
	dialogTurnReport2.verifyNumberOfEvents(2);

	// Verify that at least one event contains information about lost colony.
	dialogTurnReport2.verifyThatAtLeastOneEventMessageContains("was lost.");

	// close turn report and show game screen
	dialogTurnReport2.close();
    }

    private void verifyThatEscapedShipIsAtExpectedPlace() {
	final List<Location> locations = Lists.newArrayList(Location.of(21, 11), Location.of(22, 11),
		Location.of(23, 11));
	final List<Unit> units = new ArrayList<>();
	locations.forEach(loc -> units.addAll(getModel().getUnitsAt(loc)));
	final List<Unit> dutchUnits = units.stream().filter(unit -> unit.getOwner().isHuman())
		.collect(Collectors.toList());
	assertEquals(1, dutchUnits.size(), "There is expected exactli one escaped unit");
    }

}
