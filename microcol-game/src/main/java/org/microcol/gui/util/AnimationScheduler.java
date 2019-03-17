package org.microcol.gui.util;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

/**
 * Schedule screen repainting. Screen is represented by Consumer.
 */
public class AnimationScheduler {

    /**
     * Default game Frame Per Second value.
     */
    private final static int FPS = 30;

    private final Logger logger = LoggerFactory.getLogger(AnimationScheduler.class);

    private final Timeline gameLoop;

    public AnimationScheduler(final GraphicsContext gc, final Consumer<GraphicsContext> frame) {
        this(FPS, gc, frame);
    }

    public AnimationScheduler(final int fps, final GraphicsContext gc,
            final Consumer<GraphicsContext> frame) {
        gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        final KeyFrame kf = new KeyFrame(Duration.seconds(1f / fps),
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent ae) {
                        frame.accept(gc);
                    }
                });
        gameLoop.getKeyFrames().add(kf);
    }

    /**
     * Pause animation scheduling.
     */
    public void pause() {
        gameLoop.pause();
        logger.debug("Animation loop was stopped");
    }

    /**
     * Start animation scheduling.
     */
    public void start() {
        gameLoop.play();
        logger.debug("Animation loop was started");
    }

}
