package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitActionPlowField;
import org.akropia.model.unit.UnitActionType;
import org.akropia.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Label;

/** Real plow controls with explicit model and player-visible observations. */
public final class PlowPage extends AbstractScreen {

    private static final String CANCEL_CONTROL_ID = "cancelPlowField";

    private static final String PROGRESS_ID_PREFIX = "unitPlowWork-";

    private static final String COMPLETION_TEXT = "Work on the new field is complete.";

    private static final String CZECH_COMPLETION_TEXT = "Práce na novém poli jsou dokončeny.";

    public PlowPage(final TestContext context) {
        super(context);
    }

    /** Use the separate control that discards unfinished plow work. */
    public void cancelWork() {
        clickOnButtonWithId(CANCEL_CONTROL_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Use the visible plow control instead of the keyboard shortcut. */
    public void useControl() {
        clickOnButtonWithId("plowField");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Unit unitAt(final Location location, final int index) {
        return getModel().getUnitsAt(location).stream()
                .filter(unit -> unit.getWorkingColony().isEmpty())
                .collect(java.util.stream.Collectors.toList()).get(index);
    }

    public void verifyUnitWork(final Location location, final int index,
            final UnitActionType expectedAction, final int expectedMoves,
            final int expectedTools) {
        final Unit unit = unitAt(location, index);
        assertEquals(expectedAction, unit.getAction().getType(),
                "Unexpected unit action");
        assertEquals(expectedMoves, unit.getActionPoints(),
                "Unexpected unit movement");
        assertEquals(expectedTools, ((UnitFreeColonist) unit).getTools(),
                "Unexpected unit tools");
    }

    public void verifyRemainingUpdates(final Location location, final int index,
            final int expectedUpdates) {
        assertEquals(expectedUpdates,
                ((UnitActionPlowField) unitAt(location, index).getAction())
                        .getRemainingTurns(),
                "Unexpected plow countdown");
    }

    /**
     * The unit belongs to another nation and has an active plowing order.
     *
     * @param location        square of the unit
     * @param index           index of the unit on the square
     * @param expectedUpdates remaining owner updates of the order
     */
    public void verifyForeignPlowWork(final Location location, final int index,
            final int expectedUpdates) {
        assertFalse(unitAt(location, index).getOwner().isHuman(),
                "The plowing unit must belong to another nation");
        verifyRemainingUpdates(location, index, expectedUpdates);
    }

    public void verifyFieldAt(final Location location,
            final boolean expectedField) {
        assertEquals(expectedField,
                getModel().getMap().getTerrainAt(location).isHasField(),
                "Unexpected field at " + location);
    }

    public void verifyTotalToolsAt(final Location location,
            final int expectedTools) {
        assertEquals(expectedTools, getModel().getUnitsAt(location).stream()
                .mapToInt(unit -> ((UnitFreeColonist) unit).getTools()).sum(),
                "Unexpected tools carried at " + location);
    }

    public void verifyCancelControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isCssIdVisible(CANCEL_CONTROL_ID),
                "Unexpected cancel plow control visibility");
        closeUnitActions();
    }

    /**
     * Every plowing unit on the selected square shows the expected countdown
     * line and every other unit there shows none.
     *
     * @param location     selected square whose units are listed
     * @param expectedText countdown line of the plowing unit
     */
    public void verifyPlowProgressShown(final Location location,
            final String expectedText) {
        for (final Unit unit : getModel().getUnitsAt(location)) {
            final Optional<Label> label = progressLabel(unit);
            if (unit.getAction() instanceof UnitActionPlowField) {
                assertTrue(label.isPresent(),
                        "Plow progress must be shown for unit " + unit.getId());
                assertEquals(expectedText, label.get().getText(),
                        "Unexpected plow progress text");
            } else {
                assertTrue(label.isEmpty(),
                        "Only a plowing unit shows plow progress");
            }
        }
    }

    /** No unit listed in the right panel shows a plowing countdown. */
    public void verifyPlowProgressHidden() {
        assertTrue(
                getNodeFinder()
                        .lookup(node -> node.getId() != null
                                && node.getId().startsWith(PROGRESS_ID_PREFIX))
                        .tryQuery().isEmpty(),
                "Plow progress must not be shown");
    }

    /**
     * The report lists the completion of the given unit the expected number of
     * times.
     *
     * @param report        report that opened after the turn, if any
     * @param location      square of the completing unit and the new field
     * @param index         index of that unit on the square
     * @param expectedText  localized text following the unit number
     * @param expectedCount how many such notices are listed
     */
    public void verifyCompletionReported(
            final Optional<ScreenTurnReport> report, final Location location,
            final int index, final String expectedText,
            final int expectedCount) {
        assertTrue(report.isPresent(), "The completion must be reported");
        report.get().verifyEventMessageCount(
                "#" + unitAt(location, index).getId() + " " + expectedText,
                expectedCount);
    }

    /** Other events may be reported; no plowing completion is among them. */
    public void verifyNoCompletionReported(
            final Optional<ScreenTurnReport> report) {
        if (report.isPresent()) {
            report.get().verifyEventMessageCount(COMPLETION_TEXT, 0);
            report.get().verifyEventMessageCount(CZECH_COMPLETION_TEXT, 0);
        }
    }

    public void closeReport(final Optional<ScreenTurnReport> report) {
        if (report.isPresent()) {
            report.get().close();
        }
    }

    /** Save the whole game window with the right panel for text-fit review. */
    public void captureRightPanel(final String fileName) throws IOException {
        captureDialog("rightPanel", fileName);
    }

    private Optional<Label> progressLabel(final Unit unit) {
        return getNodeFinder().lookup("#" + PROGRESS_ID_PREFIX + unit.getId())
                .tryQuery().map(Label.class::cast);
    }
}
