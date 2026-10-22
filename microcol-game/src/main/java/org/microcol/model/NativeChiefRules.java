package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Pure rules of the native chief encounter (R-0158). The model draws one roll
 * from its persisted game random state, applies the outcome and pays the gold;
 * this class only maps the mood, the scout role and the roll to the outcome and
 * gives the gold amount. Probabilities are whole per-mille, so the seasoned
 * halving of the death chance is exact.
 */
final class NativeChiefRules {

    /** Per-mille total of every distribution. */
    static final int TOTAL = 1000;

    /** Largest gold an ordinary settlement pays in one encounter. */
    static final int ORDINARY_MAX_GOLD = 100;

    /** Largest gold a nation's capital pays in one encounter. */
    static final int CAPITAL_MAX_GOLD = 200;

    private NativeChiefRules() {
    }

    /**
     * @param hostility required mood of the settlement toward the visitor
     * @return per-mille chance of gold, which seasoned and ordinary scouts
     *         share
     */
    static int goldPerMille(final NativeHostility hostility) {
        switch (hostility) {
        case CONTENT:
            return 250;
        case UNEASY:
            return 150;
        case ANGRY:
            return 100;
        case HOSTILE:
            return 0;
        default:
            throw new IllegalStateException("Unknown native hostility");
        }
    }

    /**
     * @param hostility required mood of the settlement toward the visitor
     * @param seasoned  whether the visitor is a seasoned scout
     * @return per-mille chance of death; a seasoned scout halves it exactly
     */
    static int deathPerMille(final NativeHostility hostility,
            final boolean seasoned) {
        final int ordinary;
        switch (hostility) {
        case CONTENT:
            ordinary = 50;
            break;
        case UNEASY:
            ordinary = 100;
            break;
        case ANGRY:
            ordinary = 300;
            break;
        case HOSTILE:
            ordinary = 600;
            break;
        default:
            throw new IllegalStateException("Unknown native hostility");
        }
        return seasoned ? ordinary / 2 : ordinary;
    }

    /**
     * Information takes every per-mille not given to gold or death, so the
     * seasoned difference in death is added to it.
     *
     * @param hostility required mood of the settlement toward the visitor
     * @param seasoned  whether the visitor is a seasoned scout
     * @return per-mille chance of information
     */
    static int informationPerMille(final NativeHostility hostility,
            final boolean seasoned) {
        return TOTAL - goldPerMille(hostility)
                - deathPerMille(hostility, seasoned);
    }

    /**
     * @param hostility required mood of the settlement toward the visitor
     * @param seasoned  whether the visitor is a seasoned scout
     * @param roll      uniform roll in [0, 1)
     * @return information below its share, then gold, then death
     */
    static NativeChiefOutcome draw(final NativeHostility hostility,
            final boolean seasoned, final double roll) {
        Preconditions.checkNotNull(hostility);
        Preconditions.checkArgument(roll >= 0 && roll < 1,
                "Native chief roll must be in [0, 1)");
        final int bucket = (int) (roll * TOTAL);
        final int information = informationPerMille(hostility, seasoned);
        if (bucket < information) {
            return NativeChiefOutcome.INFORMATION;
        }
        if (bucket < information + goldPerMille(hostility)) {
            return NativeChiefOutcome.GOLD;
        }
        return NativeChiefOutcome.DEATH;
    }

    /**
     * @param treasury treasury of the settlement, nonnegative
     * @param capital  whether the settlement is its nation's capital
     * @return gold paid: the cap of the settlement, but never more than it
     *         holds
     */
    static int goldAmount(final int treasury, final boolean capital) {
        return Math.min(capital ? CAPITAL_MAX_GOLD : ORDINARY_MAX_GOLD,
                treasury);
    }

}
