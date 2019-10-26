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
import org.microcol.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.collect.Lists;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class GamePage extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(GamePage.class);

    public static GamePage of(final TestContext context) {
	//TODO add validation that it's expected page.
	return new GamePage(context);
    }

    private GamePage(final TestContext context) {
	super(context);
	WaitForAsyncUtils.waitForFxEvents();
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

    private void verifyThatStatusBarContains(final String string) {
	final Labeled label = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID);
	logger.info("Status bar: " + label.getText());
	assertTrue(label.getText().contains(string), String.format(
		"Text '%s' should appear in status bar. But status bar contains text '%s'.", string, label.getText()));
    }

    public void verifyThatTileIsVisible(final Location location) {
	assertTrue(getContext().getArea().isLocationVisible(location));
    }

    public Optional<ScreenTurnReport> nextTurn() throws Exception {
	final Button buttonNextTurn = getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
	getRobot().clickOn(buttonNextTurn);
	waitWhileMoving();
	WaitForAsyncUtils.waitForFxEvents();
	if (isButtonOkVisible()) {
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

    /**
     * Assert that just unit at specific index is selected and other units are
     * unselected.
     * 
     * @param unitIndex required unit index
     */
    public void assertThatUnitIsSelected(final int unitIndex) {
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

}
