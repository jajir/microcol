package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.po.RecoveryCheckpointPolicy;
import org.microcol.model.campaign.po.RecoverySlot;

class RecoveryCheckpointPolicyTest {

    private List<RecoverySlot> slots(final int turn, final boolean fresh,
            final boolean human, final boolean enabled) {
        return RecoveryCheckpointPolicy.slotsForTurn(
                new Calendar(1490, 1800, turn), fresh, human, enabled);
    }

    @Test
    void firstTurnAndDecadeCrossingsChooseSeparateSlots() {
        assertEquals(List.of(RecoverySlot.LATEST_TURN),
                slots(0, true, true, true));
        assertEquals(List.of(RecoverySlot.LATEST_TURN),
                slots(9, true, true, true));
        assertEquals(
                List.of(RecoverySlot.LATEST_TURN, RecoverySlot.PREVIOUS_DECADE),
                slots(10, true, true, true));
        assertEquals(List.of(RecoverySlot.LATEST_TURN),
                slots(11, true, true, true));
        assertEquals(
                List.of(RecoverySlot.LATEST_TURN, RecoverySlot.PREVIOUS_DECADE),
                slots(20, true, true, true));
    }

    @Test
    void seasonalDecadeCrossingWritesOnce() {
        assertEquals(List.of(RecoverySlot.LATEST_TURN),
                slots(149, true, true, true));
        assertEquals(
                List.of(RecoverySlot.LATEST_TURN, RecoverySlot.PREVIOUS_DECADE),
                slots(150, true, true, true));
        for (int turn = 151; turn <= 153; turn++) {
            assertEquals(List.of(RecoverySlot.LATEST_TURN),
                    slots(turn, true, true, true));
        }
    }

    @Test
    void resumedComputerOrDisabledTurnWritesNothing() {
        for (int turn : new int[] { 0, 10, 150 }) {
            assertEquals(List.of(), slots(turn, false, true, true));
            assertEquals(List.of(), slots(turn, true, false, true));
            assertEquals(List.of(), slots(turn, true, true, false));
        }
    }
}
