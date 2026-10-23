package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Rules of a paid veteran mercenary group (R-0169). Peacetime and rebel offers
 * share the same bundle and price; only the trigger differs.
 */
final class MercenaryRules {

    /** Treasury a human colonial nation needs for a peacetime draw. */
    static final int TREASURY_THRESHOLD = 5000;

    /** Every tenth own update before rebellion, or rebel update after it. */
    static final int INTERVAL = 10;

    /** Chance of the peacetime draw, as a fraction. */
    static final double PEACETIME_CHANCE = 0.25;

    /** Veteran soldiers in one bundle. */
    static final int SOLDIERS = 2;

    /** Base price of the bundle before the difficulty factor. */
    private static final int BASE_PRICE = 1000;

    private MercenaryRules() {
    }

    /**
     * @param difficulty required selected difficulty
     * @return the bundle price, {@code ceil(1000 × k)} gold
     */
    static int price(final Difficulty difficulty) {
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        return (BASE_PRICE * RecruitmentPool.factor(difficulty) + 99) / 100;
    }

    /**
     * @param updates count of updates so far, including the current one
     * @return whether this update is one of the tenth updates of the interval
     */
    static boolean isIntervalUpdate(final int updates) {
        return updates > 0 && updates % INTERVAL == 0;
    }
}
