package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Pure rules of one patrol check on a legal ship step. The model draws the roll
 * from its persisted game random state and applies the result; this class only
 * maps the roll to a band and the band to the movement that remains.
 */
public final class ShipPatrolRules {

    /** Result of one patrol check. */
    public enum Outcome {
        UNDETECTED, SLOWED, STOPPED
    }

    /** Roll below which the ship passes unnoticed. */
    static final double UNDETECTED_BELOW = 0.5;

    /** Roll below which one remaining movement point is lost. */
    static final double SLOWED_BELOW = 0.75;

    private ShipPatrolRules() {
    }

    /**
     * @param roll uniform roll in [0, 1)
     * @return undetected below one half, slowed below three quarters, otherwise
     *         stopped
     */
    public static Outcome draw(final double roll) {
        Preconditions.checkArgument(roll >= 0 && roll < 1,
                "Patrol roll must be in [0, 1)");
        if (roll < UNDETECTED_BELOW) {
            return Outcome.UNDETECTED;
        }
        return roll < SLOWED_BELOW ? Outcome.SLOWED : Outcome.STOPPED;
    }

    /**
     * @param outcome         required result of the patrol check
     * @param remainingThirds movement left before the check, in thirds of a
     *                        point
     * @return movement left after the check, in thirds of a point
     */
    public static int remainingAfter(final Outcome outcome,
            final int remainingThirds) {
        Preconditions.checkNotNull(outcome);
        Preconditions.checkArgument(remainingThirds >= 0,
                "Remaining movement cannot be negative");
        switch (outcome) {
        case SLOWED:
            return Math.max(0,
                    remainingThirds - MovementPoints.THIRDS_PER_POINT);
        case STOPPED:
            return 0;
        default:
            return remainingThirds;
        }
    }
}
