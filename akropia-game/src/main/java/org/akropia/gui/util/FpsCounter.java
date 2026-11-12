package org.akropia.gui.util;

import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class helps measure Frame per second rate.
 */
public final class FpsCounter {

    private final Logger logger = LoggerFactory.getLogger(FpsCounter.class);

    private final LongSupplier nanoTime;

    private boolean running;

    private long intervalStart;

    private int fps;

    private int counter;

    public FpsCounter() {
        this(System::nanoTime);
    }

    FpsCounter(final LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    public void start() {
        counter = 0;
        fps = 0;
        intervalStart = nanoTime.getAsLong();
        running = true;
    }

    public void stop() {
        running = false;
    }

    /**
     * Should be called when screen was painted.
     */
    public void screenWasPainted() {
        if (!running) {
            return;
        }
        counter++;
        final long now = nanoTime.getAsLong();
        final long elapsed = now - intervalStart;
        if (elapsed >= 1_000_000_000L) {
            fps = (int) (counter * 1_000_000_000L / elapsed);
            counter = 0;
            intervalStart = now;
            logger.debug("Fps is: " + fps);
        }
    }

    /**
     * Return actual FPS.
     *
     * @return fps value
     */
    public int getFps() {
        return fps;
    }

}
