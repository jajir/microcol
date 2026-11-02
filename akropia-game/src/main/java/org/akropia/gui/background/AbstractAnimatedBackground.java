package org.akropia.gui.background;

import org.akropia.gui.screen.ScreenLifeCycle;
import org.akropia.gui.util.AnimationScheduler;

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
        animationScheduler = new AnimationScheduler(gcontex -> repaint());
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
