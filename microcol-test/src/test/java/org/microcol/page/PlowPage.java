package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

/** Real plow controls with explicit model and player-visible observations. */
public final class PlowPage extends AbstractScreen {

    private static final String CANCEL_CONTROL_ID = "cancelPlowField";

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
        return getModel().getUnitsAt(location).get(index);
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
}
