package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.akropia.gui.Tile.TILE_CENTER;
import static org.akropia.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.akropia.gui.screen.colony.ColonyButtonsPanel;
import org.akropia.gui.screen.colony.PanelColonyFields;
import org.akropia.gui.screen.colony.PanelColonyGood;
import org.akropia.gui.screen.colony.PanelColonyName;
import org.akropia.gui.screen.colony.PanelOutsideColonyUnit;
import org.akropia.model.Colony;
import org.akropia.model.ColonyField;
import org.akropia.model.Goods;
import org.akropia.model.GoodsProductionStats;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;
import org.akropia.model.unit.UnitGalleon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;
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

    public void verifyTownCommons(final String expectedText) {
        new ColonyLayoutPage(getContext()).verifyCenter();
        assertEquals(expectedText,
                getLabeledById("colony-field-yield").getText());
    }

    public void showWarehouse() {
        revealGameContent(getNodeFinder().lookup(".colony-stock").query());
    }

    /**
     * The commons text is shown whole: the label shows exactly the expected
     * text, its skin did not shorten it with an ellipsis and its width holds
     * the full text.
     */
    public void verifyTownCommonsNotEllipsized(final String expectedText) {
        final Labeled label = getLabeledById("colony-field-yield");
        assertEquals(expectedText, label.getText());
        final javafx.scene.text.Text shown = (javafx.scene.text.Text) label
                .lookup(".text");
        assertEquals(expectedText, shown.getText(),
                "Commons text was shortened on screen");
        assertTrue(label.getWidth() + 0.5 >= label.prefWidth(-1),
                String.format("Commons label width %s is below its text %s",
                        label.getWidth(), label.prefWidth(-1)));
    }

    /**
     * The warehouse row keeps its expected number of goods side by side, each
     * with a usable width inside the window.
     */
    public void verifyWarehouseGoodsLayout(final int expectedGoodsPanels,
            final double minimumGoodsWidth) {
        final List<VBox> panels = new ArrayList<>(
                this.<VBox>getListOfNodes(".warehouseGoods"));
        assertEquals(expectedGoodsPanels, panels.size());
        for (int index = 0; index < panels.size(); index++) {
            final VBox panel = panels.get(index);
            final Bounds bounds = panel.localToScene(panel.getBoundsInLocal());
            assertEquals(
                    panels.get(0).localToScene(panels.get(0).getBoundsInLocal())
                            .getMinY(),
                    bounds.getMinY(), 0.5, "All warehouse goods share one row");
            assertTrue(bounds.getWidth() >= minimumGoodsWidth);
            assertTrue(bounds.getMinX() >= 0
                    && bounds.getMaxX() <= panel.getScene().getWidth());
            for (int other = index + 1; other < panels.size(); other++) {
                final VBox next = panels.get(other);
                final Bounds second = next
                        .localToScene(next.getBoundsInLocal());
                assertTrue(
                        bounds.getMaxX() <= second.getMinX() + 1
                                || second.getMaxX() <= bounds.getMinX() + 1
                                || bounds.getMaxY() <= second.getMinY() + 1
                                || second.getMaxY() <= bounds.getMinY() + 1,
                        "Goods panels do not overlap");
            }
        }
    }

    /** @return warehouse amount of the goods now, without asserting */
    public int stockOf(final org.akropia.model.GoodsType goodsType) {
        return colony.getWarehouse().getGoods(goodsType).getAmount();
    }

    /**
     * Stock and the nonzero next-turn change share one row under the name; a
     * zero change is hidden.
     */
    public void verifyGoodsForecast(final GoodsType type, final int stock,
            final int change) {
        getRobot().interact(() -> {
            final VBox panel = getNodeFinder()
                    .lookup("#colony-good-" + type.name()).queryAs(VBox.class);
            verifyGoodsValueRow(panel, String.valueOf(stock),
                    change > 0 ? "+" + change
                            : change < 0 ? "-" + Math.abs(change) : "");
        });
    }

    /**
     * Torches and owls show only their nonzero change, never a stored total. An
     * empty expected value means the value is hidden.
     */
    public void verifyLocalProduction(final GoodsType type,
            final String expectedName, final String expectedStock,
            final String expectedProduction) {
        getRobot().interact(() -> {
            final VBox panel = getNodeFinder()
                    .lookup("#colony-good-" + type.name()).queryAs(VBox.class);
            final Label name = (Label) panel.lookup(".goods-name");
            assertEquals(expectedName, name.getText());
            verifyGoodsValueRow(panel, expectedStock, expectedProduction);
            assertNull(panel.lookup(".imageGoods").getOnDragDetected(),
                    "Colony production cannot initiate cargo transport");
            final Text text = (Text) name.lookup(".text");
            assertEquals(name.getText(), text.getText());
            final Node viewport = panel.getScene()
                    .lookup(".screen-scroll .viewport");
            final Bounds visible = viewport
                    .localToScene(viewport.getBoundsInLocal());
            assertTrue(
                    visible.contains(
                            text.localToScene(text.getBoundsInLocal())),
                    "Production must fit in the viewport");
            for (final String value : List.of(".amount", ".diff")) {
                final Node label = panel.lookup(value);
                if (label.isVisible()) {
                    assertTrue(
                            visible.contains(label
                                    .localToScene(label.getBoundsInLocal())),
                            "Production must fit in the viewport");
                }
            }
            assertTrue(getNodeFinder().lookup("#colony-summary").queryAll()
                    .isEmpty());
            assertTrue(getNodeFinder().lookup("#townCommons").queryAll()
                    .isEmpty());
            assertTrue(getNodeFinder().lookup("#colony-owls").queryAll()
                    .isEmpty());
            assertTrue(getNodeFinder().lookup("#colony-torches").queryAll()
                    .isEmpty());
        });
    }

    public void verifyWarehouseStock(
            final org.akropia.model.GoodsType goodsType,
            final int expectedAmount) {
        assertEquals(expectedAmount, stockOf(goodsType), String.format(
                "Warehouse %s should hold %s", goodsType, expectedAmount));
    }

    public void verifyThatStatusBarContains(final String expectedText) {
        final Labeled label = getLabeledById(
                org.akropia.gui.screen.game.components.StatusBarView.STATUS_BAR_LABEL_ID);
        assertTrue(label.getText().contains(expectedText),
                () -> "Expected status: " + expectedText + "; actual: "
                        + label.getText());
    }

    public void assignOutdoorWorkersToTownHall() {
        final java.util.List<Integer> workers = colony.getUnitsInsideColony()
                .stream().filter(unit -> unit.isAtPlaceColonyField())
                .map(unit -> unit.getId())
                .collect(java.util.stream.Collectors.toList());
        for (final int id : workers) {
            final ColonyJobPage jobs = new ColonyJobPage(getContext());
            jobs.openFromField(colony.getName(), id);
            jobs.select(org.akropia.model.ColonyJobChoice.Kind.INDOOR,
                    GoodsType.OWLS);
            jobs.confirm();
        }
    }

    public GamePage close() {
        final Button buttonNextTurn = getButtonById(
                ColonyButtonsPanel.CLOSE_BUTTON_ID);
        getRobot().clickOn(buttonNextTurn);
        return GamePage.of(getContext());
    }

    /** Leave the colony screen with the Escape key, as a player would. */
    public GamePage closeWithEscape() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
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

    /**
     * Selects the ship with this unit ID in the port, so its holds take the
     * dragged goods.
     */
    public ColonyScreen selectShipInPort(final int unitId) {
        final java.util.List<javafx.scene.control.ToggleButton> ships = pageComponentPanelDock
                .getUnitsInPort();
        for (int index = 0; index < ships.size(); index++) {
            final Object ship = ships.get(index).getUserData();
            if (ship instanceof org.akropia.model.Unit
                    && ((org.akropia.model.Unit) ship).getId() == unitId) {
                pageComponentPanelDock.selectUnitFromPort(index);
                return this;
            }
        }
        throw new IllegalStateException("Ship is not in port: " + unitId);
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
        final Pane goods = getListOfGoodsImagesInWarehouse()
                .get(goodsIndexInWarehouse);
        final StackPane crate = getListOfCrates().get(cargoSlotIndex);
        revealGameContent(goods, crate);
        getRobot().drag(goods, MouseButton.PRIMARY).dropTo(crate)
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
        final javafx.scene.Node target = getListOfGoodsImagesInWarehouse()
                .get(goodsIndexInWarehouse);
        revealGameContent(crate);
        completeDrag(crate, () -> {
            getRobot().drag(crate, MouseButton.PRIMARY).moveBy(12, 0);
            // Cargo and stock can be farther apart than the small viewport.
            revealGameContent(target);
            getRobot().dropTo(target).release(MouseButton.PRIMARY);
        });
    }

    public void dragGoodsOutsideWindow(final int cargoSlotIndex)
            throws InterruptedException {
        final StackPane crate = getListOfCrates().get(cargoSlotIndex);
        revealGameContent(crate);
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
                String.format("Unexpected grain production '%s'",
                        actualProduction));
    }

    public void verifyWarehouseGoods(final Goods expectedGoods) {
        assertEquals(expectedGoods.getAmount(), colony.getWarehouse()
                .getGoods(expectedGoods.getType()).getAmount());
    }

    public void verifyTorchProduction(final int expectedAmount) {
        final Labeled output = getNodeFinder()
                .lookup("#id_church .production .diffPositive").queryLabeled();
        assertTrue(output.isVisible());
        assertEquals("+" + expectedAmount, output.getText());
        verifyThatProductionIs(GoodsType.TORCHES, expectedAmount);
    }

    public void verifyProductionArtwork(final String constructionId,
            final String expectedImage) {
        getRobot().interact(() -> {
            final javafx.scene.image.ImageView icon = getNodeFinder()
                    .lookup("#" + constructionId + " .production-icon")
                    .queryAs(javafx.scene.image.ImageView.class);
            assertTrue(icon.isVisible(), constructionId);
            final Canvas reference = new Canvas(22, 22);
            reference.getGraphicsContext2D()
                    .drawImage(new javafx.scene.image.Image(
                            getClass().getResource("/images/" + expectedImage)
                                    .toExternalForm()),
                            0, 0, 22, 22);
            // Attached canvases otherwise inherit the scene's paper fill.
            final var transparent = new javafx.scene.SnapshotParameters();
            transparent.setFill(javafx.scene.paint.Color.TRANSPARENT);
            final var expected = reference.snapshot(transparent, null)
                    .getPixelReader();
            final var scene = icon.getScene();
            final var root = (javafx.scene.layout.Region) scene.getRoot();
            final double width = scene.getWidth();
            final double height = scene.getHeight();
            final double preferredWidth = root.getPrefWidth();
            final double preferredHeight = root.getPrefHeight();
            final javafx.scene.image.PixelReader actual;
            try {
                // Pin snapshot layout, as AbstractScreen.snapshotScene does.
                root.setPrefSize(width, height);
                actual = icon.snapshot(transparent, null).getPixelReader();
            } finally {
                root.setPrefSize(preferredWidth, preferredHeight);
                root.resize(width, height);
                root.layout();
            }
            for (int y = 0; y < 22; y++) {
                for (int x = 0; x < 22; x++) {
                    assertEquals(expected.getArgb(x, y), actual.getArgb(x, y),
                            constructionId + " production icon at " + x + ","
                                    + y);
                }
            }
        });
    }

    private void verifyGoodsValueRow(final VBox panel,
            final String expectedStock, final String expectedChange) {
        final Label name = (Label) panel.lookup(".goods-name");
        final Label amount = (Label) panel.lookup(".amount");
        final Label diff = (Label) panel.lookup(".diff");
        assertTrue(
                amount.getParent() == diff.getParent() && amount.getParent()
                        .getStyleClass().contains("goods-values"),
                "Stock and change share one row");
        final Bounds row = amount.getParent()
                .localToScene(amount.getParent().getLayoutBounds());
        assertTrue(row.getMinY() + 0.5 >= name
                .localToScene(name.getLayoutBounds()).getMaxY(),
                "Values are under the name");
        assertEquals(!expectedStock.isEmpty(), amount.isVisible());
        assertEquals(!expectedChange.isEmpty(), diff.isVisible());
        assertEquals(expectedStock, amount.getText());
        if (!expectedChange.isEmpty()) {
            assertEquals(expectedChange, diff.getText());
            assertEquals(javafx.scene.paint.Color.web(
                    expectedChange.startsWith("+") ? "#316b2f" : "#b13224"),
                    diff.getTextFill());
        }
        if (!expectedStock.isEmpty()) {
            assertEquals(javafx.scene.paint.Color.web("#25231f"),
                    amount.getTextFill(), "Stock keeps the normal color");
        }
        final Bounds column = panel.localToScene(panel.getBoundsInLocal());
        Bounds previous = null;
        for (final Label label : List.of(amount, diff)) {
            if (!label.isVisible()) {
                continue;
            }
            final Text rendered = (Text) label.lookup(".text");
            assertEquals(label.getText(), rendered.getText(),
                    "Goods value must not be ellipsized");
            final Bounds shown = rendered
                    .localToScene(rendered.getBoundsInLocal());
            assertTrue(column.contains(shown),
                    "Goods value must fit its own column");
            if (previous != null) {
                assertTrue(
                        shown.getMinX() >= previous.getMaxX()
                                && shown.getMinY() < previous.getMaxY()
                                && previous.getMinY() < shown.getMaxY(),
                        "Change follows the stock on the same line");
            }
            previous = shown;
        }
    }

    public void verifyFamineForecast() {
        final Labeled forecast = getLabeledById(
                PanelColonyGood.FAMINE_FORECAST_ID);
        assertTrue(forecast.getText().contains("⚠"));
        assertTrue(forecast.getAccessibleHelp()
                .contains("Final production and stocks depend"));
        assertEquals(forecast.getAccessibleHelp(),
                forecast.getAccessibleText());
    }

    public void verifyFoodAccounting(final int produced, final int consumed) {
        final GoodsProductionStats food = colony.getGoodsStats()
                .getStatsByType(GoodsType.GRAIN);
        assertEquals(produced, food.getRowProduction());
        assertEquals(consumed, food.getConsumed());
    }

    public void reduceCarrierCargoForStaleOffer(final int unitId,
            final Goods amount) {
        final org.akropia.model.unit.UnitWithCargo carrier = (org.akropia.model.unit.UnitWithCargo) getModel()
                .getUnitById(unitId);
        carrier.getCargo().getSlotByIndex(0).removeCargo(amount);
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

    public void verifyCarrierGoods(final int unitId, final int cargoSlotIndex,
            final Goods expectedGoods) {
        final org.akropia.model.unit.UnitWithCargo carrier = (org.akropia.model.unit.UnitWithCargo) getModel()
                .getUnitById(unitId);
        assertEquals(expectedGoods,
                carrier.getCargo().getSlotByIndex(cargoSlotIndex).getGoods()
                        .orElse(Goods.of(expectedGoods.getType())));
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

    private List<Pane> getListOfGoodsImagesInWarehouse() {
        final String cssClass = "." + PanelColonyGood.IMAGE_GOODS_CLASS;
        final Set<Pane> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
        return new ArrayList<Pane>(cratesSet);
    }

    public void verifyThatProductionIs(final GoodsType goodsType,
            final Integer expectedAmount) {
        final GoodsProductionStats stats = colony.getGoodsStats()
                .getStatsByType(goodsType);
        assertEquals(expectedAmount, Integer.valueOf(stats.getNetProduction()));
    }

    /**
     * Net production the colony screen promises for the next update, to be
     * compared with what a later report states.
     */
    public int getNetProduction(final GoodsType goodsType) {
        return colony.getGoodsStats().getStatsByType(goodsType)
                .getNetProduction();
    }

    public void verifyNetProductionAtLeast(final GoodsType goodsType,
            final int expectedMinimum) {
        assertTrue(getNetProduction(goodsType) >= expectedMinimum,
                String.format("Net production of %s is below %s", goodsType,
                        expectedMinimum));
    }

    /**
     * Drag unit from pier to random empty field.
     *
     * @param indexOfUnitAtPier which unit will be dragged
     * @return net production of grain at newly occupied field
     */
    public int moveUnitFromPietToEmptyField(final int indexOfUnitAtPier) {
        final ColonyField colonyField = colony.getColonyFields().stream()
                .filter(field -> field.isEmpty()
                        && field.getGoodsTypeProduction(GoodsType.GRAIN) > 0)
                .findAny().get();
        final Pane worker = getUnitsAtPier().get(indexOfUnitAtPier);
        final Canvas fields = getNodeFinder().lookup("#colony-field-map")
                .queryAs(Canvas.class);
        revealGameContent(worker, fields);
        final Point2D target = findPlaceOfField(colonyField);
        getRobot().drag(worker, MouseButton.PRIMARY).dropTo(target)
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        return colony.getGoodsStats().getStatsByType(GoodsType.GRAIN)
                .getNetProduction();
    }

    public void returnFieldWorkerToColony(final int unitId) {
        openCropMenu(unitId);
        getRobot().clickOn("#returnToColony");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveWorkerFromPierToField(final int unitId,
            final org.akropia.model.Direction direction) {
        getRobot()
                .drag(getNodeFinder().lookup("#colonyUnit-" + unitId)
                        .queryAs(Node.class), MouseButton.PRIMARY)
                .dropTo(findPlaceOfField(
                        colony.getColonyFieldInDirection(direction)))
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyRejectedWorkerDrop(final String expectedMessage)
            throws Exception {
        DialogMessagePage.of(getContext()).verifyReady();
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(expectedMessage);
    }

    public void dragFieldWorkerTo(final int unitId,
            final org.akropia.model.Direction target)
            throws InterruptedException {
        dragFieldWorkerTo(unitId, target, false, () -> {
        });
    }

    public void cancelFieldWorkerDrag(final int unitId,
            final org.akropia.model.Direction target)
            throws InterruptedException {
        dragFieldWorkerTo(unitId, target, true, () -> {
        });
    }

    public void dragFieldWorkerAfterNeighborTakesSquare(final int unitId,
            final org.akropia.model.Direction target)
            throws InterruptedException {
        dragFieldWorkerTo(unitId, target, false, () -> {
            getModel().getUnitById(1).startTurn();
            getModel().getUnitById(1).placeToColonyField(
                    getModel().getColonyById(1).orElseThrow()
                            .getColonyFieldInDirection(
                                    org.akropia.model.Direction.west),
                    GoodsType.GRAIN);
            rememberWorld();
        });
    }

    public void verifyWorkerMovementThirds(final int unitId,
            final int expectedThirds) {
        assertEquals(expectedThirds,
                getModel().getUnitById(unitId).getMovementThirds());
    }

    public void verifyMovementHintWhileDragging(final int unitId,
            final org.akropia.model.Direction target, final boolean cancel,
            final String expectedHint) throws InterruptedException {
        dragFieldWorkerTo(unitId, target, cancel, () -> {
        }, () -> {
            verifyThatStatusBarContains(expectedHint);
            verifyWorkerDropDialogVisible(false);
        });
    }

    public void verifyWorkerDropDialogVisible(final boolean expected) {
        assertEquals(expected, !getNodeFinder()
                .lookup("#" + org.akropia.gui.util.ButtonBarOk.BUTTON_OK_ID)
                .queryAll().isEmpty());
    }

    public void verifyStatusBarDoesNotContain(final String unexpectedText) {
        final Labeled label = getLabeledById(
                org.akropia.gui.screen.game.components.StatusBarView.STATUS_BAR_LABEL_ID);
        assertFalse(label.getText().contains(unexpectedText), label.getText());
    }

    private void dragFieldWorkerTo(final int unitId,
            final org.akropia.model.Direction target, final boolean cancel,
            final Runnable beforeDrop) throws InterruptedException {
        dragFieldWorkerTo(unitId, target, cancel, beforeDrop, () -> {
        });
    }

    private void dragFieldWorkerTo(final int unitId,
            final org.akropia.model.Direction target, final boolean cancel,
            final Runnable beforeDrop, final Runnable whileHovering)
            throws InterruptedException {
        final ColonyField source = colony.getColonyFields().stream().filter(
                field -> field.getUnit().map(Unit::getId).orElse(-1) == unitId)
                .findFirst().orElseThrow();
        final javafx.scene.Node canvas = getContext()
                .getClassFromGuice(PanelColonyFields.class).getContent()
                .lookup("#colony-field-map");
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
        final EventHandler<DragEvent> change = event -> beforeDrop.run();
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
            canvas.addEventFilter(DragEvent.DRAG_DROPPED, change);
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
            getRobot().moveTo(destination);
            WaitForAsyncUtils.waitForFxEvents();
            whileHovering.run();
            if (cancel) {
                getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
            }
            getRobot().release(MouseButton.PRIMARY);
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
                canvas.removeEventFilter(DragEvent.DRAG_DROPPED, change);
            });
        }
    }

    public void verifyFieldWorker(final int expectedUnitId,
            final Location expectedLocation, final int expectedGrainYield) {
        final ColonyField field = colony.getColonyFields().stream()
                .filter(candidate -> candidate.getUnit().map(Unit::getId)
                        .orElse(-1) == expectedUnitId)
                .findFirst().orElseThrow();
        assertEquals(expectedLocation, field.getLocation());
        assertEquals(expectedGrainYield,
                field.getProduction().orElseThrow().getAmount());
        assertEquals(GoodsType.GRAIN,
                field.getProduction().orElseThrow().getType());
        assertEquals(1, colony.getColonyFields().stream()
                .filter(ColonyField::isNotEmpty).count());
    }

    private com.google.gson.JsonElement previousWorld;

    public void rememberWorld() {
        previousWorld = new com.google.gson.Gson()
                .toJsonTree(getModel().save());
    }

    public void verifyWorldUnchanged() {
        assertEquals(previousWorld,
                new com.google.gson.Gson().toJsonTree(getModel().save()));
    }

    public void openCropMenu(final int unitId) {
        final ColonyField field = colony.getColonyFields().stream()
                .filter(f -> f.getUnit().map(Unit::getId).orElse(-1) == unitId
                        || getModel().getUnitsAt(f.getLocation()).stream()
                                .anyMatch(unit -> unit.getId() == unitId
                                        && unit.getFishingColony()
                                                .orElse(null) == colony))
                .findFirst().orElseThrow();
        getRobot().clickOn(findPlaceOfField(field), MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelCropMenu() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyCropMenuShowing(final boolean expected) {
        assertEquals(expected,
                javafx.stage.Window.getWindows().stream().anyMatch(
                        w -> w instanceof javafx.scene.control.ContextMenu
                                && w.isShowing()));
    }

    public void chooseCrop(final GoodsType crop, final int displayedYield) {
        getRobot().clickOn(crop.name() + "   " + displayedYield);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveWorkerWhenCropIsConfirmed(final int unitId) {
        final javafx.scene.control.ContextMenu menu = javafx.stage.Window
                .getWindows().stream()
                .filter(w -> w instanceof javafx.scene.control.ContextMenu
                        && w.isShowing())
                .map(w -> (javafx.scene.control.ContextMenu) w).findFirst()
                .orElseThrow();
        getRobot().interact(() -> menu
                .addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    getModel().getUnitById(unitId).placeToColonyField(
                            colony.getColonyFieldInDirection(
                                    org.akropia.model.Direction.north),
                            GoodsType.GRAIN);
                    rememberWorld();
                }));
    }

    public void verifyFieldProduction(final int expectedUnitId,
            final Location expectedLocation, final GoodsType expectedCrop,
            final int expectedYield) {
        final ColonyField field = colony.getColonyFields().stream().filter(
                f -> f.getUnit().map(Unit::getId).orElse(-1) == expectedUnitId)
                .findFirst().orElseThrow();
        assertEquals(expectedLocation, field.getLocation());
        assertEquals(Goods.of(expectedCrop, expectedYield),
                field.getProduction().orElseThrow());
        assertEquals(1, colony.getColonyFields().stream()
                .filter(ColonyField::isNotEmpty).count());
    }

    private Point2D findPlaceOfField(final ColonyField colonyField) {
        final Canvas canvas = getNodeFinder().lookup("#colony-field-map")
                .queryAs(Canvas.class);
        revealGameContent(canvas);
        final Bounds boundsInScreen = canvas
                .localToScreen(canvas.getBoundsInLocal());
        final double tileWidth = boundsInScreen.getWidth() / 3;
        final Location loc = colonyField.getDirection().getVector();
        final double x = boundsInScreen.getMinX()
                + (loc.getX() + 1.5) * tileWidth;
        final double y = boundsInScreen.getMinY()
                + (loc.getY() + 1.5) * tileWidth;
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

    public void verifyFieldPreview(final org.akropia.model.Direction direction,
            final boolean blocked) {
        final ColonyField field = colony.getColonyFieldInDirection(direction);
        getRobot().clickOn(findPlaceOfField(field));
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.canvas.Canvas preview = getRobot()
                .lookup("#colony-field-preview")
                .queryAs(javafx.scene.canvas.Canvas.class);
        final java.util.concurrent.atomic.AtomicReference<javafx.scene.image.WritableImage> image = new java.util.concurrent.atomic.AtomicReference<>();
        getRobot().interact(() -> image.set(preview.snapshot(null, null)));
        boolean allGrey = true;
        final var pixels = image.get().getPixelReader();
        for (int y = 0; y < image.get().getHeight(); y++) {
            for (int x = 0; x < image.get().getWidth(); x++) {
                final javafx.scene.paint.Color color = pixels.getColor(x, y);
                allGrey &= Math
                        .abs(color.getRed()
                                - javafx.scene.paint.Color.GRAY.getRed()) < 0.01
                        && Math.abs(
                                color.getGreen() - javafx.scene.paint.Color.GRAY
                                        .getGreen()) < 0.01
                        && Math.abs(
                                color.getBlue() - javafx.scene.paint.Color.GRAY
                                        .getBlue()) < 0.01;
            }
        }
        assertEquals(blocked, allGrey);
        if (blocked) {
            assertEquals("", getRobot().lookup("#colony-field-name")
                    .queryAs(javafx.scene.control.Label.class).getText());
            assertEquals("", getRobot().lookup("#colony-field-worker")
                    .queryAs(javafx.scene.control.Label.class).getText());
            assertTrue(getRobot().lookup("#colony-field-output")
                    .queryAs(javafx.scene.layout.VBox.class).getChildren()
                    .isEmpty());
        }
    }
}
