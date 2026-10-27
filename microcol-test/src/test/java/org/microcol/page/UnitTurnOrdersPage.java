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
        openUnitActions();
        final boolean hasSelection = isCssIdShown("waitUnit");
        assertEquals(wait,
                hasSelection && !getButtonById("waitUnit").isDisabled());
        assertEquals(skip,
                hasSelection && !getButtonById("skipUnitTurn").isDisabled());
        closeUnitActions();
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
                .localToScreen(controls.getLayoutBounds());
        final javafx.geometry.Bounds mapBounds = canvas
                .localToScreen(canvas.getBoundsInLocal());
        assertTrue(mapBounds.getMinY() >= controlsBounds.getMaxY(),
                "Toolbar layout must be outside the playable map: controls="
                        + controlsBounds + "; map=" + mapBounds);
        final Button wait = getButtonById("waitUnit");
        final Button skip = getButtonById("skipUnitTurn");
        assertEquals("Wait (Tab)", wait.getText());
        assertEquals("Skip unit turn (Space)", skip.getText());
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
        closeUnitActions();
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

    public void openActionsWithKeyboard() {
        openUnitActions();
    }

    public void openActionsWithRightClick() {
        final Unit unit = getContext()
                .getClassFromGuice(SelectedUnitManager.class).getSelectedUnit()
                .orElseThrow();
        GamePage.of(getContext()).openUnitContextAt(unit.getLocation());
    }

    public void dismissActions() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveWithMenuKeyboard() {
        final Button move = getButtonById("move");
        for (int step = 0; step < 24; step++) {
            getRobot().push(javafx.scene.input.KeyCode.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
            final javafx.scene.Node focused = move.getScene().getFocusOwner();
            for (javafx.scene.Node node = move; node != null; node = node
                    .getParent()) {
                if (node == focused) {
                    getRobot().push(javafx.scene.input.KeyCode.ENTER);
                    WaitForAsyncUtils.waitForFxEvents();
                    return;
                }
            }
        }
        throw new IllegalStateException(
                "Keyboard navigation did not reach Move");
    }

    public void waitWithMenuKeyboard() {
        final Button wait = getButtonById("waitUnit");
        getRobot().interact(wait::requestFocus);
        getRobot().push(javafx.scene.input.KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyMenuVisible(final boolean expected) {
        assertEquals(expected,
                javafx.stage.Window.getWindows().stream().anyMatch(
                        window -> window instanceof javafx.scene.control.ContextMenu
                                && "unitActionsMenu".equals(
                                        ((javafx.scene.control.ContextMenu) window)
                                                .getId())
                                && window.isShowing()));
    }

    public void verifyGlobalToolbar() {
        final javafx.scene.Parent bar = getNodeFinder().lookup(".buttonPanel")
                .queryAs(javafx.scene.Parent.class);
        for (final String id : java.util.List.of("move", "buildColony",
                "waitUnit", "skipUnitTurn", "sentryUnit", "fortifyUnit",
                "goToUnit", "goToColonyUnit", "disbandUnit", "plowField",
                "clearForest", "buildRoad", "jettisonUnit", "goToStatus")) {
            assertNull(bar.lookup("#" + id),
                    "Unit command must leave toolbar: " + id);
        }
        for (final String id : java.util.List.of("coloniesMenu", "congress",
                "retire", "center", "statistics", "turnReport", "goals", "help",
                "europe", "nextTurn", "exit")) {
            assertNotNull(bar.lookup("#" + id),
                    "Global control must remain: " + id);
        }
        assertFalse(isCssIdShown("unitActions"));
    }

    public void verifyContextCommands(final boolean canFoundColony) {
        assertTrue(isCssIdShown("waitUnit"));
        assertTrue(isCssIdShown("move"));
        assertEquals(canFoundColony, isCssIdShown("buildColony"));
        assertTrue(isCssIdShown("disbandUnit"));
        assertTrue(
                getNodeFinder().lookup(".unit-action-explanation")
                        .queryAllAs(javafx.scene.control.Label.class).stream()
                        .anyMatch(label -> label.isVisible()
                                && !label.getText().isEmpty()),
                "Unavailable commands have a visible explanation");
    }

    public void resizeWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyLocalizedMenu(final String unitName, final String wait,
            final String skip) {
        assertEquals(unitName, getNodeFinder()
                .lookup("#unitContextTitle .label").queryLabeled().getText());
        assertEquals(wait, getButtonById("waitUnit").getText());
        assertEquals(skip, getButtonById("skipUnitTurn").getText());
        for (final String id : java.util.List.of("waitUnit", "skipUnitTurn",
                "disbandUnit")) {
            final Button button = getButtonById(id);
            assertTrue(button.getWidth() + 0.5 >= button.prefWidth(-1),
                    "Label fits: " + id);
            final javafx.geometry.Bounds bounds = button
                    .localToScreen(button.getBoundsInLocal());
            final javafx.geometry.Rectangle2D screen = javafx.stage.Screen
                    .getScreensForRectangle(bounds.getMinX(), bounds.getMinY(),
                            bounds.getWidth(), bounds.getHeight())
                    .get(0).getVisualBounds();
            assertTrue(
                    screen.contains(bounds.getMinX(), bounds.getMinY(),
                            bounds.getWidth(), bounds.getHeight()),
                    "Menu command stays on screen: " + id);
        }
    }

    public void verifyNoUnitActions() {
        assertFalse(isCssIdShown("unitActions"));
        assertFalse(isCssIdShown("waitUnit"));
    }

    public void clickInformationCard(final int unitId,
            final javafx.scene.input.MouseButton button) {
        closeUnitActions();
        getRobot().clickOn("#unitPanel-" + unitId, button);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyInformationOnly() {
        assertFalse(isCssIdShown("unitActions"));
        assertTrue(getNodeFinder().lookup(".unitPanel .button").queryAll()
                .isEmpty());
        assertNotNull(getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.GamePanelComponent.class)
                .getCanvas().getScene(),
                "The map remains open, including for units in a colony");
    }

    public org.microcol.gui.Point readTilePosition(
            final org.microcol.model.Location location) {
        return getContext().getArea().convertToCanvasPoint(location);
    }

    public void verifyMapPanned(final org.microcol.model.Location location,
            final org.microcol.gui.Point before) {
        assertNotEquals(before, readTilePosition(location));
        verifyInformationOnly();
        verifyMenuVisible(false);
    }

    public void verifySingleUnitContext(final String unitName) {
        assertEquals(unitName, getNodeFinder()
                .lookup("#unitContextTitle .label").queryLabeled().getText());
        assertFalse(isCssIdShown("unitChoices"));
        verifyMenuVisible(true);
    }

}
