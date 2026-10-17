package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.ConstructionType;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public final class ColonyJobPage extends AbstractScreen {
    private com.google.gson.JsonElement remembered;

    public ColonyJobPage(final TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void openFromField(final int unitId) {
        ColonyScreen.of(getContext(), "New Amsterdam").openCropMenu(unitId);
        getRobot().clickOn("#chooseWork");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openFromCenter(final int unitId) {
        getRobot().clickOn("#colonyUnit-" + unitId, MouseButton.SECONDARY)
                .clickOn("#chooseWork");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openFromCenterWithKeyboard(final int unitId) {
        getRobot().clickOn("#colonyUnit-" + unitId).push(KeyCode.SHIFT,
                KeyCode.F10);
        getRobot().push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN)
                .push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Point2D slotPoint(final String buildingId, final int index) {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                .stream().filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        return canvas.localToScreen(10 + 31 * index, 82);
    }

    public void openFromIndoor(final String buildingId, final int index)
            throws Exception {
        final Point2D target = slotPoint(buildingId, index);
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        final javafx.scene.Scene scene = building.getScene();
        final java.util.concurrent.atomic.AtomicReference<String> press = new java.util.concurrent.atomic.AtomicReference<>(
                "absent");
        final java.util.concurrent.atomic.AtomicReference<String> menu = new java.util.concurrent.atomic.AtomicReference<>(
                "absent");
        final java.util.concurrent.atomic.AtomicBoolean reachedBuilding = new java.util.concurrent.atomic.AtomicBoolean();
        final javafx.event.EventHandler<javafx.scene.input.MouseEvent> pressed = event -> press
                .set("target=" + event.getTarget() + "; picked="
                        + (event.getPickResult() == null ? "none"
                                : event.getPickResult().getIntersectedNode())
                        + "; scene=" + event.getSceneX() + ","
                        + event.getSceneY());
        final javafx.event.EventHandler<javafx.scene.input.ContextMenuEvent> requested = event -> menu
                .set("target=" + event.getTarget() + "; picked="
                        + (event.getPickResult() == null ? "none"
                                : event.getPickResult().getIntersectedNode())
                        + "; scene=" + event.getSceneX() + ","
                        + event.getSceneY());
        final javafx.event.EventHandler<javafx.scene.input.ContextMenuEvent> received = event -> reachedBuilding
                .set(true);
        getRobot().interact(() -> {
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                    pressed);
            scene.addEventFilter(
                    javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                    requested);
            building.addEventFilter(
                    javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                    received);
        });
        try {
            getRobot().clickOn(target, MouseButton.SECONDARY);
            // Native drag cleanup and context-menu creation need separate
            // event cycles.
            WaitForAsyncUtils.waitForFxEvents();
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getNodeFinder()
                    .lookup("#chooseWork").tryQuery().isPresent());
        } catch (java.util.concurrent.TimeoutException timeout) {
            final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                    .stream().filter(node -> node instanceof Canvas).findFirst()
                    .orElseThrow();
            final String details = "Indoor context menu absent at " + target
                    + "; sceneTarget="
                    + building.localToScene(building.screenToLocal(target))
                    + "; canvas="
                    + canvas.localToScreen(canvas.getBoundsInLocal())
                    + "; building="
                    + building.localToScreen(building.getBoundsInLocal())
                    + "; scene=" + scene.getWidth() + "x" + scene.getHeight()
                    + "; press=" + press.get() + "; contextRequested="
                    + menu.get() + "; reachedBuilding=" + reachedBuilding.get()
                    + "; menuPresent=" + getNodeFinder().lookup("#chooseWork")
                            .tryQuery().isPresent();
            try {
                captureDialog(buildingId, "indoor-context-menu-timeout.png");
            } catch (Exception captureError) {
                timeout.addSuppressed(captureError);
            }
            final java.util.concurrent.TimeoutException diagnosed = new java.util.concurrent.TimeoutException(
                    details);
            diagnosed.initCause(timeout);
            throw diagnosed;
        } finally {
            getRobot().interact(() -> {
                scene.removeEventFilter(
                        javafx.scene.input.MouseEvent.MOUSE_PRESSED, pressed);
                scene.removeEventFilter(
                        javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                        requested);
                building.removeEventFilter(
                        javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                        received);
            });
        }
        getRobot().clickOn("#chooseWork");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private ListView<ColonyJobChoice> choices() {
        return (ListView<ColonyJobChoice>) getNodeFinder().lookup("#jobChoices")
                .queryAs(ListView.class);
    }

    public void select(final ColonyJobChoice.Kind kind, final GoodsType goods) {
        final ListView<ColonyJobChoice> list = choices();
        final ColonyJobChoice target = list.getItems().stream()
                .filter(choice -> choice.getKind() == kind
                        && choice.getGoodsType() == goods)
                .findFirst().orElseThrow();
        final int index = list.getItems().indexOf(target);
        getRobot().clickOn(list).push(KeyCode.HOME);
        for (int i = 0; i < index; i++)
            getRobot().push(KeyCode.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("jobConfirm");
    }

    public void confirmWithEnter() {
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("jobCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void rememberWorld() {
        remembered = new com.google.gson.Gson().toJsonTree(getModel().save());
    }

    public void verifyWorldUnchanged() {
        assertEquals(remembered,
                new com.google.gson.Gson().toJsonTree(getModel().save()));
    }

    public void verifyDetails(final String expectedPart,
            final boolean canConfirm) {
        assertTrue(isCssIdVisible("jobDialog"));
        assertTrue(
                getLabeledById("jobDetails").getText().contains(expectedPart),
                () -> getLabeledById("jobDetails").getText());
        assertEquals(!canConfirm, getButtonById("jobConfirm").isDisabled());
    }

    public void verifyVisibleChoice(final String expectedTitle) {
        assertTrue(
                getNodeFinder().lookup(expectedTitle).tryQuery().isPresent());
    }

    public void verifyMessage(final String expectedPart) {
        assertTrue(
                getLabeledById("jobMessage").getText().contains(expectedPart));
    }

    public void verifyClosed() {
        assertFalse(isCssIdVisible("jobDialog"));
    }

    public void verifyOpen() {
        assertTrue(isCssIdVisible("jobDialog"));
    }

    public void openClearSpecialty() {
        clickOnButtonWithId("jobClearSpecialty");
    }

    /** Remove the specialty through the model while the dialog is open. */
    public void clearSpecialtyBehindDialog(final int workerId) {
        getRobot().interact(() -> {
            colony().clearSpecialty(getModel().getUnitById(workerId));
            rememberWorld();
        });
    }

    public void verifyClearSpecialtyOffered(final boolean expected) {
        assertEquals(expected, getButtonById("jobClearSpecialty").isVisible());
    }

    public void verifyClearPrompt(final int workerId, final String currentRole,
            final String resultingRole) {
        verifyOpen();
        final String text = getLabeledById("jobDetails").getText();
        assertTrue(text.contains(currentRole + " (unit " + workerId
                + ") will become " + resultingRole), text);
        assertEquals("Clear specialty", getButtonById("jobConfirm").getText());
        assertFalse(getButtonById("jobConfirm").isDisabled());
        assertEquals("", getLabeledById("jobMessage").getText());
    }

    public void verifyClearBlocked(final String expectedReasonPart) {
        verifyOpen();
        assertTrue(
                getLabeledById("jobMessage").getText()
                        .contains(expectedReasonPart),
                () -> getLabeledById("jobMessage").getText());
        assertTrue(getButtonById("jobConfirm").isDisabled());
    }

    public void verifyClearRejectedAsChanged(final String expectedReasonPart) {
        verifyClearBlocked("The colonist changed");
        verifyClearBlocked(expectedReasonPart);
    }

    public void verifyFieldJob(final int workerId, final Location at,
            final Goods expected) {
        final ColonyField field = colony().getColonyFields().stream().filter(
                f -> f.getUnit().map(Unit::getId).orElse(-1) == workerId)
                .findFirst().orElseThrow();
        assertEquals(at, field.getLocation());
        assertEquals(expected, field.getProduction().orElseThrow());
        assertEquals(1, colony().getColonyFields().stream().filter(
                f -> f.getUnit().map(Unit::getId).orElse(-1) == workerId)
                .count());
    }

    public void verifyIndoorJob(final int workerId,
            final ConstructionType buildingType, final int index) {
        final org.microcol.model.ConstructionSlot slot = colony()
                .getConstructionByType(buildingType).getConstructionSlots()
                .get(index);
        assertSame(getModel().getUnitById(workerId), slot.getUnit());
        assertSame(slot, getModel().getUnitById(workerId).getPlaceConstruction()
                .getConstructionSlot());
    }

    public void verifyNeighborUnchanged(final int workerId, final Location at,
            final Goods expected) {
        final ColonyField field = getModel().getColonyById(1).orElseThrow()
                .getColonyFields().stream().filter(f -> f.getUnit()
                        .map(Unit::getId).orElse(-1) == workerId)
                .findFirst().orElseThrow();
        assertEquals(at, field.getLocation());
        assertEquals(expected, field.getProduction().orElseThrow());
    }

    public void occupyBestFieldWhileOpen(final int otherWorker) {
        getRobot().interact(() -> {
            getModel().getUnitById(otherWorker).placeToColonyField(
                    colony().getColonyFieldInDirection(Direction.northWest),
                    GoodsType.LUMBER);
            rememberWorld();
        });
    }

    public void changeSourceCropWhileOpen(final GoodsType crop) {
        getRobot().interact(() -> {
            colony().getColonyFieldInDirection(Direction.west)
                    .setProducedGoodsType(crop);
            rememberWorld();
        });
    }

    private boolean dropChangeApplied;

    public void dragAfterEquipmentStockChanges(final int workerId,
            final String buildingId, final boolean buildingBody)
            throws Exception {
        dragWithInterveningChange(workerId, buildingId, buildingBody,
                () -> colony().getWarehouse()
                        .addGoods(Goods.of(GoodsType.MUSKET, 51)));
    }

    public void dragAfterSlotIsTaken(final int workerId,
            final boolean buildingBody) throws Exception {
        dragWithInterveningChange(workerId, "id_blacksmith", buildingBody,
                () -> getModel().getUnitById(6)
                        .placeToColonyStructureSlot(colony()
                                .getConstructionByType(
                                        ConstructionType.BLACKSMITHS_HOUSE)
                                .getConstructionSlots().get(1)));
    }

    private void dragWithInterveningChange(final int workerId,
            final String buildingId, final boolean buildingBody,
            final Runnable interveningChange) throws Exception {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                .stream().filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        final javafx.event.EventHandler<javafx.scene.input.DragEvent> change = event -> {
            interveningChange.run();
            dropChangeApplied = true;
            rememberWorld();
        };
        final javafx.scene.Node source = getNodeFinder()
                .lookup("#colonyUnit-" + workerId).query();
        final Point2D start = source.localToScreen(
                source.getBoundsInLocal().getCenterX(),
                source.getBoundsInLocal().getCenterY());
        final java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(
                1);
        final java.util.concurrent.CountDownLatch finished = new java.util.concurrent.CountDownLatch(
                1);
        final javafx.event.EventHandler<javafx.scene.input.MouseEvent> detected = event -> javafx.application.Platform
                .runLater(started::countDown);
        final javafx.event.EventHandler<javafx.scene.input.DragEvent> done = event -> finished
                .countDown();
        getRobot().interact(() -> {
            building.addEventFilter(javafx.scene.input.DragEvent.DRAG_DROPPED,
                    change);
            source.addEventHandler(javafx.scene.input.MouseEvent.DRAG_DETECTED,
                    detected);
            source.addEventHandler(javafx.scene.input.DragEvent.DRAG_DONE,
                    done);
        });
        boolean released = false;
        try {
            getRobot().moveTo(start.add(0, -8)).moveTo(start);
            getRobot().press(MouseButton.PRIMARY);
            getRobot().moveTo(start.add(15, 0));
            if (!started.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "The native indoor drag must start");
            }
            getRobot().moveTo(buildingBody ? canvas.localToScreen(50, 30)
                    : slotPoint(buildingId, 1));
            getRobot().release(MouseButton.PRIMARY);
            released = true;
            if (!finished.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "The native indoor drag must finish");
            }
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            if (!released) {
                getRobot().release(MouseButton.PRIMARY);
            }
            getRobot().interact(() -> {
                building.removeEventFilter(
                        javafx.scene.input.DragEvent.DRAG_DROPPED, change);
                source.removeEventHandler(
                        javafx.scene.input.MouseEvent.DRAG_DETECTED, detected);
                source.removeEventHandler(
                        javafx.scene.input.DragEvent.DRAG_DONE, done);
            });
        }
    }

    public void verifyDropRejected(final int workerId) {
        assertTrue(dropChangeApplied,
                "The drop must reach the intervening change");
        verifyMessage("This worker cannot take that job now");
        verifyWorldUnchanged();
        assertTrue(getModel().getUnitById(workerId).isAtPlaceLocation());
    }

    public void verifyEquipmentDropRejected(final int workerId) {
        verifyDropRejected(workerId);
        final org.microcol.model.unit.UnitFreeColonist worker = (org.microcol.model.unit.UnitFreeColonist) getModel()
                .getUnitById(workerId);
        assertTrue(worker.isAtPlaceLocation());
        assertTrue(worker.isHoldingGuns());
        assertEquals(Goods.of(GoodsType.MUSKET, 51),
                colony().getWarehouse().getGoods(GoodsType.MUSKET));
    }

    public void dragCenterToBuilding(final int workerId,
            final String buildingId) throws Exception {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                .stream().filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        getRobot().drag("#colonyUnit-" + workerId, MouseButton.PRIMARY)
                .dropTo(canvas.localToScreen(50, 30));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(workerId).isAtPlaceConstructionSlot());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dragCenterToIndoor(final int workerId, final String buildingId,
            final int slotIndex) throws Exception {
        getRobot().drag("#colonyUnit-" + workerId, MouseButton.PRIMARY)
                .dropTo(slotPoint(buildingId, slotIndex));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(workerId).isAtPlaceConstructionSlot());
        WaitForAsyncUtils.waitForFxEvents();
    }
}
