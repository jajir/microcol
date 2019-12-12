package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.gui.Tile.TILE_CENTER;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.Location;
import org.microcol.model.unit.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

/**
 * Represents main game page with world map.
 */
public class GamePage extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(GamePage.class);

    public static GamePage of(final TestContext context) {
	return new GamePage(context);
    }

    private GamePage(final TestContext context) {
	super(context);
	WaitForAsyncUtils.waitForFxEvents();
	verifyThatStatusBarIsVisible();
    }

    public void moveMouseAtLocation(final Location location) {
	verifyThatTileIsVisible(location);
	final Point p = getContext().getArea().convertToCanvasPoint(location)
		.add(getPrimaryStage().getX(), getPrimaryStage().getY()).add(TILE_CENTER);
	getRobot().moveTo(p.getX(), p.getY(), Motion.DEFAULT);
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    public void dragMouseAtLocation(final Location location) throws Exception {
	verifyThatTileIsVisible(location);
	final Point p = getContext().getArea().convertToCanvasPoint(location)
		.add(getPrimaryStage().getX(), getPrimaryStage().getY()).add(TILE_CENTER);
	getRobot().drag(MouseButton.PRIMARY).drag(p.getX(), p.getY()).release(MouseButton.PRIMARY);
	waitWhileMoving();
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    public void dragMouseAtLocationAndClosePossibleDialog(final Location location) throws Exception {
	dragMouseAtLocation(location);
	if (isCssIdVisible("buttonOk")) {
	    clickOnButtonWithId("buttonOk");
	}
    }

    private void verifyThatStatusBarContains(final String string) {
	final Labeled label = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID);
	logger.info("Status bar: " + label.getText());
	assertTrue(label.getText().contains(string), String.format(
		"Text '%s' should appear in status bar. But status bar contains text '%s'.", string, label.getText()));
    }

    private void verifyThatStatusBarIsVisible() {
	assertTrue(isCssIdVisible(StatusBarView.STATUS_BAR_LABEL_ID), String.format("Status bar is not visible."));
	assertTrue(isCssIdVisible(ButtonsGamePanel.BUTTON_NEXT_TURN_ID),
		String.format("Next turn button is not visible."));
    }

    public void verifyThatTileIsVisible(final Location location) {
	assertTrue(getContext().getArea().isLocationVisible(location));
    }

    public void selectTile(final Location location) {
	Preconditions.checkNotNull(location);
	moveMouseAtLocation(location);
	getRobot().clickOn(MouseButton.PRIMARY);
    }

    public Optional<ScreenTurnReport> nextTurn() throws Exception {
	final Button buttonNextTurn = getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
	getRobot().clickOn(buttonNextTurn);
	waitWhileMoving();
	WaitForAsyncUtils.waitForFxEvents();
	if (isButtonBackVisible()) {
	    return Optional.of(ScreenTurnReport.of(getContext()));
	} else {
	    return Optional.empty();
	}
    }

    public void nextTurnAndCloseDialogs() throws Exception {
	final Optional<ScreenTurnReport> oTurnEvents = nextTurn();
	if (oTurnEvents.isPresent()) {
	    final ScreenTurnReport dialog = oTurnEvents.get();
	    dialog.close();
	}
    }

    public DialogMessagePage declareIndependence() throws Exception {
	final Button buttonNextTurn = getButtonById(ButtonsGamePanel.BUTTON_DECLARE_INDEPENDENCE_ID);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return DialogMessagePage.of(getContext());
    }

    public ScreenTurnReport openTurnReport() throws Exception {
	final Button buttonNextTurn = getButtonById(ButtonsGamePanel.BUTTON_TURN_REPORT_ID);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return ScreenTurnReport.of(getContext());
    }

    public ColonyScreen openColonyAt(final Location colonyLocation, final String expectedNamePart) {
	moveMouseAtLocation(colonyLocation);
	getRobot().clickOn(MouseButton.PRIMARY);
	return ColonyScreen.of(getContext(), expectedNamePart);
    }

    public GamePage verifyThatItsNotPossibleToOpenColonyAt(final Location colonyLocation) {
	moveMouseAtLocation(colonyLocation);
	getRobot().clickOn(MouseButton.PRIMARY);
	verifyThatStatusBarContains(String.valueOf(colonyLocation.getX()));
	verifyThatStatusBarContains(String.valueOf(colonyLocation.getY()));
	return this;
    }

    public EuropePortScreen openEuroperPort() {
	final Button buttonNextTurn = getButtonById(ButtonsGamePanel.BUTTON_EUROPE_ID);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return EuropePortScreen.of(getContext());
    }

    public EuropePortScreen openEuroperPortByPressingKey() {
	getRobot().press(KeyCode.E).sleep(200, TimeUnit.MILLISECONDS).release(KeyCode.E);
	WaitForAsyncUtils.waitForFxEvents();
	return EuropePortScreen.of(getContext());
    }

    /**
     * Wait until next turn button is available again. Unavailable next turn button
     * meant that there is some animation in progress.
     * 
     * @throws Exception
     */
    public void waitWhileMoving() throws TimeoutException {
	final String id = ButtonsGamePanel.BUTTON_NEXT_TURN_ID;
	WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> {
	    // button could be hidden, because turn report page could be shown.
	    if (isCssIdVisible(id)) {
		final Button buttonNextTurn = getButtonById(id);
		return !buttonNextTurn.isDisabled();
	    } else {
		// immediately stop waiting.
		return true;
	    }
	});
    }

    public void verifyNumberOfUnitInRightPanel(int expectedUnitsInRightPanel) {
	final List<VBox> set = getListOfUnitsInRightPanel();
	assertNotNull(set);
	assertEquals(expectedUnitsInRightPanel, set.size(),
		String.format("Expected number of unit in right panel is '%s' but there are '%s'",
			expectedUnitsInRightPanel, set.size()));
    }

    public void pressTab() {
	getRobot().push(KeyCode.TAB);
    }

    public void pressP() {
	getRobot().push(KeyCode.P);
    }

    /**
     * Assert that just unit at specific index is selected and other units are
     * unselected.
     * 
     * @param unitIndex required unit index
     */
    public void verifyThatUnitIsSelected(final int unitIndex) {
	final int numberOfUnitsInRightPanel = getListOfUnitsInRightPanel().size();
	for (int i = 0; i < numberOfUnitsInRightPanel; i++) {
	    final RightPanelUnit rightPanelUnit = getRightPanelUnit(i);
	    if (i == unitIndex) {
		assertTrue(rightPanelUnit.isSelected());
	    } else {
		assertFalse(rightPanelUnit.isSelected());
	    }
	}
    }

    public void verifyThatSelectedTileIs(final Location expectedSelectedTile) {
	final SelectedTileManager selectedTileManager = getContext().getClassFromGuice(SelectedTileManager.class);
	Preconditions.checkNotNull(selectedTileManager);

	assertTrue(selectedTileManager.getSelectedTile().isPresent(), "No tile is selected");
	assertEquals(expectedSelectedTile, selectedTileManager.getSelectedTile().get(),
		String.format("Tile at %s should be selected but tile %s is really selected.", expectedSelectedTile,
			selectedTileManager.getSelectedTile().get()));
    }

    public void verifyThatThereIsNoColonyAt(final Location colonyLocation) {
	assertTrue(getModel().getColonyAt(colonyLocation).isEmpty(),
		String.format("At %s should not be any colony.", colonyLocation));
    }

    public RightPanelUnit getRightPanelUnit(final int unitIndexInRightPanel) {
	final VBox unitBox = getListOfUnitsInRightPanel().get(unitIndexInRightPanel);
	return RightPanelUnit.of(getContext(), unitBox);
    }

    private List<VBox> getListOfUnitsInRightPanel() {
	return Lists.newArrayList(getNodeFinder().lookup(".unitPanel").queryAllAs(VBox.class));
    }

    public void verifyThatBuildColonyButtonInHidden() {
	assertFalse(getNodeFinder().lookup("#buildColony").tryQuery().isPresent());
    }

    public ShipWrapper getShipAt(final Location location) {
	assertFalse(getModel().getUnitsAt(location).isEmpty(),
		String.format("There should be at leat on ship at %s.", location));
	return new ShipWrapper((Ship) getModel().getUnitsAt(location).get(0));
    }

}
