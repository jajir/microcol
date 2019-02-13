package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.Point;
import org.microcol.gui.StatusBarView;
import org.microcol.gui.buttonpanel.ButtonsGamePanel;
import org.microcol.gui.colony.ColonyButtonsPanel;
import org.microcol.gui.colony.PanelUnitWithContextMenu;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockCrate;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import com.google.inject.Binder;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_01_embark_disembark_test extends AbstractMicroColTest {

    private final Logger logger = LoggerFactory.getLogger(TC_01_embark_disembark_test.class);

    private final static File verifyLoadingUnloading = new File(
	    "src/test/scenarios/test-verify-loading-unloading.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage);
    }

    @Override
    protected void bind(Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(verifyLoadingUnloading);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
    }

    @Test
    void TC_01_embark_disembark(final FxRobot robot) throws Exception {
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	// go at main game panel
	gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Verify that there is just one ship in port
	assertEquals(1, getUnitsInPort().size(), String
		.format("Expected numebr of shipd is '1' but really there is '%s' ships.", getUnitsInPort().size()));

	// Select first ship to drag to ship.
	robot.clickOn(getUnitsInPort().get(0));

	// drag first units to crate 0
	robot.drag(getUnitsAtPier().get(0), MouseButton.PRIMARY).dropTo(getListOfCrates().get(0))
		.release(MouseButton.PRIMARY);

	// drag second units to crate 1
	robot.drag(getUnitsAtPier().get(0), MouseButton.PRIMARY).dropTo(getListOfCrates().get(1))
		.release(MouseButton.PRIMARY);

	// return back to main game screen.
	buttonCloseClick(robot);

	// move ship few tiles to the right
	moveMouseAtLocation(robot, Location.of(22, 12));
	dragMouseAtLocation(robot, Location.of(24, 11));
	waitWhileMoving();

	// press next turn.
	buttonNextTurnClick(robot);

	// verify that there are no ship
	assertEquals(0, getContext().getModel().getUnitsAt(Location.of(24, 12)).size());

	// disembark units
	moveMouseAtLocation(robot, Location.of(24, 11));
	dragMouseAtLocation(robot, Location.of(24, 12));
	waitWhileMoving();

	// verify that units are at expected location.
	assertEquals(2, getContext().getModel().getUnitsAt(Location.of(24, 12)).size());
    }

    private List<ToggleButton> getUnitsInPort() {
	final String cssClass = "." + PanelDock.SHIP_IN_PORT_STYLE;
	final Set<ToggleButton> ships = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<ToggleButton>(ships);
    }

    private List<StackPane> getListOfCrates() {
	final String cssClass = "." + PanelDockCrate.CRATE_CLASS;
	final Set<StackPane> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<StackPane>(cratesSet);
    }

    /**
     * Wait until next turn button is available again. Unavailable next turn button
     * meant that there is some animation in progress.
     * 
     * @throws Exception
     */
    private void waitWhileMoving() throws Exception {
	WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
	    final NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
	    final Button buttonNextTurn = nodeFinder.lookup("#" + ButtonsGamePanel.BUTTON_NEXT_TURN_ID).queryButton();
	    return !buttonNextTurn.isDisabled();
	});
    }

    private void buttonCloseClick(final FxRobot robot) {
	final Button buttonNextTurn = getButtoonById(ColonyButtonsPanel.CLOSE_BUTTON_ID);
	robot.clickOn(buttonNextTurn);
    }

    private void buttonNextTurnClick(final FxRobot robot) {
	final Button buttonNextTurn = getButtoonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
	robot.clickOn(buttonNextTurn);
    }

    // Select units to drag to ship.
    private List<Pane> getUnitsAtPier() {
	final NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
	final Set<Pane> unitsSet = nodeFinder.lookup("." + PanelUnitWithContextMenu.UNIT_AT_PIER_STYLE).queryAll();
	return new ArrayList<Pane>(unitsSet);
    }

    private void moveMouseAtLocation(final FxRobot robot, final Location location) {
	verifyThatTileIsVisible(location);
	final Point p = getContext().getArea().convertToPoint(location)
		.add(getPrimaryStage().getX(), getPrimaryStage().getY()).add(TILE_CENTER);
	robot.moveTo(p.getX(), p.getY(), Motion.DEFAULT);
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    private void dragMouseAtLocation(final FxRobot robot, final Location location) throws Exception {
	verifyThatTileIsVisible(location);
	final Point p = getContext().getArea().convertToPoint(location)
		.add(getPrimaryStage().getX(), getPrimaryStage().getY()).add(TILE_CENTER);
	robot.drag(MouseButton.PRIMARY).drag(p.getX(), p.getY()).release(MouseButton.PRIMARY);
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    private void verifyThatStatusBarContains(final String string) {
	final Labeled label = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID);
	logger.info("Status bar: " + label.getText());
	assertTrue(label.getText().contains(string),
		String.format("Text '%s' should appear in status bar text '%s'", string, label.getText()));
    }

    private void verifyThatTileIsVisible(final Location location) {
	assertTrue(getContext().getArea().isLocationVisible(location));
    }

    private Labeled getLabeledById(final String cssId) {
	final String id = "#" + cssId;
	final Labeled label = getNodeFinder().lookup(id).queryLabeled();
	assertNotNull(label, String.format("unable to find labeled by id '%s'", cssId));
	return label;
    }

    private Button getButtoonById(final String cssId) {
	final String id = "#" + cssId;
	final Button label = getNodeFinder().lookup(id).queryButton();
	assertNotNull(label, String.format("unable to find button by id '%s'", cssId));
	return label;
    }

}
