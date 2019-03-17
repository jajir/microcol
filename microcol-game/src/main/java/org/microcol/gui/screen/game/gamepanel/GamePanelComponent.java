package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.util.AnimationScheduler;
import org.microcol.gui.util.CanvasComponent;
import org.microcol.gui.util.FpsCounter;
import org.microcol.gui.util.JavaFxComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

/**
 * Main game panel component with canvas. Allows to start stop animations.
 */
@Singleton
public final class GamePanelComponent implements ScreenLifeCycle, JavaFxComponent {

    private final Logger logger = LoggerFactory.getLogger(GamePanelComponent.class);

    private final CanvasComponent canvasComponent;

    private final GameModelController gameModelController;

    private final FpsCounter fpsCounter;

    private final VisibleArea visibleArea;

    private final GamePanelPainter gamePanelPainter;

    private final AnimationScheduler animationScheduler;

    @Inject
    public GamePanelComponent(final GameModelController gameModelController,
            final VisibleArea visibleArea, final CanvasComponent paneCanvas,
            final GamePanelPainter gamePanelPainter) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.canvasComponent = Preconditions.checkNotNull(paneCanvas);
        this.gamePanelPainter = Preconditions.checkNotNull(gamePanelPainter);

        canvasComponent.widthProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < VisibleArea.MAX_CANVAS_SIDE_LENGTH) {
                visibleArea.setCanvasWidth(newValue.intValue());
            }
        });
        canvasComponent.heightProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < VisibleArea.MAX_CANVAS_SIDE_LENGTH) {
                visibleArea.setCanvasHeight(newValue.intValue());
            }
        });

        fpsCounter = new FpsCounter();
        fpsCounter.start();

        /**
         * Following class main define animation loop.
         */
        final GraphicsContext gc = canvasComponent.getCanvas().getGraphicsContext2D();
        animationScheduler = new AnimationScheduler(gc, gcontex -> paintFrame(gcontex));
    }

    /**
     * Smallest game time interval. In ideal case it have time to draw world on
     * screen.
     */
    private void paintFrame(final GraphicsContext gc) {
        if (gameModelController.isModelReady()) {
            logger.debug("painting: " + visibleArea);
            gamePanelPainter.paint(gc);
        }
    }

    @Override
    public void beforeShow() {
        animationScheduler.start();
        fpsCounter.start();
    }

    @Override
    public void beforeHide() {
        animationScheduler.pause();
        fpsCounter.stop();
    }

    @Override
    public Region getContent() {
        return canvasComponent.getContent();
    }

    public Canvas getCanvas() {
        return canvasComponent.getCanvas();
    }

}
