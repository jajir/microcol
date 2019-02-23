package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.microcol.gui.screen.colony.ColonyButtonsPanel;
import org.microcol.gui.screen.colony.ColonyPanel;
import org.microcol.gui.screen.colony.PanelUnitWithContextMenu;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockCrate;
import org.microcol.model.Colony;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.service.finder.NodeFinder;

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
		String.format("Expected numeber of shipd is '%s' but really there is '%s' ships.",
			expectedNumberOfShipsInPort, getUnitsInPort().size()));
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
}
