package org.akropia.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitActionType;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.PlowPage;
import org.akropia.page.RightPanelUnit;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that pressing P starts plowing, consumes movement and hides the
 * action, that plow work can be rejected, canceled and completed, that the unit
 * panel shows its countdown only for own plowing units (R-0391) and that the
 * turn report announces the completed field once (R-0393).
 */
@ExtendWith(ApplicationExtension.class)
public class TC_18_F01_plow_field_test extends AbstractAkropiaTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location NEAR_LOCATION = Location.of(23, 12);

    private final static String COMPLETION_NOTICE = "at (23, 12): "
            + "Work on the new field is complete. "
            + "The unit can receive new orders.";

    private final static String CZECH_COMPLETION_NOTICE = "na (23, 12): "
            + "Práce na novém poli jsou dokončeny. "
            + "Jednotka může dostat další příkazy.";

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.akropia");

    /** Scenario with a visible colonist of another nation. */
    private final static File FOREIGN_FILE = new File(
            "src/test/scenarios/F27-attack-eligibility.akropia");

    /** Square of the French colonist in F27. */
    private final static Location FOREIGN_LOCATION = Location.of(17, 11);

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_plowing_starts_and_hides_action() throws Exception {
        // Equip the colonists of a scoped copy; the shared scenario is kept.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-ordinary.akropia", 100, null, NEAR_LOCATION);

        // Open Akropia and load defined game
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

        // The unit panel shows the whole countdown of the new work.
        gamePage.selectTile(NEAR_LOCATION);
        new PlowPage(getContext()).verifyPlowProgressShown(NEAR_LOCATION,
                "Plowing — 4 turns left");
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
                "plow-f01-nineteen.akropia", 19, null, NEAR_LOCATION);
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
                "plow-f01-cancel.akropia", 100, null, NEAR_LOCATION);
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
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 3 turns left");

        // Cancel: the order is gone, no movement or tools are refunded.
        plow.cancelWork();
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 100);
        plow.verifyCancelControlVisible(false);
        plow.verifyFieldAt(NEAR_LOCATION, false);

        // The countdown line disappears with the canceled work.
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressHidden();

        // Waiting beyond the original completion leaves terrain and tools, and
        // no report announces a completed field.
        for (int update = 0; update < 4; update++) {
            final Optional<ScreenTurnReport> report = gamePage.nextTurn();
            plow.verifyNoCompletionReported(report);
            plow.closeReport(report);
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
                "plow-f01-complete.akropia", 100, null, NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third colonist next to the colony and start plowing.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.pressP();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 4 turns left");

        // One update leaves three, and Continue shows the same three.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 3 turns left");
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 3);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 3 turns left");

        // The second update makes the countdown halfway; nothing is charged.
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 2);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.plowField, 0, 100);
        plow.verifyFieldAt(NEAR_LOCATION, false);
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 2 turns left");

        // Continue keeps the countdown without advancing it.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 2);
        plow.verifyFieldAt(NEAR_LOCATION, false);

        // The next update leaves the last one; no completion is reported yet.
        Optional<ScreenTurnReport> report = gamePage.nextTurn();
        plow.verifyNoCompletionReported(report);
        plow.closeReport(report);
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 1);
        plow.verifyFieldAt(NEAR_LOCATION, false);
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION,
                "Plowing — completes at the start of next turn");

        // The fourth update completes exactly once and reports it once.
        report = gamePage.nextTurn();
        plow.verifyCompletionReported(report, NEAR_LOCATION, 0,
                COMPLETION_NOTICE, 1);
        plow.closeReport(report);
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 80);
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressHidden();

        // Later updates do not charge or report again, and Continue keeps the
        // field.
        report = gamePage.nextTurn();
        plow.verifyNoCompletionReported(report);
        plow.closeReport(report);
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
                "plow-f01-hardy.akropia", 100, "HARDY_PIONEER", NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third pioneer next to the colony and start plowing.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.pressP();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 2);
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 2 turns left");

        // The first update leaves one; the second completes the field.
        gamePage.nextTurnAndCloseDialogs();
        plow.verifyRemainingUpdates(NEAR_LOCATION, 0, 1);
        plow.verifyFieldAt(NEAR_LOCATION, false);
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION,
                "Plowing — completes at the start of next turn");
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        plow.verifyCompletionReported(report, NEAR_LOCATION, 0,
                COMPLETION_NOTICE, 1);
        plow.closeReport(report);
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 80);

        // The countdown line is gone once the field exists.
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressHidden();
    }

    @Test
    @Tag("local")
    void TC_08_plowing_countdown_follows_selected_unit() throws Exception {
        // Equip the colonists of a scoped copy and load it.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-selection.akropia", 100, null, NEAR_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // The colony square is selected; its own idle units show no countdown.
        gamePage.verifyThatSelectedTileIs(COLONY_LOCATION);
        plow.verifyPlowProgressHidden();

        // Move the third colonist next to the colony and start a new turn.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();

        // The colonist without a plowing order shows no countdown.
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressHidden();

        // Start plowing; the same colonist now shows the countdown.
        gamePage.pressP();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 4 turns left");

        // Switch to the colony square: its idle units show no countdown.
        gamePage = gamePage.openColonyAt(COLONY_LOCATION, "Delft").close();
        gamePage.verifyThatSelectedTileIs(COLONY_LOCATION);
        plow.verifyPlowProgressHidden();

        // Back on the plowing square the current countdown is shown again.
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 4 turns left");
        plow.captureRightPanel("plow-countdown-selection.png");

        // After an owner update the reselected square shows the new countdown.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Plowing — 3 turns left");
    }

    @Test
    @Tag("local")
    void TC_09_plowing_completion_report_survives_continue() throws Exception {
        // Switch to Czech and load hardy pioneers in a scoped copy.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-report.akropia", 100, "HARDY_PIONEER", NEAR_LOCATION);
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        GamePage gamePage = settings.goBack().loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // Move the third pioneer next to the colony and start plowing.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(NEAR_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.pressP();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION, "Orání — zbývají 2 tahy");

        // The last remaining update is announced in Czech in the panel.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(NEAR_LOCATION);
        plow.verifyPlowProgressShown(NEAR_LOCATION,
                "Orání — dokončení na začátku příštího tahu");
        plow.captureRightPanel("plow-countdown-last-czech.png");

        // The completing update reports the new field once, in Czech.
        Optional<ScreenTurnReport> report = gamePage.nextTurn();
        plow.verifyCompletionReported(report, NEAR_LOCATION, 0,
                CZECH_COMPLETION_NOTICE, 1);
        report.get().captureDialog("closeButtonId",
                "plow-completion-report-czech.png");
        plow.closeReport(report);
        plow.verifyFieldAt(NEAR_LOCATION, true);

        // Continue restores the same single notice without repeating the work.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        plow.verifyFieldAt(NEAR_LOCATION, true);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 0, 80);
        report = Optional.of(gamePage.openTurnReport());
        plow.verifyCompletionReported(report, NEAR_LOCATION, 0,
                CZECH_COMPLETION_NOTICE, 1);
        plow.closeReport(report);

        // Reopening the report still lists it once.
        report = Optional.of(gamePage.openTurnReport());
        plow.verifyCompletionReported(report, NEAR_LOCATION, 0,
                CZECH_COMPLETION_NOTICE, 1);
        plow.closeReport(report);

        // The next update brings no new completion and charges nothing more.
        report = gamePage.nextTurn();
        plow.verifyNoCompletionReported(report);
        plow.closeReport(report);
        plow.verifyUnitWork(NEAR_LOCATION, 0, UnitActionType.noAction, 1, 80);
    }

    @Test
    @Tag("local")
    void TC_10_foreign_plowing_unit_shows_no_countdown() throws Exception {
        // F01 has no other nation; a scoped F27 copy turns the visible French
        // colonist into a pioneer that is already plowing its square.
        PlowFixture.prepareForeignPlowing(getContext(), FOREIGN_FILE,
                "plow-f27-foreign.akropia", 6, 3);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final PlowPage plow = new PlowPage(getContext());

        // The French unit really has an active plowing order.
        plow.verifyForeignPlowWork(FOREIGN_LOCATION, 0, 3);

        // Select its square: the unit is listed, but without a countdown.
        gamePage.selectTile(FOREIGN_LOCATION);
        gamePage.verifyNumberOfUnitInRightPanel(1);
        plow.verifyPlowProgressHidden();
    }

    @Test
    @Tag("local")
    void TC_06_exactly_twenty_tools_are_spent_and_zero_tools_cannot_plow_again()
            throws Exception {
        // Equip the colonists with exactly the required tools.
        PlowFixture.prepare(getContext(), testFileName,
                "plow-f01-exact.akropia", 20, null, NEAR_LOCATION);
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
