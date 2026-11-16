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
        if (id.startsWith("mapJob-")) {
            final String[] parts = id.split("-");
            getRobot().moveTo("#workForColony-" + parts[1]);
            getRobot().sleep(400);
            moveToSubmenuRow(getNodeFinder()
                    .lookup("#mapJob-" + parts[1] + "-output-" + parts[2])
                    .query());
            getRobot().clickOn();
        } else {
            getRobot().clickOn("#" + id);
        }
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

    /**
     * The worker's receiving colony is described beside its image in the right
     * panel, wrapped inside the panel.
     */
    public void verifyRecipientInPanel(final int unitId,
            final String expectedText) {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(isCssIdShown("unitPanel-" + unitId));
        final javafx.scene.control.Label label = getNodeFinder()
                .lookup("#unitWorkingFor-" + unitId)
                .queryAs(javafx.scene.control.Label.class);
        assertEquals(expectedText, label.getText());
        assertTrue(label.isWrapText());
        final javafx.scene.Node panel = getNodeFinder().lookup("#rightPanel")
                .query();
        assertTrue(
                label.localToScene(label.getBoundsInLocal()).getMaxX() <= panel
                        .localToScene(panel.getBoundsInLocal()).getMaxX());
    }

    /**
     * No receiving colony is described for the unit; when listed, its panel is
     * the current one without the stale description.
     */
    public void verifyRecipientNotInPanel(final int unitId,
            final boolean listed) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(listed, isCssIdShown("unitPanel-" + unitId));
        assertTrue(getNodeFinder().lookup("#unitWorkingFor-" + unitId)
                .tryQuery().isEmpty());
    }

    public void captureMapAndRightPanel(final String fileName)
            throws java.io.IOException {
        captureDialog("rightPanel", fileName);
    }

    private java.util.Map<String, Boolean> rememberedCommands;

    private java.util.Map<String, Boolean> commandStates() {
        final java.util.Map<String, Boolean> states = new java.util.TreeMap<>();
        javafx.stage.Window.getWindows().stream().filter(
                window -> window instanceof javafx.scene.control.ContextMenu
                        && window.isShowing())
                .forEach(window -> window.getScene().getRoot()
                        .lookupAll(".button").stream()
                        .filter(node -> node instanceof javafx.scene.control.Button
                                && node.isVisible())
                        .forEach(node -> states.put(node.getId(),
                                node.isDisabled())));
        return states;
    }

    public void rememberCommands() {
        getRobot().interact(() -> rememberedCommands = commandStates());
    }

    public void verifySameCommands() {
        getRobot().interact(() -> {
            assertFalse(rememberedCommands.isEmpty());
            assertEquals(rememberedCommands, commandStates());
        });
    }

    public void closeMenu() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE)
                .push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyReceivingColonies(final Integer... expected) {
        getRobot().interact(() -> {
            final java.util.Set<Integer> actual = new java.util.TreeSet<>();
            javafx.stage.Window.getWindows().stream().filter(
                    window -> window instanceof javafx.scene.control.ContextMenu
                            && window.isShowing())
                    .map(window -> (javafx.scene.control.ContextMenu) window)
                    .forEach(menu -> menu.getItems().stream()
                            .filter(item -> item.getId() != null && item.getId()
                                    .startsWith("workForColony-"))
                            .forEach(item -> actual
                                    .add(Integer.valueOf(item.getId().substring(
                                            "workForColony-".length())))));
            assertEquals(new java.util.TreeSet<>(java.util.List.of(expected)),
                    actual);
        });
    }

}
