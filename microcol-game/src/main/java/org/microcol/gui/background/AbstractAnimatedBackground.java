package org.microcol.gui.background;

import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.util.AnimationScheduler;

import javafx.scene.canvas.GraphicsContext;

/**
 * When this background is used and started than method
 * {@link #paint(GraphicsContext)} is periodically called. In this method
 * animation step should be painted.
 */
public abstract class AbstractAnimatedBackground extends AbstractBackground
        implements ScreenLifeCycle {

    private final AnimationScheduler animationScheduler;

    public AbstractAnimatedBackground() {
        super();
        final GraphicsContext gc = getCanvas().getGraphicsContext2D();
        animationScheduler = new AnimationScheduler(gc, gcontex -> paint(gcontex));
    }

    /**
     * Start animation timer.
     */
    @Override
    public void beforeShow() {
        animationScheduler.start();
    }

    /**
     * Stop animation timer.
     */
    @Override
    public void beforeHide() {
        animationScheduler.pause();
    }
}
