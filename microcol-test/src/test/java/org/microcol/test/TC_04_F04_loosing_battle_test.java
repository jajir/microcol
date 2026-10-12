package org.microcol.test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_04_F04_loosing_battle_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F04-loosing-battles.microcol");

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
        gamePage.verifyTurnReportPresent(oDialog);
        final ScreenTurnReport dialogTurnReport = oDialog.get();
        dialogTurnReport.verifyReady();

        // verify that this event is in turn report.
        dialogTurnReport.verifyNumberOfEvents(2);

        // Verify that at least one event contains information about lost
        // colony.
        dialogTurnReport.verifyThatAtLeastOneEventMessageContains(
                "Colony Delft was lost.");

        // close turn report and show game screen
        dialogTurnReport.close();

        // Verify that it's not possible to open colony. Colony was lost just
        // now.
        gamePage.selectTile(Location.of(22, 12));
        gamePage.verifyMapRemainsVisibleAt(Location.of(22, 12));

        // Verify that ship escape lost colony and is at ocean.
        gamePage.verifyNumberOfHumanUnitsAt(List.of(Location.of(21, 11),
                Location.of(22, 11), Location.of(23, 11)), 1);

        // open turn dialog again
        final ScreenTurnReport dialogTurnReport2 = gamePage.openTurnReport();
        dialogTurnReport2.verifyReady();

        // verify that this event is in turn report.
        dialogTurnReport2.verifyNumberOfEvents(2);

        // Verify that at least one event contains information about lost
        // colony.
        dialogTurnReport2.verifyThatAtLeastOneEventMessageContains("was lost.");

        // close turn report and show game screen
        dialogTurnReport2.close();
    }

}
