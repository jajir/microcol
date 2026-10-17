package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.ClearForestPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that a pioneer clears a forest only after the player confirms the
 * expected 20 lumber, that declining, Escape and a stale confirmation change
 * nothing, and that completion spends 20 tools once, removes the forest for
 * good and delivers or reports the lumber.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_73_F65_clear_forest_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location FOREST_LOCATION = Location.of(23, 12);

    /** Delft: the nearest colony of the forest square, with ID 1. */
    private final static int DELFT_ID = 1;

    private final static String QUESTION = "Clear the forest here? Work takes 4 owner updates and spends 20 tools when it finishes. Expected result: 20 lumber, probably stored in colony Delft. The colony is chosen when the work finishes and a full warehouse loses the lumber it cannot hold. Spent movement is not restored if you cancel later.";

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    /** Equip a scoped copy, plant the forest and move a colonist next to it. */
    private GamePage prepareAndMove(final String name, final int tools,
            final String unitType, final int delftLumber) throws Exception {
        ClearForestFixture.prepare(getContext(), testFileName, name, tools,
                unitType, FOREST_LOCATION, "Delft", delftLumber);
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(FOREST_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(FOREST_LOCATION);
        return gamePage;
    }

    @Test
    @Tag("local")
    void TC_01_confirmed_clearing_completes_after_four_updates_and_survives_continue()
            throws Exception {
        // Load a prepared copy and bring a tooled colonist to the forest.
        GamePage gamePage = prepareAndMove("clear-f01-ordinary.microcol", 100,
                null, 0);
        final ClearForestPage clear = new ClearForestPage(getContext());
        clear.verifyControlVisible(true);

        // The control asks first and states the expected result.
        clear.useControl();
        clear.verifyConfirmation("Clear forest", QUESTION);
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                100);

        // Confirming spends the movement only; tools, forest and lumber stay.
        clear.confirm();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.clearForest, 0,
                100);
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 4);
        clear.verifyForestAt(FOREST_LOCATION, true);
        clear.verifyColonyLumber(DELFT_ID, 0);
        clear.verifyCancelControlVisible(true);

        // Two updates later nothing is charged; Continue keeps the countdown.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 2);
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 2);
        clear.verifyForestAt(FOREST_LOCATION, true);

        // The fourth update removes the forest, charges 20 tools and pays out.
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 1);
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyForestAt(FOREST_LOCATION, false);
        clear.verifyClearedAt(FOREST_LOCATION, true);
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 0,
                80);
        clear.verifyColonyLumber(DELFT_ID, 20);
        clear.verifyTerrainOutput(FOREST_LOCATION, 0, 0);

        // Later updates and Continue neither charge nor pay a second time.
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                80);
        clear.verifyColonyLumber(DELFT_ID, 20);
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        clear.verifyClearedAt(FOREST_LOCATION, true);
        clear.verifyTerrainOutput(FOREST_LOCATION, 0, 0);
        clear.verifyColonyLumber(DELFT_ID, 20);
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                80);
    }

    @Test
    @Tag("local")
    void TC_02_declining_and_escape_start_nothing() throws Exception {
        // Load a prepared copy and bring a tooled colonist to the forest.
        final GamePage gamePage = prepareAndMove("clear-f01-decline.microcol",
                100, null, 0);
        final ClearForestPage clear = new ClearForestPage(getContext());

        // Declining with the Cancel button leaves everything unchanged.
        clear.pressKey();
        clear.verifyConfirmation("Clear forest", QUESTION);
        clear.decline();
        clear.verifyDialogClosed();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                100);
        clear.verifyForestAt(FOREST_LOCATION, true);
        clear.verifyCancelControlVisible(false);

        // Escape closes the question the same way.
        clear.useControl();
        clear.verifyConfirmation("Clear forest", QUESTION);
        clear.escape();
        clear.verifyDialogClosed();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                100);
        clear.verifyForestAt(FOREST_LOCATION, true);
        clear.verifyClearedAt(FOREST_LOCATION, false);
        clear.verifyColonyLumber(DELFT_ID, 0);

        // Waiting changes nothing either: no order exists.
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                100);
        clear.verifyForestAt(FOREST_LOCATION, true);
    }

    @Test
    @Tag("local")
    void TC_03_stale_confirmation_changes_nothing() throws Exception {
        // Load a prepared copy and bring a tooled colonist to the forest.
        final GamePage gamePage = prepareAndMove("clear-f01-stale.microcol",
                100, null, 0);
        final ClearForestPage clear = new ClearForestPage(getContext());

        // The forest disappears while the question is open.
        clear.useControl();
        clear.verifyConfirmation("Clear forest", QUESTION);
        clear.removeForestBehindDialog(FOREST_LOCATION);

        // Confirming is rejected; nothing is spent or paid.
        clear.confirm();
        gamePage.verifyThatStatusBarContains(
                "The situation changed, so forest clearing was not started.");
        clear.verifyDialogClosed();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                100);
        clear.verifyColonyLumber(DELFT_ID, 0);
        clear.verifyCancelControlVisible(false);
    }

    @Test
    @Tag("local")
    void TC_04_hardy_pioneer_completes_after_two_updates() throws Exception {
        // Load a prepared copy with hardy pioneers and start the work.
        final GamePage gamePage = prepareAndMove("clear-f01-hardy.microcol",
                100, "HARDY_PIONEER", 0);
        final ClearForestPage clear = new ClearForestPage(getContext());
        clear.pressKey();
        clear.confirm();
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 2);

        // The first update leaves one; the second completes the clearing.
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 1);
        clear.verifyForestAt(FOREST_LOCATION, true);
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyForestAt(FOREST_LOCATION, false);
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 0,
                80);
        clear.verifyColonyLumber(DELFT_ID, 20);
    }

    @Test
    @Tag("local")
    void TC_05_cancel_discards_progress_without_refund() throws Exception {
        // Load a prepared copy and start the work.
        final GamePage gamePage = prepareAndMove("clear-f01-cancel.microcol",
                100, null, 0);
        final ClearForestPage clear = new ClearForestPage(getContext());
        clear.pressKey();
        clear.confirm();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(FOREST_LOCATION);
        clear.verifyRemainingUpdates(FOREST_LOCATION, 0, 3);

        // Cancel: no refund of movement or tools, forest and lumber unchanged.
        clear.cancelWork();
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 0,
                100);
        clear.verifyCancelControlVisible(false);
        clear.verifyForestAt(FOREST_LOCATION, true);

        // Waiting beyond the original completion changes nothing.
        for (int update = 0; update < 4; update++) {
            gamePage.nextTurnAndCloseDialogs();
        }
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                100);
        clear.verifyForestAt(FOREST_LOCATION, true);
        clear.verifyClearedAt(FOREST_LOCATION, false);
        clear.verifyColonyLumber(DELFT_ID, 0);
    }

    @Test
    @Tag("local")
    void TC_06_overflow_is_reported_next_turn_once_and_survives_continue()
            throws Exception {
        // The nearest warehouse holds 95 of 100 lumber, so only 5 fit.
        GamePage gamePage = prepareAndMove("clear-f01-overflow.microcol", 100,
                null, 95);
        final ClearForestPage clear = new ClearForestPage(getContext());
        clear.pressKey();
        clear.confirm();
        for (int update = 0; update < 3; update++) {
            gamePage.nextTurnAndCloseDialogs();
        }
        clear.verifyColonyLumber(DELFT_ID, 95);

        // Completion fills the warehouse and reports the 15 lost lumber.
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        clear.verifyTurnReportShown(report);
        report.get().verifyThatAtLeastOneEventMessageContains(
                "Forest cleared at (23, 12): lumber lost: 15. The warehouse of colony Delft is full.");
        report.get().close();
        clear.verifyColonyLumber(DELFT_ID, 100);
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 0,
                80);

        // Continue keeps the same single notice without disposing again.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        clear.verifyColonyLumber(DELFT_ID, 100);
        final ScreenTurnReport again = gamePage.openTurnReport();
        again.verifyEventMessageCount("lumber lost: 15", 1);
        again.close();

        // The notice belongs to this turn only; nothing repeats afterwards.
        final Optional<ScreenTurnReport> next = gamePage.nextTurn();
        clear.verifyNoLumberLossReported(next);
        clear.closeReport(next);
        clear.verifyColonyLumber(DELFT_ID, 100);
        clear.verifyUnitWork(FOREST_LOCATION, 0, UnitActionType.noAction, 1,
                80);
    }

}
