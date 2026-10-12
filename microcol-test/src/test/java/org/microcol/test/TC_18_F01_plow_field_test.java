package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.GamePage;
import org.microcol.page.RightPanelUnit;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that pressing P starts plowing, consumes movement and hides the
 * action.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_18_F01_plow_field_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location NEAR_LOCATION = Location.of(23, 12);

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_plowing_starts_and_hides_action() throws Exception {
        // Open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Verify that tile with colony is selected.
        gamePage.verifyThatSelectedTileIs(COLONY_LOCATION);

        // Select the third unit in the right panel. It is a free colonist.
        final RightPanelUnit rpu = gamePage.getRightPanelUnit(2);
        rpu.selectUnit();
        rpu.verifyFreeActionPoints(1);

        // Verify there is no unit at location near colony.
        gamePage.verifyNumberOfUnitsAt(NEAR_LOCATION, 0);

        // Move the colonist to the tile next to the colony.
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Verify that unit moved to new place.
        gamePage.verifyNumberOfUnitsAt(NEAR_LOCATION, 1);

        // Verify the moved colonist is ready for a new order.
        final Unit capturedUnit = getModel().getUnitsAt(NEAR_LOCATION).get(0);
        gamePage.verifyUnitAction(capturedUnit, UnitActionType.noAction);
        gamePage.verifyUnitActionPoints(capturedUnit, 1);

        // Verify that icon for plow field is visible.
        gamePage.verifyThatPlowFieldButtonIsVisible();

        // Press 'p' to plow field.
        gamePage.pressP();

        // Verify that icon for plow field is invisible.
        gamePage.verifyThatPlowFieldButtonIsHidden();

        // Assert the captured unit's action type changed to plowField and AP
        // consumed
        gamePage.verifyUnitAction(capturedUnit, UnitActionType.plowField);
        gamePage.verifyUnitActionPoints(capturedUnit, 0);
        gamePage.verifyUnitLocation(capturedUnit, NEAR_LOCATION);
    }

}
