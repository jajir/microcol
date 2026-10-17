package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionClearForest;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

/** Real forest-clearing controls with explicit model and visible results. */
public final class ClearForestPage extends AbstractScreen {

    private static final String CANCEL_CONTROL_ID = "cancelClearForest";

    public ClearForestPage(final TestContext context) {
        super(context);
    }

    /** Use the visible control instead of the keyboard shortcut. */
    public void useControl() {
        clickOnButtonWithId("clearForest");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Use the keyboard shortcut of the selected pioneer. */
    public void pressKey() {
        getRobot().push(KeyCode.F);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("clearForestConfirm");
    }

    public void decline() {
        clickOnButtonWithId("clearForestCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Use the separate control that discards unfinished clearing work. */
    public void cancelWork() {
        clickOnButtonWithId(CANCEL_CONTROL_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Let another pioneer finish the forest while the dialog is open. */
    public void removeForestBehindDialog(final Location location) {
        getRobot().interact(() -> getModel().getMap().clearForest(location));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Unit unitAt(final Location location, final int index) {
        return getModel().getUnitsAt(location).get(index);
    }

    public void verifyConfirmation(final String expectedTitle,
            final String expectedQuestion) {
        assertEquals(expectedTitle, getNodeFinder().lookup("#clearForestTitle")
                .queryAs(Label.class).getText());
        assertEquals(expectedQuestion, getNodeFinder()
                .lookup("#clearForestQuestion").queryAs(Label.class).getText());
        assertTrue(getNodeFinder().lookup("#clearForestConfirm")
                .queryAs(Button.class).isVisible());
    }

    public void verifyDialogClosed() {
        assertTrue(getNodeFinder().lookup("#clearForestQuestion").tryQuery()
                .isEmpty(), "The confirmation must be closed");
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
                ((UnitActionClearForest) unitAt(location, index).getAction())
                        .getRemainingTurns(),
                "Unexpected clearing countdown");
    }

    public void verifyForestAt(final Location location,
            final boolean expectedForest) {
        assertEquals(expectedForest,
                getModel().getMap().getTerrainAt(location).isHasTrees(),
                "Unexpected forest at " + location);
    }

    public void verifyClearedAt(final Location location,
            final boolean expectedCleared) {
        assertEquals(expectedCleared,
                getModel().getMap().getTerrainAt(location).isCleared(),
                "Unexpected cleared state at " + location);
    }

    public void verifyTerrainOutput(final Location location,
            final int expectedLumber, final int expectedFur) {
        final Terrain terrain = getModel().getMap().getTerrainAt(location);
        assertEquals(expectedLumber,
                terrain.canProduceAmmount(GoodsType.LUMBER),
                "Unexpected lumber output at " + location);
        assertEquals(expectedFur, terrain.canProduceAmmount(GoodsType.FUR),
                "Unexpected fur output at " + location);
    }

    public void verifyColonyLumber(final int colonyId,
            final int expectedLumber) {
        assertEquals(expectedLumber,
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(GoodsType.LUMBER).getAmount(),
                "Unexpected lumber in colony " + colonyId);
    }

    public void verifyCancelControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible,
                getNodeFinder().lookup("#" + CANCEL_CONTROL_ID).tryQuery()
                        .isPresent(),
                "Unexpected cancel clearing control visibility");
    }

    public void verifyControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible,
                getNodeFinder().lookup("#clearForest").tryQuery().isPresent(),
                "Unexpected clear forest control visibility");
    }

    public void verifyTurnReportShown(final Optional<ScreenTurnReport> report) {
        assertTrue(report.isPresent(), "The loss notice must be reported");
    }

    /** Other events may be reported; the lumber loss must not be repeated. */
    public void verifyNoLumberLossReported(
            final Optional<ScreenTurnReport> report) {
        if (report.isPresent()) {
            report.get().verifyEventMessageCount("lumber lost:", 0);
        }
    }

    public void closeReport(final Optional<ScreenTurnReport> report) {
        if (report.isPresent()) {
            report.get().close();
        }
    }
}
