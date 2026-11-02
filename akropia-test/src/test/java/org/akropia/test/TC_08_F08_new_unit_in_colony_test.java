package org.akropia.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.ColonyScreen;
import org.akropia.page.ColonyJobPage;
import org.akropia.page.GamePage;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_08_F08_new_unit_in_colony_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F08-new-unit-in-colony.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_08_new_unit_in_colony() throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Verify that in colony is 190 food.
        verifyGrainInColony(gamePage, 190);

        // Reopening the colony does not run its food update.
        ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12),
                "Delft");
        colonyScreen.rememberWorld();
        final ColonyScreen rememberedScreen = colonyScreen;
        gamePage = colonyScreen.close();
        colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");
        rememberedScreen.verifyWorldUnchanged();
        gamePage = colonyScreen.close();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that in colony is 193 food.
        verifyGrainInColony(gamePage, 193);

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that in colony is 196 food.
        verifyGrainInColony(gamePage, 196);

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that in colony is 199 food.
        verifyGrainInColony(gamePage, 199);

        // The fourth update crosses 200 food: the report names the colony of
        // the birth once and offers to open it.
        final Optional<ScreenTurnReport> birth = gamePage.nextTurn();
        gamePage.verifyTurnReportPresent(birth);
        birth.get().verifyEventMessageCount(
                "There is a new unit in colony Delft.", 1);
        birth.get().verifyOpenColonyActionFor("Delft", "Open colony");
        birth.get().close();

        // Four +3 updates cross 200 food; the birth leaves exactly 2.
        verifyGrainInColony(gamePage, 2);

        // Verify that new unit was born and placed outside colony.
        verifyNumberOfUnitsOutsideColony(gamePage, 3);

        // The newborn's job choices can be opened and canceled at the pier.
        colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        jobs.openFromCenter(27);
        jobs.verifyOpen();
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        colonyScreen.close();

    }

    private GamePage verifyGrainInColony(final GamePage gamePage,
            final int expectedFood) {

        // Open colony Delft at [22,12].
        final ColonyScreen colonyScreen = gamePage
                .openColonyAt(Location.of(22, 12), "Delft");

        // Verify the expected food stock.
        colonyScreen.verifyNumberOfGoodsInWrehouse(0, expectedFood);

        // Close colony screen.
        return colonyScreen.close();
    }

    private GamePage verifyNumberOfUnitsOutsideColony(final GamePage gamePage,
            final int expectedNumberOfUnits) {

        // Open colony Delft at [22,12].
        final ColonyScreen colonyScreen = gamePage
                .openColonyAt(Location.of(22, 12), "Delft");

        // Verify number of grain is 'expectedNumberOfUnits'.
        colonyScreen.verifyNumberOfUnitsAtPier(expectedNumberOfUnits);

        // Close colony screen.
        return colonyScreen.close();
    }

}
