package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.GamePage;
import org.microcol.page.PlowPage;
import org.microcol.page.RightPanelUnit;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Test verify plow field action. Also verify that action points are correctly
 * shown in right panel.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_15_F05_plow_field_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F05-moving-goods.microcol");

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location NEAR_COLONY = Location.of(23, 12);

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_05_plow_field_test() throws Exception {
        // Equip the colonists of a scoped copy; the shared scenario is kept.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f05-start.microcol", 100, null, NEAR_COLONY);

        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Select first unit. It should be free colonist.
        gamePage.getRightPanelUnit(1).selectUnit();

        // Move free colonist to close location.
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_COLONY);

        // Press 'tab' to select units in colony.
        gamePage.pressTab();

        // Select second unit. It should be free colonist.
        gamePage.getRightPanelUnit(1).selectUnit();

        // Move free colonist to close location.
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_COLONY);

        // Start new turn and close all dialogs.
        gamePage.nextTurnAndCloseDialogs();

        // Select tile with moved units and select first.
        gamePage.selectTile(NEAR_COLONY);

        // Press 'p' to plow field.
        gamePage.pressP();

        // Verify that selected unit have '0' action points.
        final RightPanelUnit rpu = gamePage.getRightPanelUnit(1);
        rpu.verifySelected(true);
        rpu.verifyFreeActionPoints(0);
    }

    @Test
    @Tag("local")
    void TC_06_second_pioneer_on_a_completed_field_is_not_charged()
            throws Exception {
        // Equip the colonists of a scoped copy and load it.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f05-competing.microcol", 100, null, NEAR_COLONY);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move two free colonists next to the colony.
        gamePage.getRightPanelUnit(1).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_COLONY);
        gamePage.pressTab();
        gamePage.getRightPanelUnit(1).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_COLONY);
        gamePage.nextTurnAndCloseDialogs();

        // Both start plowing the same square; neither has paid yet.
        gamePage.selectTile(NEAR_COLONY);
        gamePage.getRightPanelUnit(0).selectUnit();
        gamePage.pressP();
        gamePage.getRightPanelUnit(1).selectUnit();
        gamePage.pressP();

        // Both units now plow with spent movement and unspent tools.
        plow.verifyUnitWork(NEAR_COLONY, 0, UnitActionType.plowField, 0, 100);
        plow.verifyUnitWork(NEAR_COLONY, 1, UnitActionType.plowField, 0, 100);
        plow.verifyTotalToolsAt(NEAR_COLONY, 200);

        // Both complete at the same update: one field, one charge of 20 tools.
        for (int update = 0; update < 4; update++) {
            gamePage.nextTurnAndCloseDialogs();
        }
        plow.verifyFieldAt(NEAR_COLONY, true);
        plow.verifyTotalToolsAt(NEAR_COLONY, 180);
    }

}
