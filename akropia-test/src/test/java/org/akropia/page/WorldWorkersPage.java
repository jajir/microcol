package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import org.akropia.model.Unit;
import org.akropia.model.Location;
import org.akropia.model.GoodsType;
import org.testfx.util.WaitForAsyncUtils;

/** Map work actions and their observable, model-backed outcomes. */
public final class WorldWorkersPage extends AbstractScreen {
    public WorldWorkersPage(final TestContext context) {
        super(context);
    }

    public void choose(final String id) {
        getRobot().clickOn("#" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmMovement() {
        getRobot().push(javafx.scene.input.KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pressMovementShortcut() {
        getRobot().push(javafx.scene.input.KeyCode.M);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyMenuAction(final String id, final String label,
            final boolean disabled) {
        getRobot().interact(() -> {
            final javafx.scene.control.ContextMenu menu = (javafx.scene.control.ContextMenu) javafx.stage.Window
                    .getWindows().stream()
                    .filter(window -> window instanceof javafx.scene.control.ContextMenu
                            && window.isShowing())
                    .findFirst().orElseThrow();
            final java.util.Optional<javafx.scene.control.MenuItem> item = menu
                    .getItems().stream()
                    .filter(entry -> id.equals(entry.getId())).findFirst();
            if (item.isPresent()) {
                assertEquals(label, item.get().getText());
                assertEquals(disabled, item.get().isDisable());
            } else {
                final javafx.scene.control.Button button = getRobot()
                        .lookup("#" + id)
                        .queryAs(javafx.scene.control.Button.class);
                assertEquals(label, button.getText());
                assertEquals(disabled, button.isDisabled());
            }
        });
    }

    public void verifyMapSelection(final int id, final Location location,
            final String task, final Integer colonyId,
            final int movementThirds) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(id, getContext().getClassFromGuice(
                org.akropia.gui.screen.game.gamepanel.SelectedUnitManager.class)
                .getSelectedUnit().orElseThrow().getId());
        assertFalse(getContext().getClassFromGuice(
                org.akropia.gui.screen.game.gamepanel.ModeController.class)
                .isMoveMode());
        assertTrue(isCssIdVisible(
                org.akropia.gui.screen.game.components.ButtonsGamePanel.BUTTON_NEXT_TURN_ID));
        verifyWorker(id, location, task, colonyId, movementThirds);
    }

    public void verifyWorker(final int id, final Location location,
            final String task, final Integer colonyId,
            final int movementThirds) {
        final Unit worker = getModel().getUnitById(id);
        assertEquals(location, worker.getLocation());
        assertEquals(task, worker.getTaskSign());
        assertEquals(colonyId, worker.getWorkingColony()
                .map(colony -> colony.getId()).orElse(null));
        assertEquals(movementThirds, worker.getMovementThirds());
        assertEquals(1, getModel().getUnitsAt(location).stream()
                .filter(unit -> unit.getId() == id).count());
    }

    public void verifyFood(final int colonyId, final int expectedFood) {
        assertEquals(expectedFood, getModel().getColonyById(colonyId)
                .orElseThrow().getRequiredFoodPerTurn());
    }

    public void verifyStock(final int colonyId, final int grain) {
        assertEquals(grain, getModel().getColonyById(colonyId).orElseThrow()
                .getWarehouse().getGoods(GoodsType.GRAIN).getAmount());
    }

    public void verifyFishing(final int unitId, final int colonyId,
            final boolean working) {
        final Unit unit = getModel().getUnitById(unitId);
        assertTrue(unit.isFishing());
        assertEquals(colonyId, unit.getFishingColony().orElseThrow().getId());
        assertEquals(working ? "w" : "", unit.getTaskSign());
        assertFalse(unit.canAttack());
        assertTrue(unit.getMapJobChoices().isEmpty());
    }

    public void verifySelectedFieldWorker() {
        assertFalse(getRobot().lookup("#colony-field-worker")
                .queryAs(javafx.scene.control.Label.class).getText().isBlank());
    }

    public void verifyReturned(final int unitId, final Location home) {
        final Unit unit = getModel().getUnitById(unitId);
        assertFalse(unit.isFishing());
        assertTrue(unit.getWorkingColony().isEmpty());
        assertEquals(home, unit.getLocation());
        assertEquals(0, unit.getMovementThirds());
    }

    public void changeOrder(final String buttonId) {
        clickOnButtonWithId(buttonId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyTask(final int unitId, final String task) {
        assertEquals(task, getModel().getUnitById(unitId).getTaskSign());
    }
}
