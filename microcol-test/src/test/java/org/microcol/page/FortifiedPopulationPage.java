package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.*;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import org.testfx.util.WaitForAsyncUtils;

/** Fortified colony departures and forced population losses. */
public final class FortifiedPopulationPage extends AbstractScreen {
    public FortifiedPopulationPage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void dragIndoorOutside(String buildingId, int index)
            throws Exception {
        Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        Canvas canvas = (Canvas) building.getChildrenUnmodifiable().stream()
                .filter(n -> n instanceof Canvas).findFirst().orElseThrow();
        // A native cross-colony drag needs both endpoints visible at once.
        new GameBarWidthPage(getContext()).resizeContent(
                Math.max(1200, canvas.getScene().getWidth()),
                Math.max(925, canvas.getScene().getHeight()));
        final javafx.scene.Node outside = getNodeFinder().lookup(".outside")
                .queryAs(javafx.scene.Node.class);
        revealGameContent(canvas, outside);
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(
                1);
        javafx.event.EventHandler<javafx.scene.input.DragEvent> completed = event -> done
                .countDown();
        getRobot().interact(() -> canvas.addEventHandler(
                javafx.scene.input.DragEvent.DRAG_DONE, completed));
        try {
            getRobot().drag(canvas.localToScreen(16 + 35 * index, 16),
                    MouseButton.PRIMARY).dropTo(outside);
            // Rejected native drags must finish before the next mouse gesture.
            if (!done.await(5, java.util.concurrent.TimeUnit.SECONDS))
                throw new java.util.concurrent.TimeoutException(
                        "Indoor drag did not finish");
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            getRobot().interact(() -> canvas.removeEventHandler(
                    javafx.scene.input.DragEvent.DRAG_DONE, completed));
        }
    }

    public void exhaustFood() {
        getRobot().interact(() -> colony().getWarehouse()
                .removeGoods(colony().getWarehouse().getGoods(GoodsType.CORN)));
    }

    public void verifyPopulation(int expected) {
        assertEquals(expected, colony().getUnitsInsideColony().size());
        assertTrue(colony().wasConstructionBuilded(ConstructionType.STOCKADE));
    }

    public void verifyWorker(int id, ConstructionType building,
            boolean mayLeave) {
        Unit unit = getModel().getUnitById(id);
        assertTrue(unit.isAtPlaceConstructionSlot());
        assertTrue(colony().getConstructionByType(building)
                .getConstructionSlots()
                .contains(unit.getPlaceConstruction().getConstructionSlot()));
        assertEquals(mayLeave, unit.canLeaveColony());
    }

    public void verifyOutside(int id) {
        assertTrue(getModel().getUnitById(id).isAtPlaceLocation());
        assertEquals(colony().getLocation(),
                getModel().getUnitById(id).getLocation());
    }

    public void verifyFortDoesNotOverlap(String... buildingIds) {
        javafx.scene.Node fort = getNodeFinder().lookup("#id_fort")
                .queryAs(javafx.scene.Node.class);
        assertTrue(fort.isVisible());
        javafx.geometry.Bounds bounds = fort
                .localToScene(fort.getBoundsInLocal());
        for (String id : buildingIds) {
            javafx.scene.Node other = getNodeFinder().lookup("#" + id)
                    .queryAs(javafx.scene.Node.class);
            assertFalse(bounds.intersects(
                    other.localToScene(other.getBoundsInLocal())), id);
        }
    }

    public void verifyBoarded(int id) {
        assertTrue(getModel().getUnitById(id).isAtCargoSlot());
    }
}
