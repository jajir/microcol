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
