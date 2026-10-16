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
public class TC_10_F10_famine_will_plague_colony_test
        extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F10-famine-will-plague.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void T10_famine_will_plague_colony() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // One town-hall resident has two food, enough for this turn only.
        verifyCornInColony(gamePage, 2);

        // Perform next turn and ignore turn events dialog.
        Optional<ScreenTurnReport> oTurnDialog = gamePage.nextTurn();

        // Verify that turn event dialog is shown.
        gamePage.verifyTurnReportPresent(oTurnDialog);

        ScreenTurnReport dialogTurnReport = oTurnDialog.get();
        dialogTurnReport.verifyReady();

        dialogTurnReport.verifyThatAtLeastOneEventMessageContains(
                "Famine is coming in colony Delft. In next turn colonist will die.");

        // Confirm that the next colony forecast marks the variable outcome.
        dialogTurnReport.close();
        final ColonyScreen colonyAfterWarning = gamePage
                .openColonyAt(Location.of(22, 12), "Delft");
        colonyAfterWarning.verifyFamineForecast();
        colonyAfterWarning.close();

        // The next turn starves that final resident and removes the port.
        final Optional<ScreenTurnReport> lossReport = gamePage.nextTurn();
        gamePage.verifyTurnReportPresent(lossReport);
        lossReport.get().verifyFamineLossCount(1);
        lossReport.get().verifyThatAtLeastOneEventMessageContains(
                "Colony Delft was destroyed.");
        lossReport.get().close();

        gamePage.verifyThatThereIsNoColonyAt(Location.of(22, 12));
        gamePage.verifyNumberOfUnitsAt(Location.of(22, 12), 3);
        gamePage.verifyNumberOfUnitsAt(Location.of(21, 11), 1);
        gamePage.selectTile(Location.of(22, 12));
        gamePage.verifyNumberOfUnitInRightPanel(3);
        gamePage.pressTab();
        gamePage.verifyOneUnitIsSelected();
        gamePage.nextTurnAndCloseDialogs();

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
