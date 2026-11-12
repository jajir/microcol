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
        naming.verifyDraft("Massalia");
        naming.save();
        final ColonyScreen opened = ColonyScreen.of(getContext(), "Massalia");
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
                .openColonyAt(Location.of(19, 11), "Massalia");

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

    @Test
    @Tag("local")
    void TC_02_exhausted_founder_explains_without_changing_the_world()
            throws Exception {
        // Load the same founding site with the movement already spent.
        FoundingMovementFixture.exhaust(getContext(), testFileName);
        WelcomePage.of(getContext()).loadGame();
        final org.akropia.page.FoundingMovementPage founding = new org.akropia.page.FoundingMovementPage(
                getContext());
        founding.rememberWorld();
        // The disabled action retains a visible explanation; B works in its
        // menu.
        founding.verifyWaitingAction();
        founding.pressFoundingShortcut();
        founding.verifyInformation();
        founding.dismiss(javafx.scene.input.KeyCode.ENTER);
        founding.verifyInformationClosed();
        founding.verifyWorldUnchanged();
        // The same shortcut on the map opens a single dismissible notice.
        founding.pressFoundingShortcut();
        founding.verifyInformation();
        founding.dismiss(javafx.scene.input.KeyCode.ESCAPE);
        founding.verifyInformationClosed();
        founding.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_03_czech_exhausted_founder_recovers_after_continue_and_next_turn()
            throws Exception {
        // The Czech player sees the same reason and dismisses it with OK.
        FoundingMovementFixture.exhaust(getContext(), testFileName);
        final org.akropia.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final int founderId = getModel().getUnitsAt(Location.of(19, 11)).get(0)
                .getId();
        game.selectMapUnit(founderId);
        final org.akropia.page.FoundingMovementPage founding = new org.akropia.page.FoundingMovementPage(
                getContext());
        founding.rememberWorld();
        founding.verifyWaitingAction("nemá zbývající pohyb");
        founding.pressFoundingShortcut();
        founding.verifyInformation("nemá zbývající pohyb");
        founding.dismissWithOk();
        founding.verifyInformationClosed();
        founding.verifyWorldUnchanged();

        // Continue derives availability from the saved exhausted unit.
        game.exitToMenu();
        new org.akropia.page.FreePlayAuditPage(getContext()).continueGame();
        game.selectMapUnit(founderId);
        founding.verifyWaitingAction("nemá zbývající pohyb");
        founding.dismiss(javafx.scene.input.KeyCode.ESCAPE);

        // Restored movement enables the normal naming flow, which can cancel.
        game.nextTurnAndCloseDialogs();
        game.selectMapUnit(founderId);
        founding.verifyFoundingAvailable();
        founding.pressFoundingShortcut();
        final ColonyNameDialog naming = new ColonyNameDialog(getContext());
        naming.verifyDraft("Massalia");
        naming.cancel();
    }

}
