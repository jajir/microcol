package org.microcol.test;

import org.junit.jupiter.api.BeforeEach;
import org.microcol.MicroCol;
import org.microcol.gui.Point;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.gui.util.GamePreferences;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Model;
import org.microcol.page.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;

import com.google.inject.Binder;

import javafx.stage.Stage;

public abstract class AbstractMicroColTest {

    protected final static Point TILE_CENTER = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX)
	    .divide(2);

    private final Logger logger = LoggerFactory.getLogger(AbstractMicroColTest.class);

    private TestContext context;

    @BeforeEach
    void pred(final FxRobot robot) {
	getContext().setRobot(robot);
    }

    /**
     * Method that initialize MicroCol game.
     * 
     * @param primaryStage required primary stage
     * @throws Exception
     */
    protected void initialize(final Stage primaryStage, final Class<?> clazz) throws Exception {
	logger.info("Starting MicroCol UI test " + clazz.getName());
	System.setProperty(GamePreferences.SYSTEM_PROPERTY_DEVELOPMENT, Boolean.TRUE.toString());
	final MicroCol microCol = new MicroCol(binder -> {
	    binder.bind(CursorService.class).toInstance(new CursorServiceNoOpp());
	    bind(binder);
	});
	microCol.start(primaryStage);
	context = new TestContext(microCol, primaryStage);
    }

    /**
     * Allows to custom guice in every test case.
     *
     * @param binder required guice binder
     */
    abstract protected void bind(final Binder binder);

    protected MicroCol getMicroCol() {
	return context.getMicroCol();
    }

    protected Stage getPrimaryStage() {
	return context.getPrimaryStage();
    }

    protected TestContext getContext() {
	return context;
    }

    protected NodeFinder getNodeFinder() {
	return FxAssert.assertContext().getNodeFinder();
    }

    protected Model getModel() {
	return getContext().getModel();
    }

}
