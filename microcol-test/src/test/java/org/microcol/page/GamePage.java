package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
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

    protected final static Point TILE_CENTER = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX)
	    .divide(2);

    private final Logger logger = LoggerFactory.getLogger(GamePage.class);

    public static GamePage of(final TestContext context) {
	return new GamePage(context);
    }

    private GamePage(final TestContext context) {
	super(context);
	WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveMouseAtLocation(final Location location) {
	verifyThatTileIsVisible(location);
	final Point p = getContext().getArea().convertToPoint(location)
		.add(getPrimaryStage().getX(), getPrimaryStage().getY()).add(TILE_CENTER);
	getRobot().moveTo(p.getX(), p.getY(), Motion.DEFAULT);
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    public void dragMouseAtLocation(final Location location) throws Exception {
	verifyThatTileIsVisible(location);
	final Point p = getContext().getArea().convertToPoint(location)
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

    public void nextTurn() throws Exception {
	final Button buttonNextTurn = getButtoonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
	getRobot().clickOn(buttonNextTurn);
	waitWhileMoving();
    }

    public DialogMessagePage declareIndependence() throws Exception {
	final Button buttonNextTurn = getButtoonById(ButtonsGamePanel.BUTTON_DECLARE_INDEPENDENCE_ID);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return DialogMessagePage.of(getContext());
    }

    public DialogTurnReport openTurnReport() throws Exception {
	final Button buttonNextTurn = getButtoonById(ButtonsGamePanel.BUTTON_TURN_REPORT_ID);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return DialogTurnReport.of(getContext());
    }

    public ColonyScreen openColonyAt(final Location colonyLocation, final String expectedNamePart) throws Exception {
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
	final Button buttonNextTurn = getButtoonById(ButtonsGamePanel.BUTTON_EUROPE_ID);
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
	WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> {
	    final Button buttonNextTurn = getNodeFinder().lookup("#" + ButtonsGamePanel.BUTTON_NEXT_TURN_ID)
		    .queryButton();
	    return !buttonNextTurn.isDisabled();
	});
    }

    public void verifyNumberOfUnitInRightPanel(int expectedUnitsInRightPanel) {
	final List<VBox> set = getListOfUnitsInRightPanel();
	assertNotNull(set);
	assertEquals(expectedUnitsInRightPanel, set.size(),
		String.format("Expected number of unit in right panel is '%s' but there are '%s'",
			expectedUnitsInRightPanel, set.size()));
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
