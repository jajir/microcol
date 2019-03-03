package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.microcol.MicroCol;
import org.microcol.gui.Point;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.model.Model;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.stage.Stage;

public abstract class AbstractScreen {
    
    protected final static Point TILE_CENTER = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX)
	    .divide(2);

    private final TestContext context;

    AbstractScreen(final TestContext context) {
	this.context = Preconditions.checkNotNull(context);
    }

    protected TestContext getContext() {
	return context;
    }

    protected MicroCol getMicroCol() {
	return context.getMicroCol();
    }

    protected Stage getPrimaryStage() {
	return context.getPrimaryStage();
    }

    protected FxRobot getRobot() {
	return context.getRobot();
    }

    protected Model getModel() {
	return context.getModel();
    }

    protected NodeFinder getNodeFinder() {
	return FxAssert.assertContext().getNodeFinder();
    }

    protected void clickOnButtonWithId(final String buttonId) {
	final String id = "#" + buttonId;
	getRobot().clickOn(id);
	WaitForAsyncUtils.waitForFxEvents();
    }

    protected Labeled getLabeledById(final String cssId) {
	final String id = "#" + cssId;
	final Labeled label = getNodeFinder().lookup(id).queryLabeled();
	assertNotNull(label, String.format("unable to find labeled by id '%s'", cssId));
	return label;
    }

    protected Button getButtoonById(final String cssId) {
	final String id = "#" + cssId;
	final Button label = getNodeFinder().lookup(id).queryButton();
	assertNotNull(label, String.format("unable to find button by id '%s'", cssId));
	return label;
    }
    
    protected <T extends Node> List<T> getListOfNodes(final String cssClass) {
	final Set<T> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<T>(cratesSet);
    }

}
