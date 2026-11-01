package org.microcol.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.SuccessCuePlayer;

/**
 * Success cue player that plays nothing and only counts the cues the game
 * requested (R-0228). It can stand in for an unavailable audio device, which
 * fails every request.
 */
public class RecordingSuccessCuePlayer implements SuccessCuePlayer {

    private final AtomicInteger requests = new AtomicInteger();

    private volatile boolean deviceUnavailable;

    @Override
    public void play() {
        requests.incrementAndGet();
        if (deviceUnavailable) {
            throw new IllegalStateException("No audio device in this test.");
        }
    }

    @Override
    public void stop() {
        // nothing is playing
    }

    /**
     * Make every later cue fail like a missing audio device.
     */
    public void makeDeviceUnavailable() {
        deviceUnavailable = true;
    }

    /**
     * @return number of cues requested so far, including failed ones
     */
    public int getRequests() {
        return requests.get();
    }

}
