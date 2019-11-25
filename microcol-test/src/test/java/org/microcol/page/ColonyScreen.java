package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.gui.Tile.TILE_CENTER;
import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.microcol.gui.screen.colony.ColonyButtonsPanel;
import org.microcol.gui.screen.colony.PanelColonyFields;
import org.microcol.gui.screen.colony.PanelColonyGood;
import org.microcol.gui.screen.colony.PanelColonyName;
import org.microcol.gui.screen.colony.PanelOutsideColonyUnit;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.GoodsProductionStats;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ColonyScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(ColonyScreen.class);

    private final PageComponentPanelDock pageComponentPanelDock;

    private final Colony colony;

    public static ColonyScreen of(final TestContext context, final String expectedName) {
	return new ColonyScreen(context, expectedName);
    }

    private ColonyScreen(final TestContext context, final String expectedName) {
	super(context);
	verifyColonyName(expectedName);
	colony = getModel().getColonyByName(expectedName).get();
	pageComponentPanelDock = PageComponentPanelDock.of(context);
    }

    private void verifyColonyName(final String expectedNamePart) {
	final Labeled labeled = getLabeledById(PanelColonyName.COLONY_NAME_ID);
	logger.info("Colony name: " + labeled.getText());
	assertTrue(labeled.getText().contains(expectedNamePart),
		String.format("Text '%s' should appear in colony name '%s'", expectedNamePart, labeled.getText()));
    }

    public GamePage close() {
	final Button buttonNextTurn = getButtonById(ColonyButtonsPanel.CLOSE_BUTTON_ID);
	getRobot().clickOn(buttonNextTurn);
	return GamePage.of(getContext());
    }

    public ColonyScreen selectUnitFromPort(final int unitIndex) {
	pageComponentPanelDock.selectUnitFromPort(unitIndex);
	return this;
    }

    public void verifyNumberOfShipsInPort(final int expectedNumberOfShipsInPort) {
	pageComponentPanelDock.verifyNumberOfShipsInPort(expectedNumberOfShipsInPort);
    }

    public void verifyNumberOfUnitsAtPier(final int expectedNumberOfUnitsAtPier) {
	assertEquals(expectedNumberOfUnitsAtPier, getUnitsAtPier().size(),
		String.format("Expected numeber of units is '%s' but really there is '%s' units.",
			expectedNumberOfUnitsAtPier, getUnitsAtPier().size()));
    }

    public void dragUnitFromPierToShipCargoSlot(final int unitIndexInPier, final int cargoSlotIndex) {
	getRobot().drag(getUnitsAtPier().get(unitIndexInPier), MouseButton.PRIMARY)
		.dropTo(getListOfCrates().get(cargoSlotIndex)).release(MouseButton.PRIMARY);
    }

    public DialogChooseGoodsAmount dragGoodsFromWarehouseToShipCargoSlotWithPressedControll(
	    final int goodsIndexInWarehouse, final int cargoSlotIndex) {
	getRobot().press(KeyCode.CONTROL);
	dragGoodsFromWarehouseToShipCargoSlot(goodsIndexInWarehouse, cargoSlotIndex);
	getRobot().release(KeyCode.CONTROL);
	return DialogChooseGoodsAmount.of(getContext());
    }

    public void dragGoodsFromWarehouseToShipCargoSlot(final int goodsIndexInWarehouse, final int cargoSlotIndex) {
	getRobot().drag(getListOfGoodsImagesInWarehouse().get(goodsIndexInWarehouse), MouseButton.PRIMARY)
		.dropTo(getListOfCrates().get(cargoSlotIndex)).release(MouseButton.PRIMARY);
    }

    public DialogChooseGoodsAmount dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(final int cargoSlotIndex,
	    final int goodsIndexInWarehouse) {
	getRobot().press(KeyCode.CONTROL);
	dragGoodsFromShipCargoSlotToWarehouse(goodsIndexInWarehouse, cargoSlotIndex);
	getRobot().release(KeyCode.CONTROL);
	return DialogChooseGoodsAmount.of(getContext());
    }

    public void dragGoodsFromShipCargoSlotToWarehouse(final int cargoSlotIndex, final int goodsIndexInWarehouse) {
	getRobot().drag(getListOfCrates().get(cargoSlotIndex), MouseButton.PRIMARY)
		.dropTo(getListOfGoodsImagesInWarehouse().get(goodsIndexInWarehouse)).release(MouseButton.PRIMARY);
    }

    public ColonyScreen verifyNumberOfUnitsOnFields(final Integer expectedumberOfUnitsAtFields) {
	final Integer unitsInColony = colony.getColonyFields().stream().mapToInt(field -> field.isEmpty() ? 0 : 1)
		.sum();
	assertEquals(expectedumberOfUnitsAtFields, unitsInColony);
	return this;
    }

    /**
     * Select unit that could dragged to ship.
     * 
     * @return list panes with units
     */
    private List<Pane> getUnitsAtPier() {
	final NodeFinder nodeFinder = getNodeFinder();
	final Set<Pane> unitsSet = nodeFinder.lookup("." + PanelOutsideColonyUnit.UNIT_AT_PIER_STYLE).queryAll();
	return new ArrayList<Pane>(unitsSet);
    }

    private List<StackPane> getListOfCrates() {
	return pageComponentPanelDock.getListOfCrates();
    }

    private List<StackPane> getListOfGoodsImagesInWarehouse() {
	final String cssClass = "." + PanelColonyGood.IMAGE_GOODS_CLASS;
	final Set<StackPane> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<StackPane>(cratesSet);
    }

    public void verifyThatProductionIs(final GoodsType goodsType, final Integer expectedAmount) {
	final GoodsProductionStats stats = colony.getGoodsStats().getStatsByType(goodsType);
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
		.filter(field -> field.isEmpty() && field.getGoodsTypeProduction(GoodsType.CORN) > 0).findAny().get();
	getRobot().drag(getUnitsAtPier().get(indexOfUnitAtPier), MouseButton.PRIMARY)
		.dropTo(findPlaceOfField(colonyField)).release(MouseButton.PRIMARY);
	WaitForAsyncUtils.waitForFxEvents();
	return colony.getGoodsStats().getStatsByType(GoodsType.CORN).getNetProduction();
    }

    private Point2D findPlaceOfField(final ColonyField colonyField) {
	final PanelColonyFields panelColonyFields = getContext().getClassFromGuice(PanelColonyFields.class);
	final Bounds boundsInScreen = panelColonyFields.getContent()
		.localToScreen(panelColonyFields.getContent().getBoundsInLocal());

	final Location loc = colonyField.getDirection().getVector();
	final double x = boundsInScreen.getMinX() + (loc.getX() + 1) * TILE_WIDTH_IN_PX + TILE_CENTER.getX();
	final double y = boundsInScreen.getMinY() + (loc.getY() + 1) * TILE_WIDTH_IN_PX + TILE_CENTER.getY();
	return new Point2D(x, y);
    }

    private VBox getGoodsPanel(final int indexOfGoodsInWarehouse) {
	final List<VBox> goodsPanels = getListOfNodes(".warehouseGoods");
	return goodsPanels.get(indexOfGoodsInWarehouse);
    }

    public ColonyScreen verifyNumberOfGoodsInWrehouse(final int indexOfGoodsInWarehouse,
	    final int expectedGoodsAmount) {
	final VBox goods = getGoodsPanel(indexOfGoodsInWarehouse);
	final String amountStr = getNodeFinder().from(goods).lookup(".amount").queryLabeled().getText();
	final int amount = Integer.valueOf(amountStr);
	assertEquals(expectedGoodsAmount, amount,
		String.format("In warehouse at position %s was expected %s goods but there is %s goods",
			indexOfGoodsInWarehouse, expectedGoodsAmount, amount));
	return this;
    }

}
