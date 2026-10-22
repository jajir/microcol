package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Pure rules of the rival-mission contest (R-0165). The model draws one roll
 * from its persisted game random state and applies the outcome; this class only
 * gives each missionary's strength, the challenger's chance and the outcome of
 * one roll. Strength is 1 for an ordinary missionary or 2 for an effective
 * expert, multiplied by {@code 1 + (100 - hostility) / 100}, where hostility is
 * the numeric larger of tribal anger and local alarm toward that missionary's
 * nation.
 */
final class NativeContestRules {

    /** Largest hostility; the strength of a side is lowest at this value. */
    static final double MAX_HOSTILITY = 100;

    /** Strength of an ordinary missionary before the hostility factor. */
    static final double ORDINARY_STRENGTH = 1;

    /** Strength of an effective expert before the hostility factor. */
    static final double EXPERT_STRENGTH = 2;

    /** Lowest chance that the challenger wins, as a fraction. */
    static final double MIN_CHANCE = 0.10;

    /** Highest chance that the challenger wins, as a fraction. */
    static final double MAX_CHANCE = 0.90;

    private NativeContestRules() {
    }

    /**
     * @param expert    whether the missionary acts as an effective expert
     * @param hostility required numeric hostility toward its nation, 0 to 100
     * @return contest strength of that missionary
     */
    static double strength(final boolean expert, final double hostility) {
        Preconditions.checkArgument(
                hostility >= 0 && hostility <= MAX_HOSTILITY,
                "Native hostility is out of range");
        final double base = expert ? EXPERT_STRENGTH : ORDINARY_STRENGTH;
        return base * (1 + (MAX_HOSTILITY - hostility) / MAX_HOSTILITY);
    }

    /**
     * @param challenger strength of the visiting missionary, positive
     * @param resident   strength of the missionary holding the mission,
     *                   positive
     * @return chance that the challenger wins,
     *         {@code challenger / (challenger +
     *         resident)} clamped to 10 to 90 percent
     */
    static double challengerChance(final double challenger,
            final double resident) {
        Preconditions.checkArgument(challenger > 0 && resident > 0,
                "Contest strengths must be positive");
        return Math.min(MAX_CHANCE,
                Math.max(MIN_CHANCE, challenger / (challenger + resident)));
    }

    /**
     * @param chance challenger's chance from {@link #challengerChance}
     * @param roll   uniform roll in [0, 1)
     * @return success when the roll falls below the chance, as in combat
     */
    static NativeMissionContestOutcome outcome(final double chance,
            final double roll) {
        Preconditions.checkArgument(roll >= 0 && roll < 1,
                "Native contest roll must be in [0, 1)");
        return roll < chance ? NativeMissionContestOutcome.SUCCESS
                : NativeMissionContestOutcome.FAILURE;
    }

}
