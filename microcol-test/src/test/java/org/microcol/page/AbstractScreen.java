package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.microcol.MicroCol;
import org.microcol.model.Model;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.stage.Stage;

public abstract class AbstractScreen {

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

}
