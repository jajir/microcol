package org.microcol.gui.screen.editor;

import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.screen.game.gamepanel.GamePanelComponent;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.gui.util.AnimationScheduler;
import org.microcol.gui.util.CanvasComponent;
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
 * Panel with canvas containing edited game model.
 */
@Singleton
class EditorPanel implements ScreenLifeCycle, JavaFxComponent {

    private final Logger logger = LoggerFactory.getLogger(GamePanelComponent.class);

    private final CanvasComponent canvasComponent = new CanvasComponent();

    private final EditorPaintingService editorPaintingService;

    private final AnimationScheduler animationScheduler;

    @Inject
    EditorPanel(final @Named("editor") VisibleAreaService visibleAreaService,
            final EditorPaintingService editorPaintingService) {
        this.editorPaintingService = Preconditions.checkNotNull(editorPaintingService);

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

        /**
         * Following class main define animation loop.
         */
        animationScheduler = new AnimationScheduler(this::paintFrame);
    }

    /**
     * Smallest game time interval. In ideal case it have time to draw world on
     * screen.
     */
    @SuppressWarnings("unused")
    private void paintFrame(final Long tick) {
        editorPaintingService.paint(canvasComponent.getGraphicsContext2D());
        logger.debug("painting editor");
    }

    @Override
    public Region getContent() {
        return canvasComponent.getContent();
    }

    @Override
    public void beforeShow() {
        animationScheduler.start();

    }

    @Override
    public void beforeHide() {
        animationScheduler.pause();
    }

    Canvas getCanvas() {
        return canvasComponent.getCanvas();
    }

}
