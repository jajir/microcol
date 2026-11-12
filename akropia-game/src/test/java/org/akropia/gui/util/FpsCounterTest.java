package org.akropia.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class FpsCounterTest {
    private final AtomicLong time = new AtomicLong(17);
    private final FpsCounter counter = new FpsCounter(time::get);

    @Test
    void updatesAtOneSecondAndStartsANewInterval() {
        counter.start();
        paintAfter(999_999_999L);
        assertEquals(0, counter.getFps());
        paintAfter(1);
        assertEquals(2, counter.getFps());
        paintAfter(1_000_000_000L);
        assertEquals(1, counter.getFps());
    }

    @Test
    void dividesByActualElapsedTimeAndRetainsValueWithoutPainting() {
        counter.start();
        paintAfter(250_000_000L);
        paintAfter(250_000_000L);
        paintAfter(250_000_000L);
        paintAfter(1_250_000_000L);
        assertEquals(2, counter.getFps());
        time.addAndGet(10_000_000_000L);
        assertEquals(2, counter.getFps());
        counter.screenWasPainted();
        assertEquals(0, counter.getFps());
    }

    @Test
    void ignoresInactiveFramesAndExcludesPauseOnRestart() {
        paintAfter(1_000_000_000L);
        assertEquals(0, counter.getFps());
        counter.start();
        paintAfter(500_000_000L);
        paintAfter(500_000_000L);
        assertEquals(2, counter.getFps());
        paintAfter(500_000_000L);
        counter.stop();
        paintAfter(60_000_000_000L);
        assertEquals(2, counter.getFps());
        counter.start();
        assertEquals(0, counter.getFps());
        paintAfter(1_000_000_000L);
        assertEquals(1, counter.getFps());
    }

    @Test
    void startResetsAnActiveMeasurement() {
        counter.start();
        paintAfter(1_000_000_000L);
        paintAfter(500_000_000L);
        counter.start();
        assertEquals(0, counter.getFps());
        paintAfter(500_000_000L);
        assertEquals(0, counter.getFps());
        paintAfter(500_000_000L);
        assertEquals(2, counter.getFps());
    }

    @Test
    void elapsedTimeWorksAcrossNanoTimeWraparound() {
        time.set(Long.MAX_VALUE - 500_000_000L);
        counter.start();
        paintAfter(1_000_000_000L);
        assertEquals(1, counter.getFps());
    }

    private void paintAfter(final long nanos) {
        time.addAndGet(nanos);
        counter.screenWasPainted();
    }
}
