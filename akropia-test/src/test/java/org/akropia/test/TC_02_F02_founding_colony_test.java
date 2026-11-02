package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.page.ColonyNameDialog;
import org.akropia.page.ColonyScreen;
import org.akropia.page.GamePage;
import org.akropia.page.WelcomePage;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_02_F02_founding_colony_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F02-founding-colony.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_02_embark_disembark(final FxRobot robot) throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that location is visible at screen
        gamePage.verifyThatTileIsVisible(Location.of(19, 11));

        // verify that there are two units
        gamePage.verifyNumberOfUnitInRightPanel(2);

        // verify that both units have 1 free action point
        gamePage.getRightPanelUnit(0).verifyFreeActionPoints(1);
        gamePage.getRightPanelUnit(1).verifyFreeActionPoints(1);

        // found colony by pressing key 'b'
        final ColonyNameDialog naming = gamePage.foundColonyFromActions();
        naming.verifyDraft("Leiden");
        naming.save();
        final ColonyScreen opened = ColonyScreen.of(getContext(), "Leiden");
        // The new colony produces its base torch without a preacher.
        opened.verifyTorchProduction(1);
        gamePage = opened.close();

        // verify that option build colony is not here
        gamePage.verifyThatBuildColonyButtonIsHidden();

        // verify that one unit was moved into city
        gamePage.verifyNumberOfUnitInRightPanel(1);
        gamePage.getRightPanelUnit(0).verifyFreeActionPoints(1);

        // open colony and verify city name
        final ColonyScreen colonyScreen = gamePage
                .openColonyAt(Location.of(19, 11), "Leiden");

        // verify that on filed is exactly one unit, the one that founded colony
        colonyScreen.verifyNumberOfUnitsOnFields(1);

        // The grassland center adds its unattended commons, food 4 and grapes
        // 3 with no worker, to the founder's field: net food is the earlier 2
        // plus the 4 from the commons.
        colonyScreen.verifyTownCommons("Grain: 4, Grapes: 3");
        colonyScreen.verifyThatProductionIs(GoodsType.GRAIN, 6);

        colonyScreen.verifyNumberOfUnitsAtPier(1);

        int grainProduction = colonyScreen.moveUnitFromPietToEmptyField(0);

        // The second unit lands on a random free grain field. The returned
        // value
        // is the colony's net food, not the worker's yield: it was 3 or 4
        // before the town commons, and the same 4 commons food now raises it
        // to 7 or 8.
        colonyScreen.verifyProductionInRange(grainProduction, 7, 8);
    }

}
