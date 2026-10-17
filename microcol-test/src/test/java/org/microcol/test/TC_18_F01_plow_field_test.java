package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.PlowPage;
import org.microcol.page.RightPanelUnit;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that pressing P starts plowing, consumes movement and hides the
 * action, and that plow work can be rejected, canceled and completed.
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
        // Equip the colonists of a scoped copy; the shared scenario is kept.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-ordinary.microcol", 100, null, NEAR_LOCATION);

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

    @Test
    @Tag("local")
    void TC_02_colonist_without_enough_tools_is_told_why_it_cannot_plow()
            throws Exception {
        // Load the shared scenario, whose colonists carry no tools.
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third colonist next to the colony and start a new turn.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();

        // Press 'p'; the status bar explains the missing tools.
        gamePage.pressP();
        gamePage.verifyThatStatusBarContains(
                "Plowing needs a pioneer with at least 20 tools.");

        // The colonist keeps its movement, has no order and no field appears.
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 0);
        plow.verifyFieldAt(NEAR_LOCATION, false);
        plow.verifyCancelControlVisible(false);
    }

    @Test
    @Tag("local")
    void TC_07_nineteen_tools_keep_the_control_and_explain_the_shortfall()
            throws Exception {
        // Equip the colonists with one tool fewer than required.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-nineteen.microcol", 19, null, NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third colonist next to the colony and start a new turn.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();

        // The partly equipped pioneer still shows the plow control.
        gamePage.selectTile(NEAR_LOCATION);
        gamePage.verifyThatPlowFieldButtonIsVisible();

        // Using the control explains the shortfall and changes nothing.
        plow.useControl();
        gamePage.verifyThatStatusBarContains(
                "Plowing needs a pioneer with at least 20 tools.");
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 19);
        plow.verifyFieldAt(NEAR_LOCATION, false);
    }

    @Test
    @Tag("local")
    void TC_03_cancel_discards_progress_without_refund() throws Exception {
        // Equip the colonists of a scoped copy and load it.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-cancel.microcol", 100, null, NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third colonist next to the colony and start a new turn.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();

        // Start plowing; the separate cancel control appears.
        gamePage.pressP();
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.plowField, 0, 100);
        plow.verifyCancelControlVisible(true);

        // One owner update passes and the work is still unfinished.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 3);

        // Cancel: the order is gone, no movement or tools are refunded.
        plow.cancelWork();
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 100);
        plow.verifyCancelControlVisible(false);
        plow.verifyFieldAt(NEAR_LOCATION, false);

        // Waiting beyond the original completion leaves terrain and tools.
        for (int update = 0; update < 4; update++) {
            gamePage.nextTurnAndCloseDialogs();
        }
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 100);
        plow.verifyFieldAt(NEAR_LOCATION, false);
    }

    @Test
    @Tag("local")
    void TC_04_ordinary_work_completes_after_four_updates_and_survives_continue()
            throws Exception {
        // Equip the colonists of a scoped copy and load it.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-complete.microcol", 100, null, NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third colonist next to the colony and start plowing.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.pressP();

        // Two updates later the countdown is halfway and nothing is charged.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 2);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.plowField, 0, 100);
        plow.verifyFieldAt(NEAR_LOCATION, false);

        // Continue keeps the countdown without advancing it.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 2);
        plow.verifyFieldAt(NEAR_LOCATION, false);

        // The next update leaves one, and the fourth completes exactly once.
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 1);
        plow.verifyFieldAt(NEAR_LOCATION, false);
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 80);

        // Later updates do not charge again, and Continue keeps the field.
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 80);
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 80);
    }

    @Test
    @Tag("local")
    void TC_05_hardy_pioneer_completes_after_two_updates() throws Exception {
        // Equip hardy pioneers in a scoped copy and load it.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-hardy.microcol", 100, "HARDY_PIONEER", NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third pioneer next to the colony and start plowing.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.pressP();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 2);

        // The first update leaves one; the second completes the field.
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 1);
        plow.verifyFieldAt(NEAR_LOCATION, false);
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 80);
    }

    @Test
    @Tag("local")
    void TC_06_exactly_twenty_tools_are_spent_and_zero_tools_cannot_plow_again()
            throws Exception {
        // Equip the colonists with exactly the required tools.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-exact.microcol", 20, null, NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third colonist next to the colony and plow to completion.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.pressP();
        for (int update = 0; update < 4; update++) {
            gamePage.nextTurnAndCloseDialogs();
        }

        // All tools are spent and the field exists.
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 0);

        // A later 'p' is rejected and explains the missing tools.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(NEAR_LOCATION);
        gamePage.pressP();
        gamePage.verifyThatStatusBarContains(
                "Plowing needs a pioneer with at least 20 tools.");
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 0);
    }

}
