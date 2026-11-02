package org.akropia.gui.screen.game.gamepanel;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.ScreenLifeCycle;
import org.akropia.gui.screen.game.components.WorldMinimap;
import org.akropia.gui.util.AnimationScheduler;
import org.akropia.gui.util.CanvasComponent;
import org.akropia.gui.util.FpsCounter;
import org.akropia.gui.util.JavaFxComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;

/**
 * Main game panel component with canvas. Allows to start stop animations.
 */
@Singleton
public final class GamePanelComponent
        implements ScreenLifeCycle, JavaFxComponent {

    private final Logger logger = LoggerFactory
            .getLogger(GamePanelComponent.class);

    /** Distance of a map control from the map's corner, in pixels. */
    private static final double MAP_CONTROL_MARGIN = 10;

    private final CanvasComponent canvasComponent;

    private final GameModelController gameModelController;

    private final FpsCounter fpsCounter;

    private final WorldMinimap minimap;

    private final VisibleAreaService visibleAreaService;

    private final GamePanelPainter gamePanelPainter;

    private final AnimationScheduler animationScheduler;

    @Inject
    public GamePanelComponent(final GameModelController gameModelController,
            final @Named("game") VisibleAreaService visibleAreaService,
            final CanvasComponent paneCanvas,
            final GamePanelPainter gamePanelPainter,
            final WorldMinimap minimap) {
        this.minimap = minimap;
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.visibleAreaService = Preconditions
                .checkNotNull(visibleAreaService);
        this.canvasComponent = Preconditions.checkNotNull(paneCanvas);
        this.gamePanelPainter = Preconditions.checkNotNull(gamePanelPainter);

        canvasComponent.widthProperty()
                .addListener((obj, oldValue, newValue) -> {
                    if (newValue
                            .intValue() < VisibleAreaService.MAX_CANVAS_SIDE_LENGTH) {
                        visibleAreaService.setCanvasWidth(newValue.intValue());
                    }
                });
        canvasComponent.heightProperty()
                .addListener((obj, oldValue, newValue) -> {
                    if (newValue
                            .intValue() < VisibleAreaService.MAX_CANVAS_SIDE_LENGTH) {
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
     * Paint one animation frame.
     *
     * @param tick number of elapsed animation ticks
     */
    private void paintFrame(final Long tick) {
        if (gameModelController.isGameModelReady()) {
            logger.debug("painting: " + visibleAreaService);
            gamePanelPainter.paint(canvasComponent.getGraphicsContext2D(),
                    tick);
            minimap.paint();
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

    /**
     * Places a control over the map's upper-left corner. It takes its own
     * clicks; the rest of the map still reaches the canvas.
     *
     * @param control required control
     */
    public void addMapControl(final Node control) {
        control.relocate(MAP_CONTROL_MARGIN, MAP_CONTROL_MARGIN);
        canvasComponent.getCanvasPane().getChildren().add(control);
    }

}
