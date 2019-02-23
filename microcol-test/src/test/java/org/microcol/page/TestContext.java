package org.microcol.page;

import org.microcol.MicroCol;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.model.Model;
import org.testfx.api.FxRobot;

import com.google.common.base.Preconditions;

import javafx.stage.Stage;

/**
 * This class contains reference to MicroCol game in JVM. Object exists just
 * during executing one test.
 */
public class TestContext {

    private final MicroCol microCol;

    private final Stage primaryStage;
    
    private FxRobot robot; 

    public TestContext(final MicroCol microCol, final Stage primaryStage) {
	this.microCol = Preconditions.checkNotNull(microCol);
	this.primaryStage = Preconditions.checkNotNull(primaryStage);
    }

    public MicroCol getMicroCol() {
        return microCol;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public <T> T getClassFromGuice(final Class<T> clazz) {
	final T t = getMicroCol().getInjector().getInstance(clazz);
	return Preconditions.checkNotNull(t);
    }

    public Model getModel() {
	final GameModelController gameModelController = getClassFromGuice(GameModelController.class);
	return gameModelController.getModel();
    }

    public Area getArea() {
	final GamePanelView gamePanelView = getClassFromGuice(GamePanelView.class);
	return gamePanelView.getArea();
    }

    public FxRobot getRobot() {
        return robot;
    }

    public void setRobot(FxRobot robot) {
        this.robot = Preconditions.checkNotNull(robot);
    }
    
}
