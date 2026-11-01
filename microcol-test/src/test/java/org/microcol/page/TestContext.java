package org.microcol.page;

import org.microcol.MicroColApplication;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.GamePanelPainter;
import org.microcol.model.Model;
import org.testfx.api.FxRobot;

import com.google.common.base.Preconditions;

import javafx.stage.Stage;

/**
 * This class contains reference to MicroCol game in JVM. Object exists just
 * during executing one test.
 */
public class TestContext {

    private final MicroColApplication microCol;

    private final Stage primaryStage;

    private FxRobot robot;

    public TestContext(final MicroColApplication microCol,
            final Stage primaryStage) {
        this.microCol = Preconditions.checkNotNull(microCol);
        this.primaryStage = Preconditions.checkNotNull(primaryStage);
    }

    public MicroColApplication getMicroCol() {
        return microCol;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * The stubbed file chooser of the test. The game uses a chooser that
     * forwards to it and passes chosen authored scenarios as player saves.
     */
    private FileSelectingService stubbedChooser;

    /**
     * @param stubbed optional chooser that scenarios and fixtures stub
     */
    public void useStubbedChooser(final FileSelectingService stubbed) {
        stubbedChooser = stubbed;
    }

    public <T> T getClassFromGuice(final Class<T> clazz) {
        if (clazz == FileSelectingService.class && stubbedChooser != null) {
            // Fixtures stub and verify the chooser behind the game's one.
            return clazz.cast(stubbedChooser);
        }
        final T t = getMicroCol().getInjector().getInstance(clazz);
        return Preconditions.checkNotNull(t);
    }

    public Model getModel() {
        final GameModelController gameModelController = getClassFromGuice(
                GameModelController.class);
        return gameModelController.getModel();
    }

    public Area getArea() {
        final GamePanelPainter gamePanelView = getClassFromGuice(
                GamePanelPainter.class);
        return gamePanelView.getArea();
    }

    public FxRobot getRobot() {
        return robot;
    }

    public void setRobot(FxRobot robot) {
        this.robot = Preconditions.checkNotNull(robot);
    }

}
