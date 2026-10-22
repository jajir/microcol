package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Pure rules of the paid incitement (R-0166). The model draws one roll from its
 * persisted game random state after every check has passed; this class gives
 * the exact price, the chance of success and the outcome of one roll.
 *
 * The price is
 * {@code ceil(500 * (1 + hostility / 100) / (1 + 0.1 * missions))}, where
 * hostility is the visitor's nation's numeric hostility toward the native
 * settlement and missions counts the visitor's owner's missions in that tribe.
 * It is rewritten as {@code ceil(50 * (100 + hostility) / (10 + missions))}, so
 * the fraction is kept until the ceiling and no integer truncation happens
 * first.
 */
final class NativeIncitementRules {

    /** Price of an incitement before the hostility and mission factors. */
    static final double BASE_PRICE = 500;

    /** Largest hostility; the price is highest at this value. */
    static final double MAX_HOSTILITY = 100;

    /** Chance of success of an ordinary missionary, as a fraction. */
    static final double ORDINARY_CHANCE = 0.25;

    /** Chance of success of an effective expert, as a fraction. */
    static final double EXPERT_CHANCE = 0.5;

    /** Tribal anger of the target after a success, at least this value. */
    static final double TARGET_ANGER = 75;

    /** Native action turns that a successful incitement target keeps. */
    static final int ORDER_TURNS = 20;

    private NativeIncitementRules() {
    }

    /**
     * @param hostility required numeric hostility of the visitor's nation, 0 to
     *                  100
     * @param missions  required count of the owner's missions in the tribe,
     *                  nonnegative
     * @return the exact price in gold, rounded up once
     */
    static int price(final double hostility, final int missions) {
        Preconditions.checkArgument(
                hostility >= 0 && hostility <= MAX_HOSTILITY,
                "Native hostility is out of range");
        Preconditions.checkArgument(missions >= 0,
                "Native mission count is negative");
        return (int) Math.ceil(BASE_PRICE / 10 * (MAX_HOSTILITY + hostility)
                / (10 + missions));
    }

    /**
     * @param expert whether the missionary acts as an effective expert
     * @return chance that the incitement succeeds, a fraction
     */
    static double chance(final boolean expert) {
        return expert ? EXPERT_CHANCE : ORDINARY_CHANCE;
    }

    /**
     * @param chance chance from {@link #chance}
     * @param roll   uniform roll in [0, 1)
     * @return success when the roll falls below the chance
     */
    static NativeIncitementOutcome outcome(final double chance,
            final double roll) {
        Preconditions.checkArgument(roll >= 0 && roll < 1,
                "Native incitement roll must be in [0, 1)");
        return roll < chance ? NativeIncitementOutcome.SUCCESS
                : NativeIncitementOutcome.FAILURE;
    }

}
