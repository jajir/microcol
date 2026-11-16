package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.akropia.model.Colony;
import org.akropia.model.ColonyField;
import org.akropia.model.ColonyJobChoice;
import org.akropia.model.ConstructionType;
import org.akropia.model.Direction;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public final class ColonyJobPage extends AbstractScreen {
    private com.google.gson.JsonElement remembered;
    private javafx.scene.control.RadioButton selectedRow;
    private String colonyName = "New Amsterdam";

    public ColonyJobPage(final TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void openFromField(final int unitId) {
        openFromField("New Amsterdam", unitId);
    }

    /**
     * Open the job menu of a worker standing on a field of the named colony.
     *
     * @param colonyName name of the colony that owns the field
     * @param unitId     worker on the field
     */
    public void openFromField(final String colonyName, final int unitId) {
        this.colonyName = colonyName;
        ColonyScreen.of(getContext(), colonyName).openCropMenu(unitId);
        openWorkMenu();
    }

    public void openFromCenter(final int unitId) {
        colonyName = getModel().getColonies().stream()
                .filter(value -> value.getLocation()
                        .equals(getModel().getUnitById(unitId).getLocation()))
                .findFirst().orElseThrow().getName();
        revealGameContent(
                getNodeFinder().lookup("#colonyUnit-" + unitId).query());
        getRobot().clickOn("#colonyUnit-" + unitId, MouseButton.SECONDARY);
        openWorkMenu();
    }

    public void openFromCenterWithKeyboard(final int unitId) {
        colonyName = getModel().getColonies().stream()
                .filter(value -> value.getLocation()
                        .equals(getModel().getUnitById(unitId).getLocation()))
                .findFirst().orElseThrow().getName();
        revealGameContent(
                getNodeFinder().lookup("#colonyUnit-" + unitId).query());
        getRobot().clickOn("#colonyUnit-" + unitId).push(KeyCode.SHIFT,
                KeyCode.F10);
        openWorkMenu();
    }

    private Point2D slotPoint(final String buildingId, final int index) {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                .stream().filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        revealGameContent(canvas);
        return canvas.localToScreen(16 + 35 * index, 16);
    }

    public void openFromIndoor(final String buildingId, final int index)
            throws Exception {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        revealGameContent(building);
        getRobot().clickOn(slotPoint(buildingId, index), MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
        openWorkMenu();
    }

    private void openWorkMenu() {
        WaitForAsyncUtils.waitForFxEvents();
        final String id = "workForColony-" + getModel().getColonies().stream()
                .filter(value -> value.getName().equals(colonyName)).findFirst()
                .orElseThrow().getId();
        if (!isCssIdShown(id)) {
            return;
        }
        getRobot().moveTo("#" + id);
        getRobot().sleep(400);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void select(final ColonyJobChoice.Kind kind, final GoodsType goods) {
        if (kind == ColonyJobChoice.Kind.INDOOR && getNodeFinder()
                .lookup(node -> node.getId() != null
                        && node.getId().startsWith("indoorWorkForColony-"))
                .tryQuery().isPresent()) {
            getRobot().push(KeyCode.ESCAPE);
            getRobot().moveTo(getNodeFinder()
                    .lookup(node -> node.getId() != null
                            && node.getId().startsWith("indoorWorkForColony-"))
                    .queryAs(javafx.scene.Node.class));
            getRobot().sleep(400);
            WaitForAsyncUtils.waitForFxEvents();
        }
        selectedRow = getNodeFinder().lookup(".field-output-choice")
                .queryAllAs(javafx.scene.control.RadioButton.class).stream()
                .filter(row -> row.getId() != null
                        && !row.getId().startsWith("colony-field-")
                        && row.getProperties()
                                .get("jobChoice") instanceof ColonyJobChoice)
                .filter(row -> {
                    final ColonyJobChoice choice = (ColonyJobChoice) row
                            .getProperties().get("jobChoice");
                    return choice.getKind() == kind
                            && choice.getGoodsType() == goods;
                }).findFirst().orElseThrow();
        moveToSubmenuRow(selectedRow);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        if (isCssIdVisible("clearSpecialtyDialog")) {
            getRobot().clickOn("#jobConfirm");
        } else {
            getRobot().clickOn(selectedRow);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmWithEnter() {
        if (isCssIdVisible("clearSpecialtyDialog")) {
            getRobot().push(KeyCode.ENTER);
        } else {
            getRobot().interact(selectedRow::requestFocus);
            getRobot().push(KeyCode.ENTER);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        if (isCssIdVisible("clearSpecialtyDialog")) {
            getRobot().push(KeyCode.ESCAPE);
        } else {
            getRobot().push(KeyCode.ESCAPE).push(KeyCode.ESCAPE);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void escape() {
        cancel();
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
        assertNotNull(selectedRow);
        assertTrue(selectedRow.getAccessibleHelp().contains(expectedPart),
                selectedRow.getAccessibleHelp());
        assertEquals(!canConfirm, selectedRow.isDisabled());
    }

    public void verifyVisibleChoice(final String expectedTitle) {
        assertTrue(
                getNodeFinder().lookup(expectedTitle).tryQuery().isPresent());
    }

    public void verifyMessage(final String expectedPart) {
        assertTrue(getNodeFinder().lookup((
                javafx.scene.Node node) -> node instanceof javafx.scene.control.Label
                        && ((javafx.scene.control.Label) node).getText()
                                .contains(expectedPart))
                .tryQuery().isPresent());
    }

    /**
     * The manufacturing choice that a convert was refused is offered now: the
     * details carry no refusal text and the confirmation is enabled.
     */
    public void verifyManufacturingOffered(final String refusalPart) {
        verifyOpen();
        assertFalse(selectedRow.getAccessibleHelp().contains(refusalPart));
        assertFalse(selectedRow.isDisabled());
    }

    public void verifyClosed() {
        assertFalse(isCssIdVisible("clearSpecialtyDialog"));
        assertFalse(isCssIdVisible("unitActionsMenu"));
    }

    public void verifyOpen() {
        assertTrue(isCssIdVisible("unitActionsMenu")
                || isCssIdVisible("clearSpecialtyDialog"));
    }

    public void openClearSpecialty() {
        getRobot().clickOn("#jobClearSpecialty");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Remove the specialty through the model while the dialog is open. */
    public void clearSpecialtyBehindDialog(final int workerId) {
        getRobot().interact(() -> {
            colony().clearSpecialty(getModel().getUnitById(workerId));
            rememberWorld();
        });
    }

    public void verifyClearSpecialtyOffered(final boolean expected) {
        assertEquals(expected, getNodeFinder().lookup("#jobClearSpecialty")
                .tryQuery().isPresent());
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
        final org.akropia.model.ConstructionSlot slot = colony()
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
                        .addGoods(Goods.of(GoodsType.WEAPONS, 51)));
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
        final javafx.event.EventHandler<javafx.scene.input.DragEvent> change = event -> {
            interveningChange.run();
            dropChangeApplied = true;
            rememberWorld();
        };
        final javafx.scene.Node source = getNodeFinder()
                .lookup("#colonyUnit-" + workerId).query();
        revealGameContent(source, building);
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
            getRobot().moveTo(buildingBody
                    ? building.lookup(".building-top").localToScreen(25, 23)
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
        final org.akropia.model.unit.UnitFreeColonist worker = (org.akropia.model.unit.UnitFreeColonist) getModel()
                .getUnitById(workerId);
        assertTrue(worker.isAtPlaceLocation());
        assertTrue(worker.isHoldingWeapons());
        assertEquals(Goods.of(GoodsType.WEAPONS, 51),
                colony().getWarehouse().getGoods(GoodsType.WEAPONS));
    }

    public void dragCenterToBuilding(final int workerId,
            final String buildingId) throws Exception {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        revealGameContent(
                getNodeFinder().lookup("#colonyUnit-" + workerId).query(),
                getNodeFinder().lookup("#" + buildingId).query());
        getRobot().drag("#colonyUnit-" + workerId, MouseButton.PRIMARY)
                .dropTo(building.lookup(".building-top").localToScreen(25, 23));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(workerId).isAtPlaceConstructionSlot());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dragCenterToIndoor(final int workerId, final String buildingId,
            final int slotIndex) throws Exception {
        revealGameContent(
                getNodeFinder().lookup("#colonyUnit-" + workerId).query(),
                getNodeFinder().lookup("#" + buildingId).query());
        getRobot().drag("#colonyUnit-" + workerId, MouseButton.PRIMARY)
                .dropTo(slotPoint(buildingId, slotIndex));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(workerId).isAtPlaceConstructionSlot());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void assignField(final int unitId, final Location target,
            final GoodsType goods) throws Exception {
        final Colony current = getModel().getColonies().stream()
                .filter(value -> value.getName().equals(colonyName)).findFirst()
                .orElseThrow();
        final Direction direction = Direction.valueOf(
                Location.of(target.getX() - current.getLocation().getX(),
                        target.getY() - current.getLocation().getY()));
        final ColonyScreen screen = ColonyScreen.of(getContext(), colonyName);
        if (getModel().getUnitById(unitId).isAtPlaceColonyField()) {
            screen.dragFieldWorkerTo(unitId, direction);
        } else {
            screen.moveWorkerFromPierToField(unitId, direction);
        }
        WaitForAsyncUtils.waitForFxEvents();
        openFromField(colonyName, unitId);
        select(ColonyJobChoice.Kind.CURRENT_FIELD, goods);
        confirm();
    }

    public void verifyJobNotOffered(final ColonyJobChoice.Kind kind,
            final GoodsType goods) {
        assertTrue(getNodeFinder().lookup(".field-output-choice")
                .queryAllAs(javafx.scene.control.RadioButton.class).stream()
                .map(row -> row.getProperties().get("jobChoice"))
                .filter(value -> value instanceof ColonyJobChoice)
                .map(value -> (ColonyJobChoice) value)
                .noneMatch(choice -> choice.getKind() == kind
                        && choice.getGoodsType() == goods));
    }

    public void verifyCurrentTileChoices(final boolean indoor) {
        verifyCurrentTileChoices(indoor, !indoor);
    }

    public void verifyCurrentTileChoices(final boolean indoor,
            final boolean field) {
        final java.util.Set<javafx.scene.control.RadioButton> rows = getNodeFinder()
                .lookup(".field-output-choice")
                .queryAllAs(javafx.scene.control.RadioButton.class);
        assertFalse(rows.isEmpty());
        for (final javafx.scene.control.RadioButton row : rows) {
            if (!(row.getProperties()
                    .get("jobChoice") instanceof ColonyJobChoice)) {
                continue;
            }
            final ColonyJobChoice choice = (ColonyJobChoice) row.getProperties()
                    .get("jobChoice");
            assertTrue(choice.getUnavailableReason().isEmpty());
            assertTrue((indoor
                    && choice.getKind() == ColonyJobChoice.Kind.INDOOR)
                    || (field && choice
                            .getKind() == ColonyJobChoice.Kind.CURRENT_FIELD));
            assertFalse(row.isDisabled());
        }
        if (!indoor) {
            assertTrue(getNodeFinder()
                    .lookup(node -> node.getId() != null
                            && node.getId().startsWith("indoorWorkForColony-"))
                    .queryAll().isEmpty());
        }
    }

    public void verifyNoAutomaticFieldChoice() {
        final java.util.Set<javafx.scene.control.RadioButton> rows = getNodeFinder()
                .lookup(".field-output-choice")
                .queryAllAs(javafx.scene.control.RadioButton.class);
        assertFalse(rows.stream().anyMatch(row -> {
            final Object value = row.getProperties().get("jobChoice");
            return value instanceof ColonyJobChoice && ((ColonyJobChoice) value)
                    .getKind() == ColonyJobChoice.Kind.BEST_FIELD;
        }));
        assertFalse(
                getNodeFinder().lookup("#chooseWork").tryQuery().isPresent());
        assertFalse(isCssIdVisible("jobDialog"));
    }

    public void assignIndoorFromField(final int unitId,
            final String buildingId) {
        ColonyScreen.of(getContext(), colonyName)
                .moveFieldWorkerToBuilding(unitId, buildingId);
    }

    public void assignIndoorWorkerToField(final String buildingId,
            final int slot, final int unitId, final Location location,
            final GoodsType goods) {
        final ColonyScreen screen = ColonyScreen.of(getContext(), colonyName);
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        revealGameContent(building,
                getNodeFinder().lookup("#colony-field-map").query());
        getRobot().drag(slotPoint(buildingId, slot), MouseButton.PRIMARY)
                .dropTo(screen.fieldPoint(location));
        WaitForAsyncUtils.waitForFxEvents();
        openFromField(colonyName, unitId);
        select(ColonyJobChoice.Kind.CURRENT_FIELD, goods);
        confirm();
    }

    public void verifyOnlyColonyMenu(final int colonyId) {
        final java.util.Set<javafx.scene.Node> entries = getNodeFinder()
                .lookup(node -> node.getId() != null
                        && node.getId().startsWith("workForColony-"))
                .queryAll();
        assertEquals(1, entries.size());
        assertEquals("workForColony-" + colonyId,
                entries.iterator().next().getId());
    }

    public void verifySameProductionTable() {
        final java.util.Set<javafx.scene.control.RadioButton> rows = getNodeFinder()
                .lookup(".field-output-choice")
                .queryAllAs(javafx.scene.control.RadioButton.class);
        final java.util.List<javafx.scene.control.RadioButton> inline = rows
                .stream()
                .filter(row -> row.getId().startsWith("colony-field-output-"))
                .sorted(java.util.Comparator
                        .comparingDouble(row -> row.localToScene(0, 0).getY()))
                .collect(java.util.stream.Collectors.toList());
        final java.util.List<javafx.scene.control.RadioButton> popup = rows
                .stream().filter(row -> row.getId().startsWith("mapJob-"))
                .sorted(java.util.Comparator
                        .comparingDouble(row -> row.localToScene(0, 0).getY()))
                .collect(java.util.stream.Collectors.toList());
        assertFalse(inline.isEmpty());
        assertEquals(
                inline.stream().map(javafx.scene.Node::getAccessibleText)
                        .collect(java.util.stream.Collectors.toList()),
                popup.stream().map(javafx.scene.Node::getAccessibleText)
                        .collect(java.util.stream.Collectors.toList()));
        assertEquals(
                inline.stream()
                        .map(javafx.scene.control.RadioButton::isSelected)
                        .collect(java.util.stream.Collectors.toList()),
                popup.stream().map(javafx.scene.control.RadioButton::isSelected)
                        .collect(java.util.stream.Collectors.toList()));
    }

    public void captureProductionTable(final String filename) throws Exception {
        final javafx.scene.Parent table = selectedRow.getParent()
                .getChildrenUnmodifiable().stream()
                .anyMatch(node -> "jobDetails".equals(node.getId()))
                        ? selectedRow.getParent()
                        : selectedRow.getParent().getParent();
        final String previousId = table.getId();
        getRobot().interact(() -> table.setId("capturedProductionTable"));
        try {
            captureDialog("capturedProductionTable", filename);
        } finally {
            getRobot().interact(() -> table.setId(previousId));
        }
    }

}
