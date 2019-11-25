package org.microcol.gui.util;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * Schedule screen repainting. Screen is represented by Consumer.
 */
public class AnimationScheduler {

    private final Logger logger = LoggerFactory.getLogger(AnimationScheduler.class);

    /**
     * Default game Frame Per Second value.
     */
    public final static int FPS = 30;

    /**
     * Source of game ticks.
     */
    private final Timeline gameLoop;

    /**
     * Every game tick increase this number. It allows plan animation event base
     * on game ticks and not based on time.
     */
    private long tick = 0;

    public AnimationScheduler(final Consumer<Long> frame) {
        this(FPS, frame);
    }

    public AnimationScheduler(final int fps, final Consumer<Long> frame) {
        gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        final KeyFrame kf = new KeyFrame(Duration.seconds(1f / fps),
                new EventHandler<ActionEvent>() {
                    public void handle(final ActionEvent ae) {
                        frame.accept(tick);
                        tick++;
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

    public long getTick() {
        return tick;
    }

}
