package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Pure rules of one lost-city rumor. The model draws the random rolls from its
 * persisted game random state, consumes the marker and applies the result; this
 * class only maps a roll to the outcome, the gold amount and the De Soto
 * substitution.
 */
public final class LostCityRumorRules {

    /** Result of a resolved rumor before the De Soto substitution. */
    public enum Outcome {
        EMPTY, GOLD, RECRUIT, EXPLORER_LOST, BURIAL
    }

    /** Smallest gold award, inclusive. */
    public static final int MIN_GOLD = 200;

    /** Largest gold award, inclusive. */
    public static final int MAX_GOLD = 500;

    /** Cumulative upper bounds of the ordinary scout distribution. */
    private static final double[] ORDINARY_BOUNDS = { 0.40, 0.65, 0.80, 0.90,
            1.00 };

    /** Cumulative upper bounds of the seasoned scout distribution. */
    private static final double[] SEASONED_BOUNDS = { 0.30, 0.65, 0.90, 0.95,
            1.00 };

    private LostCityRumorRules() {
    }

    /**
     * @param seasoned whether the explorer is a seasoned scout
     * @param roll     uniform roll in [0, 1)
     * @return outcome of the roll in the order empty, gold, recruit, lost,
     *         burial
     */
    public static Outcome draw(final boolean seasoned, final double roll) {
        Preconditions.checkArgument(roll >= 0 && roll < 1,
                "Lost-city roll must be in [0, 1)");
        final double[] bounds = seasoned ? SEASONED_BOUNDS : ORDINARY_BOUNDS;
        final Outcome[] outcomes = Outcome.values();
        for (int index = 0; index < bounds.length; index++) {
            if (roll < bounds[index]) {
                return outcomes[index];
            }
        }
        return Outcome.BURIAL;
    }

    /**
     * @param roll uniform roll in [0, 1)
     * @return whole gold from {@link #MIN_GOLD} through {@link #MAX_GOLD},
     *         uniformly
     */
    public static int gold(final double roll) {
        Preconditions.checkArgument(roll >= 0 && roll < 1,
                "Lost-city roll must be in [0, 1)");
        return MIN_GOLD + (int) (roll * (MAX_GOLD - MIN_GOLD + 1));
    }

    /**
     * @param drawn  outcome of the roll
     * @param deSoto whether De Soto is a member of the explorer's Congress
     * @return the drawn outcome, except that De Soto turns both bad results
     *         into an empty one
     */
    public static Outcome withDeSoto(final Outcome drawn,
            final boolean deSoto) {
        Preconditions.checkNotNull(drawn);
        if (deSoto && (drawn == Outcome.EXPLORER_LOST
                || drawn == Outcome.BURIAL)) {
            return Outcome.EMPTY;
        }
        return drawn;
    }

}
