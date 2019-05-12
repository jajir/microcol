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
import com.google.inject.name.Named;

import javafx.scene.canvas.Canvas;
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

    private final VisibleAreaService visibleAreaService;

    private final GamePanelPainter gamePanelPainter;

    private final AnimationScheduler animationScheduler;

    @Inject
    public GamePanelComponent(final GameModelController gameModelController,
            final @Named("game") VisibleAreaService visibleAreaService,
            final CanvasComponent paneCanvas, final GamePanelPainter gamePanelPainter) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.visibleAreaService = Preconditions.checkNotNull(visibleAreaService);
        this.canvasComponent = Preconditions.checkNotNull(paneCanvas);
        this.gamePanelPainter = Preconditions.checkNotNull(gamePanelPainter);

        canvasComponent.widthProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < VisibleAreaService.MAX_CANVAS_SIDE_LENGTH) {
                visibleAreaService.setCanvasWidth(newValue.intValue());
            }
        });
        canvasComponent.heightProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < VisibleAreaService.MAX_CANVAS_SIDE_LENGTH) {
                visibleAreaService.setCanvasHeight(newValue.intValue());
            }
        });

        fpsCounter = new FpsCounter();
        fpsCounter.start();

        /**
         * Following class main define animation loop.
         */
        animationScheduler = new AnimationScheduler(this::paintFrame);
    }

    /**
     * Smallest game time interval. In ideal case it have time to draw world on
     * screen.
     */
    private void paintFrame(final Long tick) {
        if (gameModelController.isGameModelReady()) {
            logger.debug("painting: " + visibleAreaService);
            gamePanelPainter.paint(canvasComponent.getGraphicsContext2D(), tick);
            fpsCounter.screenWasPainted();
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
