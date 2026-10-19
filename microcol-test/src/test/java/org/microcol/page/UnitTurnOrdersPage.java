package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Unit;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;

/** Player controls and observations for Wait and Skip turn. */
public final class UnitTurnOrdersPage extends AbstractScreen {
    public UnitTurnOrdersPage(final TestContext context) {
        super(context);
    }

    public void waitForAnotherUnit() {
        clickOnButtonWithId("waitUnit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void skipUnitTurn() {
        clickOnButtonWithId("skipUnitTurn");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifySelectedUnit(final Integer expectedId) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expectedId,
                getContext().getClassFromGuice(SelectedUnitManager.class)
                        .getSelectedUnit().map(Unit::getId).orElse(null));
    }

    public void verifyOrdersAvailable(final boolean wait, final boolean skip) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(wait, !getButtonById("waitUnit").isDisabled());
        assertEquals(skip, !getButtonById("skipUnitTurn").isDisabled());
    }

    public void verifyLabelsAndLayout() {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.Node controls = getNodeFinder()
                .lookup(".buttonPanel").query();
        final javafx.scene.canvas.Canvas canvas = getContext()
                .getClassFromGuice(
                        org.microcol.gui.screen.game.gamepanel.GamePanelComponent.class)
                .getCanvas();
        final javafx.geometry.Bounds controlsBounds = controls
                .localToScreen(controls.getBoundsInLocal());
        final javafx.geometry.Bounds mapBounds = canvas
                .localToScreen(canvas.getBoundsInLocal());
        assertTrue(mapBounds.getMinY() >= controlsBounds.getMaxY(),
                "All toolbar rows must be outside the playable map: controls="
                        + controlsBounds + "; map=" + mapBounds);
        final Button wait = getButtonById("waitUnit");
        final Button skip = getButtonById("skipUnitTurn");
        assertEquals("Wait\n(Tab)", wait.getText());
        assertEquals("Skip turn\n(Space)", skip.getText());
        assertEquals(
                "Select the next available unit without spending movement.",
                wait.getTooltip().getText());
        assertEquals(
                "Spend all remaining movement for this unit until its next turn.",
                skip.getTooltip().getText());
        for (final Button button : new Button[] { wait, skip,
                getButtonById("nextTurn"), getButtonById("exit") }) {
            assertTrue(button.isVisible());
            assertTrue(button.getWidth() >= button.prefWidth(-1),
                    "Label must fit: " + button.getId() + " width="
                            + button.getWidth() + " preferred="
                            + button.prefWidth(-1));
            final javafx.geometry.Bounds bounds = button
                    .localToScene(button.getBoundsInLocal());
            assertTrue(
                    bounds.getMinX() >= 0
                            && bounds.getMaxX() <= button.getScene().getWidth(),
                    "Control must fit the window");
        }
    }

    public void verifyRejectionMessage(final String expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, getNodeFinder().lookup(expected)
                .queryAs(javafx.scene.control.Label.class).getText());
    }

    public void verifyMovement(final int ship, final int firstColonist,
            final int secondColonist, final int round) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(ship, getModel().getUnitById(3).getActionPoints());
        assertEquals(firstColonist,
                getModel().getUnitById(24).getActionPoints());
        assertEquals(secondColonist,
                getModel().getUnitById(25).getActionPoints());
        assertEquals(round, getModel().getCalendar().getNumberOfPlayedTurns());
        for (final int id : new int[] { 3, 24, 25 }) {
            assertTrue(getModel().getUnitById(id).getAction().isNoAction());
            assertEquals(org.microcol.model.Location.of(22, 12),
                    getModel().getUnitById(id).getLocation());
        }
    }
}
