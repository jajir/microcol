package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_08_F08_new_unit_in_colony_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F08-new-unit-in-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_08_new_unit_in_colony() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Verify that in colony is 190 food.
        verifyCornInColony(gamePage, 190);

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
        verifyCornInColony(gamePage, 193);

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that in colony is 196 food.
        verifyCornInColony(gamePage, 196);

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that in colony is 199 food.
        verifyCornInColony(gamePage, 199);

        // The fourth update crosses 200 food: the report names the colony of
        // the birth once and offers to open it.
        final Optional<ScreenTurnReport> birth = gamePage.nextTurn();
        gamePage.verifyTurnReportPresent(birth);
        birth.get().verifyEventMessageCount(
                "There is a new unit in colony Delft.", 1);
        birth.get().verifyOpenColonyActionFor("Delft", "Open colony");
        birth.get().close();

        // Four +3 updates cross 200 food; the birth leaves exactly 2.
        verifyCornInColony(gamePage, 2);

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

    private GamePage verifyCornInColony(final GamePage gamePage,
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

        // Verify number of corn is 'expectedNumberOfUnits'.
        colonyScreen.verifyNumberOfUnitsAtPier(expectedNumberOfUnits);

        // Close colony screen.
        return colonyScreen.close();
    }

}
