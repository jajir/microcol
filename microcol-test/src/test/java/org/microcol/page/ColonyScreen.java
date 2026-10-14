package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.gui.Tile.TILE_CENTER;
import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.gui.screen.colony.ColonyButtonsPanel;
import org.microcol.gui.screen.colony.PanelColonyFields;
import org.microcol.gui.screen.colony.PanelColonyGood;
import org.microcol.gui.screen.colony.PanelColonyName;
import org.microcol.gui.screen.colony.PanelOutsideColonyUnit;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.Goods;
import org.microcol.model.GoodsProductionStats;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitGalleon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ColonyScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(ColonyScreen.class);

    private final PageComponentPanelDock pageComponentPanelDock;

    private final Colony colony;

    public static ColonyScreen of(final TestContext context,
            final String expectedName) {
        return new ColonyScreen(context, expectedName);
    }

    private ColonyScreen(final TestContext context, final String expectedName) {
        super(context);
        verifyColonyName(expectedName);
        colony = getModel().getColonyByName(expectedName).get();
        pageComponentPanelDock = PageComponentPanelDock.of(context);
    }

    public void verifyColonyName(final String expectedNamePart) {
        final Labeled labeled = getLabeledById(PanelColonyName.COLONY_NAME_ID);
        logger.info("Colony name: " + labeled.getText());
        assertTrue(labeled.getText().contains(expectedNamePart),
                String.format("Text '%s' should appear in colony name '%s'",
                        expectedNamePart, labeled.getText()));
    }

    public GamePage close() {
        final Button buttonNextTurn = getButtonById(
                ColonyButtonsPanel.CLOSE_BUTTON_ID);
        getRobot().clickOn(buttonNextTurn);
        return GamePage.of(getContext());
    }

    public ColonyNameDialog rename() {
        getRobot().clickOn(getLabeledById(PanelColonyName.COLONY_NAME_ID));
        WaitForAsyncUtils.waitForFxEvents();
        return new ColonyNameDialog(getContext());
    }

    public ColonyScreen selectUnitFromPort(final int unitIndex) {
        pageComponentPanelDock.selectUnitFromPort(unitIndex);
        return this;
    }

    public void verifyUnitInPortSelected(final int unitIndex,
            final boolean expectedSelected) {
        pageComponentPanelDock.verifyUnitInPortSelected(unitIndex,
                expectedSelected);
    }

    public void verifyWarehouseWarning(final String expectedText) {
        assertEquals(expectedText,
                getLabeledById("warehouseTransferMessage").getText());
    }

    public void closeWarehouseWarning() {
        clickOnButtonWithId("buttonOk");
    }

    public void verifyNumberOfShipsInPort(final int expectedCount) {
        pageComponentPanelDock.verifyNumberOfShipsInPort(expectedCount);
    }

    public void verifyNumberOfUnitsAtPier(
            final int expectedNumberOfUnitsAtPier) {
        assertEquals(expectedNumberOfUnitsAtPier, getUnitsAtPier().size(),
                String.format(
                        "Expected numeber of units is '%s' but really there is '%s' units.",
                        expectedNumberOfUnitsAtPier, getUnitsAtPier().size()));
    }

    public void dragUnitFromPierToShipCargoSlot(final int unitIndexInPier,
            final int cargoSlotIndex) {
        getRobot()
                .drag(getUnitsAtPier().get(unitIndexInPier),
                        MouseButton.PRIMARY)
                .dropTo(getListOfCrates().get(cargoSlotIndex))
                .release(MouseButton.PRIMARY);
    }

    public DialogChooseGoodsAmount dragGoodsFromWarehouseToShipCargoSlotWithPressedControll(
            final int goodsIndexInWarehouse, final int cargoSlotIndex) {
        getRobot().press(getGoodsAmountModifiers());
        dragGoodsFromWarehouseToShipCargoSlot(goodsIndexInWarehouse,
                cargoSlotIndex);
        getRobot().release(getGoodsAmountModifiers());
        return DialogChooseGoodsAmount.of(getContext());
    }

    public void dragGoodsFromWarehouseToShipCargoSlot(
            final int goodsIndexInWarehouse, final int cargoSlotIndex) {
        getRobot()
                .drag(getListOfGoodsImagesInWarehouse()
                        .get(goodsIndexInWarehouse), MouseButton.PRIMARY)
                .dropTo(getListOfCrates().get(cargoSlotIndex))
                .release(MouseButton.PRIMARY);
    }

    public DialogChooseGoodsAmount dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(
            final int cargoSlotIndex, final int goodsIndexInWarehouse)
            throws InterruptedException {
        getRobot().press(getGoodsAmountModifiers());
        try {
            dragGoodsFromShipCargoSlotToWarehouse(cargoSlotIndex,
                    goodsIndexInWarehouse);
        } finally {
            getRobot().release(getGoodsAmountModifiers());
        }
        return DialogChooseGoodsAmount.of(getContext());
    }

    public void dragGoodsFromShipCargoSlotToWarehouse(final int cargoSlotIndex,
            final int goodsIndexInWarehouse) throws InterruptedException {
        final StackPane crate = getListOfCrates().get(cargoSlotIndex);
        completeDrag(crate,
                () -> getRobot().drag(crate, MouseButton.PRIMARY)
                        .dropTo(getListOfGoodsImagesInWarehouse()
                                .get(goodsIndexInWarehouse))
                        .release(MouseButton.PRIMARY));
    }

    public void dragGoodsOutsideWindow(final int cargoSlotIndex)
            throws InterruptedException {
        final StackPane crate = getListOfCrates().get(cargoSlotIndex);
        completeDrag(crate,
                () -> getRobot().drag(crate, MouseButton.PRIMARY)
                        .dropTo(getPrimaryStage().getX()
                                + getPrimaryStage().getWidth() + 50,
                                getPrimaryStage().getY()
                                        + getPrimaryStage().getHeight() / 2)
                        .release(MouseButton.PRIMARY));
    }

    private void completeDrag(final javafx.scene.Node crate,
            final Runnable drag) throws InterruptedException {
        final CountDownLatch finished = new CountDownLatch(1);
        final EventHandler<DragEvent> done = event -> finished.countDown();
        getRobot().interact(
                () -> crate.addEventHandler(DragEvent.DRAG_DONE, done));
        try {
            drag.run();
            // Native drag/drop can outlive the robot's release and FX event
            // queue.
            if (!finished.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The native drag must finish");
            }
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            getRobot().interact(
                    () -> crate.removeEventHandler(DragEvent.DRAG_DONE, done));
        }
    }

    public ColonyScreen verifyNumberOfUnitsOnFields(
            final Integer expectedumberOfUnitsAtFields) {
        final Integer unitsInColony = colony.getColonyFields().stream()
                .mapToInt(field -> field.isEmpty() ? 0 : 1).sum();
        assertEquals(expectedumberOfUnitsAtFields, unitsInColony);
        return this;
    }

    public void verifyProductionInRange(final int actualProduction,
            final int expectedMinimum, final int expectedMaximum) {
        assertTrue(
                actualProduction >= expectedMinimum
                        && actualProduction <= expectedMaximum,
                String.format("Unexpected corn production '%s'",
                        actualProduction));
    }

    public void verifyWarehouseGoods(final Goods expectedGoods) {
        assertEquals(expectedGoods.getAmount(), colony.getWarehouse()
                .getGoods(expectedGoods.getType()).getAmount());
    }

    public void verifyGoodsInShip(final int cargoSlotIndex,
            final Goods expectedGoods) {
        final List<Unit> ships = getModel().getUnitsAt(colony.getLocation())
                .stream()
                .filter(unit -> unit.getType().equals(UnitType.GALLEON))
                .collect(Collectors.toList());
        assertEquals(1, ships.size());
        new ShipWrapper((UnitGalleon) ships.get(0))
                .verifyCargoGoods(cargoSlotIndex, expectedGoods);
    }

    public void verifyColonyValid(final boolean expectedValid) {
        assertEquals(expectedValid, colony.isValid());
    }

    public void verifyColonyPresent(final String colonyName,
            final boolean expectedPresent) {
        assertEquals(expectedPresent,
                getModel().getColonyByName(colonyName).isPresent());
    }

    public void verifyCarrierPresent(final int unitId,
            final boolean expectedPresent) {
        assertEquals(expectedPresent,
                getModel().tryGetUnitById(unitId).isPresent());
    }

    /**
     * Select unit that could dragged to ship.
     *
     * @return list panes with units
     */
    private List<Pane> getUnitsAtPier() {
        final NodeFinder nodeFinder = getNodeFinder();
        final Set<Pane> unitsSet = nodeFinder
                .lookup("." + PanelOutsideColonyUnit.UNIT_AT_PIER_STYLE)
                .queryAll();
        return new ArrayList<Pane>(unitsSet);
    }

    private List<StackPane> getListOfCrates() {
        return pageComponentPanelDock.getListOfCrates();
    }

    private List<StackPane> getListOfGoodsImagesInWarehouse() {
        final String cssClass = "." + PanelColonyGood.IMAGE_GOODS_CLASS;
        final Set<StackPane> cratesSet = getNodeFinder().lookup(cssClass)
                .queryAll();
        return new ArrayList<StackPane>(cratesSet);
    }

    public void verifyThatProductionIs(final GoodsType goodsType,
            final Integer expectedAmount) {
        final GoodsProductionStats stats = colony.getGoodsStats()
                .getStatsByType(goodsType);
        assertEquals(expectedAmount, Integer.valueOf(stats.getNetProduction()));
    }

    /**
     * Drag unit from pier to random empty field.
     *
     * @param indexOfUnitAtPier which unit will be dragged
     * @return net production of corn at newly occupied field
     */
    public int moveUnitFromPietToEmptyField(final int indexOfUnitAtPier) {
        final ColonyField colonyField = colony.getColonyFields().stream()
                .filter(field -> field.isEmpty()
                        && field.getGoodsTypeProduction(GoodsType.CORN) > 0)
                .findAny().get();
        getRobot()
                .drag(getUnitsAtPier().get(indexOfUnitAtPier),
                        MouseButton.PRIMARY)
                .dropTo(findPlaceOfField(colonyField))
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        return colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getNetProduction();
    }

    public void dragFieldWorkerTo(final int unitId,
            final org.microcol.model.Direction target)
            throws InterruptedException {
        final ColonyField source = colony.getColonyFields().stream().filter(
                field -> field.getUnit().map(Unit::getId).orElse(-1) == unitId)
                .findFirst().orElseThrow();
        final javafx.scene.Node canvas = getContext()
                .getClassFromGuice(PanelColonyFields.class).getContent()
                .lookup("Canvas");
        final Point2D start = findPlaceOfField(source);
        final Point2D destination = findPlaceOfField(
                colony.getColonyFieldInDirection(target));
        final CountDownLatch positioned = new CountDownLatch(1);
        final CountDownLatch pressed = new CountDownLatch(1);
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(1);
        final EventHandler<javafx.scene.input.MouseEvent> moved = event -> {
            if (!event.isPrimaryButtonDown()
                    && Math.abs(event.getScreenX() - start.getX()) < 2
                    && Math.abs(event.getScreenY() - start.getY()) < 2) {
                positioned.countDown();
            }
        };
        final EventHandler<javafx.scene.input.MouseEvent> press = event -> pressed
                .countDown();
        final EventHandler<javafx.scene.input.MouseEvent> detected = event -> javafx.application.Platform
                .runLater(started::countDown);
        final EventHandler<DragEvent> done = event -> finished.countDown();
        getRobot().interact(() -> {
            canvas.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_MOVED,
                    moved);
            canvas.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED,
                    moved);
            canvas.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                    press);
            canvas.addEventHandler(javafx.scene.input.MouseEvent.DRAG_DETECTED,
                    detected);
            canvas.addEventHandler(DragEvent.DRAG_DONE, done);
        });
        boolean released = false;
        try {
            // Native drop cleanup may restore the pointer with ENTERED instead
            // of MOVED; both must reach the source before the next press.
            getRobot().moveTo(start.add(0, -8)).moveTo(start);
            if (!positioned.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "The field drag source must receive pointer movement or entry");
            }
            getRobot().press(MouseButton.PRIMARY);
            if (!pressed.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "The field drag source must receive the press");
            }
            // Cross drag hysteresis while still inside the original 45-pixel
            // field.
            getRobot().moveTo(start.add(TILE_WIDTH_IN_PX / 3.0, 0));
            if (!started.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The field drag must start");
            }
            getRobot().moveTo(destination).release(MouseButton.PRIMARY);
            released = true;
            if (!finished.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The field drag must finish");
            }
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            if (!released) {
                getRobot().release(MouseButton.PRIMARY);
            }
            getRobot().interact(() -> {
                canvas.removeEventHandler(
                        javafx.scene.input.MouseEvent.MOUSE_MOVED, moved);
                canvas.removeEventHandler(
                        javafx.scene.input.MouseEvent.MOUSE_ENTERED, moved);
                canvas.removeEventHandler(
                        javafx.scene.input.MouseEvent.MOUSE_PRESSED, press);
                canvas.removeEventHandler(
                        javafx.scene.input.MouseEvent.DRAG_DETECTED, detected);
                canvas.removeEventHandler(DragEvent.DRAG_DONE, done);
            });
        }
    }

    public void verifyFieldWorker(final int expectedUnitId,
            final Location expectedLocation, final int expectedCornYield) {
        final ColonyField field = colony.getColonyFields().stream()
                .filter(candidate -> candidate.getUnit().map(Unit::getId)
                        .orElse(-1) == expectedUnitId)
                .findFirst().orElseThrow();
        assertEquals(expectedLocation, field.getLocation());
        assertEquals(expectedCornYield,
                field.getProduction().orElseThrow().getAmount());
        assertEquals(GoodsType.CORN,
                field.getProduction().orElseThrow().getType());
        assertEquals(1, colony.getColonyFields().stream()
                .filter(ColonyField::isNotEmpty).count());
    }

    private Point2D findPlaceOfField(final ColonyField colonyField) {
        final PanelColonyFields panelColonyFields = getContext()
                .getClassFromGuice(PanelColonyFields.class);
        final Bounds boundsInScreen = panelColonyFields.getContent()
                .localToScreen(
                        panelColonyFields.getContent().getBoundsInLocal());

        final Location loc = colonyField.getDirection().getVector();
        final double x = boundsInScreen.getMinX()
                + (loc.getX() + 1) * TILE_WIDTH_IN_PX + TILE_CENTER.getX();
        final double y = boundsInScreen.getMinY()
                + (loc.getY() + 1) * TILE_WIDTH_IN_PX + TILE_CENTER.getY();
        return new Point2D(x, y);
    }

    private VBox getGoodsPanel(final int indexOfGoodsInWarehouse) {
        final List<VBox> goodsPanels = getListOfNodes(".warehouseGoods");
        return goodsPanels.get(indexOfGoodsInWarehouse);
    }

    public ColonyScreen verifyNumberOfGoodsInWrehouse(
            final int indexOfGoodsInWarehouse, final int expectedGoodsAmount) {
        final VBox goods = getGoodsPanel(indexOfGoodsInWarehouse);
        final String amountStr = getNodeFinder().from(goods).lookup(".amount")
                .queryLabeled().getText();
        final int amount = Integer.valueOf(amountStr);
        assertEquals(expectedGoodsAmount, amount, String.format(
                "In warehouse at position %s was expected %s goods but there is %s goods",
                indexOfGoodsInWarehouse, expectedGoodsAmount, amount));
        return this;
    }

}
