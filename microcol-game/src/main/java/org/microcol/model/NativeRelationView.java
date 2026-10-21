package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * What the player may read about one learned native settlement toward the
 * player's own European: the attitude from the larger of tribal anger and local
 * alarm, the signed local alarm change with its direction and rate, and a rival
 * European whose visible pressure provokes the settlement. Only the model
 * builds it; hidden values of rivals never appear here.
 */
public final class NativeRelationView {

    /** Direction of the local alarm change, read from its exact sign. */
    public enum AlarmTrend {
        RISING, FALLING, STABLE
    }

    private final NativeHostility attitude;

    private final double alarmChange;

    private final Optional<Player> provokingRival;

    private NativeRelationView(final NativeHostility attitude,
            final double alarmChange, final Optional<Player> provokingRival) {
        this.attitude = attitude;
        this.alarmChange = alarmChange;
        this.provokingRival = provokingRival;
    }

    /**
     * @param attitude       required attitude band of the settlement
     * @param alarmChange    finite signed local alarm change
     * @param provokingRival required rival whose visible pressure provokes the
     *                       settlement, empty when no such evidence exists
     * @return view of the settlement
     */
    static NativeRelationView of(final NativeHostility attitude,
            final double alarmChange, final Optional<Player> provokingRival) {
        Preconditions.checkNotNull(attitude);
        Preconditions.checkArgument(Double.isFinite(alarmChange),
                "Native alarm change must be finite");
        Preconditions.checkNotNull(provokingRival);
        return new NativeRelationView(attitude, alarmChange, provokingRival);
    }

    /**
     * Rate indication: no mark for zero or falling change, one for a rise up to
     * 2 points, two above 2 through 5 and three above 5. The rule takes the
     * exact change, so a negative zero also gets no mark.
     *
     * @param alarmChange finite signed local alarm change
     * @return number of exclamation marks from 0 to 3
     */
    public static int exclamationMarksFor(final double alarmChange) {
        if (alarmChange <= 0) {
            return 0;
        }
        if (alarmChange <= 2) {
            return 1;
        }
        if (alarmChange <= 5) {
            return 2;
        }
        return 3;
    }

    /**
     * @return attitude band from the larger of tribal anger and local alarm
     */
    public NativeHostility getAttitude() {
        return attitude;
    }

    /**
     * @return exact signed local alarm change since the last peaceful update
     */
    public double getAlarmChange() {
        return alarmChange;
    }

    /**
     * @return rising, falling or stable, from the exact sign of the change
     */
    public AlarmTrend getTrend() {
        if (alarmChange > 0) {
            return AlarmTrend.RISING;
        }
        if (alarmChange < 0) {
            return AlarmTrend.FALLING;
        }
        return AlarmTrend.STABLE;
    }

    /**
     * @return exclamation marks of the rate indication, see
     *         {@link #exclamationMarksFor}
     */
    public int getExclamationMarks() {
        return exclamationMarksFor(alarmChange);
    }

    /**
     * @return rival European whose visible pressure provokes the settlement;
     *         empty when the player has no permitted evidence of one
     */
    public Optional<Player> getProvokingRival() {
        return provokingRival;
    }

}
