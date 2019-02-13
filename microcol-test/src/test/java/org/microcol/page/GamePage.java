package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.StatusBarView;
import org.microcol.gui.buttonpanel.ButtonsGamePanel;
import org.microcol.gui.colony.ColonyPanel;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.model.Location;
import org.microcol.test.AbstractMicroColTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.robot.Motion;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseButton;

public class GamePage extends AbstractScreen {

    protected final static Point TILE_CENTER = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX)
	    .divide(2);

    private final Logger logger = LoggerFactory.getLogger(AbstractMicroColTest.class);

    public static GamePage of(final TestContext context) {
	return new GamePage(context);
    }

    private GamePage(final TestContext context) {
	super(context);
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
	assertTrue(label.getText().contains(string),
		String.format("Text '%s' should appear in status bar text '%s'", string, label.getText()));
    }

    public void verifyThatTileIsVisible(final Location location) {
	assertTrue(getContext().getArea().isLocationVisible(location));
    }

    public void openColonyAt(final Location colonyLocation, final String expectedNamePart) {
	moveMouseAtLocation(colonyLocation);
	getRobot().clickOn(MouseButton.PRIMARY);
	final Labeled labeled = getLabeledById(ColonyPanel.COLONY_NAME_ID);
	logger.info("Colony name: " + labeled.getText());
	assertTrue(labeled.getText().contains(expectedNamePart),
		String.format("Text '%s' should appear in colony name '%s'", expectedNamePart, labeled.getText()));
    }

    /**
     * Wait until next turn button is available again. Unavailable next turn button
     * meant that there is some animation in progress.
     * 
     * @throws Exception
     */
    public void waitWhileMoving() throws Exception {
	WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
	    final NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
	    final Button buttonNextTurn = nodeFinder.lookup("#" + ButtonsGamePanel.BUTTON_NEXT_TURN_ID).queryButton();
	    return !buttonNextTurn.isDisabled();
	});
    }

}
