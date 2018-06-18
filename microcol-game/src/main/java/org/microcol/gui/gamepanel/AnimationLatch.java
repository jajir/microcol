package org.microcol.gui.gamepanel;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Allows to stop waiting threads until some operation is done.
 */
public class AnimationLatch {

    private final Logger logger = LoggerFactory.getLogger(AnimationLatch.class);

    private CountDownLatch latch;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("latch", latch).toString();
    }

    /**
     * Could be called from any thread. Just first call make it lock other calls
     * even from different threads doesn't have any effect.
     */
    void lock() {
        if (latch == null) {
            latch = new CountDownLatch(1);
        }
    }

    /**
     * Release lock and all waiting processes could continue.
     */
    void unlock() {
        if (latch != null) {
            latch.countDown();
            latch = null;
        }
    }

    /**
     * Any thread that come to this method will be stopped until lock is
     * released.
     */
    void waitForUnlock() {
        if (latch != null) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
