package org.microcol.model.campaign.po;

import java.util.List;

import org.microcol.model.Calendar;

import com.google.common.base.Preconditions;

/** Selects automatic recovery slots at a completed human-turn boundary. */
public final class RecoveryCheckpointPolicy {

    private RecoveryCheckpointPolicy() {
    }

    public static List<RecoverySlot> slotsForTurn(final Calendar calendar,
            final boolean fresh, final boolean human,
            final boolean autosaveEnabled) {
        Preconditions.checkNotNull(calendar);
        if (!fresh || !human || !autosaveEnabled) {
            return List.of();
        }
        final int turn = calendar.getNumberOfPlayedTurns();
        final int year = calendar.getCurrentYear();
        if (turn > 0 && year >= 1500 && year % 10 == 0
                && calendar.getYearForTurnNo(turn - 1) < year) {
            return List.of(RecoverySlot.LATEST_TURN,
                    RecoverySlot.PREVIOUS_DECADE);
        }
        return List.of(RecoverySlot.LATEST_TURN);
    }
}
