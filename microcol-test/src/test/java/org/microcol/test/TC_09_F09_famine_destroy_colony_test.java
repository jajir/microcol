package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_09_F09_famine_destroy_colony_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F09-famine-destroy-colony.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_09_famine_destroy_colony() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Verify that in colony is 3 food.
        verifyCornInColony(gamePage, 3);

        // End the turn and verify the recorded famine outcome.
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        gamePage.verifyTurnReportPresent(report);
        report.get().verifyFamineLossCount(1);
        report.get().verifyThatAtLeastOneEventMessageContains(
                "Famine in colony Delft killed one");
        report.get().verifyThatAtLeastOneEventMessageContains("Final stocks:");
        report.get().close();

        /*
         * The victim is random. The report records the loss and final stocks;
         * later turns continue until the colony disappears.
         */

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify colony doesn't exists anymore.
        gamePage.selectTile(Location.of(22, 12));
        gamePage.verifyMapRemainsVisibleAt(Location.of(22, 12));

    }

    private GamePage verifyCornInColony(final GamePage gamePage,
            final int expectedFood) {

        // Open colony Delft at [22,12].
        final ColonyScreen colonyScreen = gamePage
                .openColonyAt(Location.of(22, 12), "Delft");

        // Verify number of corn is 185.
        colonyScreen.verifyNumberOfGoodsInWrehouse(0, expectedFood);

        // Close colony screen.
        return colonyScreen.close();
    }

}
