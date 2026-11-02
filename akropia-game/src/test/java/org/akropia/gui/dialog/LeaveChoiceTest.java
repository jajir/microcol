package org.akropia.gui.dialog;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Save and continue leaves only after the manual save succeeded; every other
 * outcome keeps the game (R-0387 user correction).
 */
class LeaveChoiceTest {

    @Test
    void saveAndContinueLeavesOnlyAfterASuccessfulSave() {
        final AtomicInteger saves = new AtomicInteger();

        assertTrue(PersistingService.leaveAfter(1, () -> {
            saves.incrementAndGet();
            return true;
        }));
        // Cancel, Escape, closing the Save dialog or a failed write.
        assertFalse(PersistingService.leaveAfter(1, () -> {
            saves.incrementAndGet();
            return false;
        }));
        assertEquals(2, saves.get());
    }

    @Test
    void discardLeavesAndCancelStaysWithoutSaving() {
        assertTrue(PersistingService.leaveAfter(2, () -> {
            throw new AssertionError("Leaving without saving saves nothing");
        }));
        assertFalse(PersistingService.leaveAfter(0, () -> {
            throw new AssertionError("Cancel saves nothing");
        }));
    }
}
