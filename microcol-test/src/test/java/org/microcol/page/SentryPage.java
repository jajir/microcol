package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/** Sentry controls and explicit observations of orders and passengers. */
public final class SentryPage extends AbstractScreen {
    public SentryPage(final TestContext context) {
        super(context);
    }

    public void openPierOrders(final int id) {
        getRobot().clickOn("#europe-pier-unit-" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void choosePierOrder(final int id) {
        getRobot().clickOn("#europe-sentry-" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelMenu() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void changeMapOrder() {
        clickOnButtonWithId("sentryUnit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void changeMapOrderWithKeyboard() {
        final Button button = getButtonById("sentryUnit");
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyMapAction(final String text) {
        final Button button = getButtonById("sentryUnit");
        assertEquals(text, button.getText());
        assertFalse(button.isDisabled());
        closeUnitActions();
    }

    public void verifyOrder(final int id, final boolean sentry,
            final int moves) {
        final Unit unit = getModel().getUnitById(id);
        assertEquals(sentry ? UnitActionType.sentry : UnitActionType.noAction,
                unit.getAction().getType());
        assertEquals(moves, unit.getActionPoints());
    }

    public void verifyWaitingOnPier(final int id, final boolean sentry) {
        assertTrue(getModel().getUnitById(id).isAtEuropePier());
        verifyOrder(id, sentry, 1);
        assertEquals(sentry, !getNodeFinder().lookup("#sentry-marker-" + id)
                .queryAll().isEmpty());
    }

    public void verifyMapPosition(final int id,
            final org.microcol.model.Location location) {
        assertTrue(getModel().getUnitById(id).isAtPlaceLocation());
        assertEquals(location, getModel().getUnitById(id).getLocation());
    }

    public void verifyPassenger(final int id, final int shipId, final int moves,
            final int tools) {
        final Unit unit = getModel().getUnitById(id);
        assertTrue(unit.getPlace() instanceof PlaceCargoSlot);
        assertTrue(
                ((org.microcol.model.unit.Ship) getModel().getUnitById(shipId))
                        .getCargo().getSlots().stream()
                        .anyMatch(slot -> slot.getUnit().orElse(null) == unit));
        verifyOrder(id, false, moves);
        assertEquals(tools, ((UnitFreeColonist) unit).getTools());
    }
}
