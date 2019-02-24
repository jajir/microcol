package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.microcol.gui.screen.colony.ColonyButtonsPanel;
import org.microcol.gui.screen.colony.ColonyPanel;
import org.microcol.gui.screen.colony.PanelColonyFields;
import org.microcol.gui.screen.colony.PanelUnitWithContextMenu;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockCrate;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.GoodProductionStats;
import org.microcol.model.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ColonyScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(ColonyScreen.class);

    private final Colony colony;

    public static ColonyScreen of(final TestContext context, final String expectedName) {
	return new ColonyScreen(context, expectedName);
    }

    private ColonyScreen(final TestContext context, final String expectedName) {
	super(context);
	verifyColonyName(expectedName);
	colony = getModel().getColonyByName(expectedName).get();
    }

    private void verifyColonyName(final String expectedNamePart) {
	final Labeled labeled = getLabeledById(ColonyPanel.COLONY_NAME_ID);
	logger.info("Colony name: " + labeled.getText());
	assertTrue(labeled.getText().contains(expectedNamePart),
		String.format("Text '%s' should appear in colony name '%s'", expectedNamePart, labeled.getText()));
    }

    public GamePage buttonCloseClick() {
	final Button buttonNextTurn = getButtoonById(ColonyButtonsPanel.CLOSE_BUTTON_ID);
	getRobot().clickOn(buttonNextTurn);
	return GamePage.of(getContext());
    }

    public ColonyScreen selectUnitFromPort(final int unitIndex) {
	getRobot().clickOn(getUnitsInPort().get(unitIndex));
	return this;
    }

    protected List<ToggleButton> getUnitsInPort() {
	final String cssClass = "." + PanelDock.SHIP_IN_PORT_STYLE;
	final Set<ToggleButton> ships = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<ToggleButton>(ships);
    }

    public void verifyNumberOfShipsInPort(final int expectedNumberOfShipsInPort) {
	assertEquals(expectedNumberOfShipsInPort, getUnitsInPort().size(),
		String.format("Expected numeber of ships is '%s' but really there is '%s' ships.",
			expectedNumberOfShipsInPort, getUnitsInPort().size()));
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
	final Set<Pane> unitsSet = nodeFinder.lookup("." + PanelUnitWithContextMenu.UNIT_AT_PIER_STYLE).queryAll();
	return new ArrayList<Pane>(unitsSet);
    }

    private List<StackPane> getListOfCrates() {
	final String cssClass = "." + PanelDockCrate.CRATE_CLASS;
	final Set<StackPane> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<StackPane>(cratesSet);
    }

    public void verifyThatProductionIs(final GoodType goodType, final Integer expectedAmount) {
	final GoodProductionStats stats = colony.getGoodsStats().getStatsByType(goodType);
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
		.filter(field -> field.isEmpty() && field.getGoodTypeProduction(GoodType.CORN) > 0).findAny().get();
	getRobot().drag(getUnitsAtPier().get(indexOfUnitAtPier), MouseButton.PRIMARY)
		.dropTo(findPlaceOfField(colonyField)).release(MouseButton.PRIMARY);
	WaitForAsyncUtils.waitForFxEvents();
	return colony.getGoodsStats().getStatsByType(GoodType.CORN).getNetProduction();
    }

    private Point2D findPlaceOfField(final ColonyField colonyField) {
	final PanelColonyFields panelColonyFields = getContext().getClassFromGuice(PanelColonyFields.class);
	final Bounds boundsInScreen = panelColonyFields.getContent()
		.localToScreen(panelColonyFields.getContent().getBoundsInLocal());

	final double x = boundsInScreen.getMinX()
		+ (colonyField.getDirection().getX() + 1) * GamePanelView.TILE_WIDTH_IN_PX + TILE_CENTER.getX();
	final double y = boundsInScreen.getMinY()
		+ (colonyField.getDirection().getY() + 1) * GamePanelView.TILE_WIDTH_IN_PX + TILE_CENTER.getY();
	return new Point2D(x, y);
    }

}
