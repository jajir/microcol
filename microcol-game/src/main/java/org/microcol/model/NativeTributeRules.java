package org.microcol.model;

import java.util.Optional;

/**
 * Pure rules of a native tribute demand (R-0162): the force that reaches the
 * defenders' strength, the payment cap of an ordinary settlement and of a
 * capital, and the ten-turn wait of one European at one settlement. No unit,
 * treasury or relation changes here.
 */
final class NativeTributeRules {

    /** Turns after a demand before the same European may demand again here. */
    static final int COOLDOWN_TURNS = 10;

    /** Most gold an ordinary settlement pays for one tribute. */
    static final int ORDINARY_CAP = 100;

    /** Most gold a capital pays for one tribute. */
    static final int CAPITAL_CAP = 200;

    private NativeTributeRules() {
    }

    /**
     * @param capital whether the settlement is its nation's capital
     * @return most gold one tribute can take from that settlement
     */
    static int cap(final boolean capital) {
        return capital ? CAPITAL_CAP : ORDINARY_CAP;
    }

    /**
     * @param treasury nonnegative gold the settlement holds
     * @param capital  whether the settlement is its nation's capital
     * @return gold paid when the demand is accepted: the cap, or the whole
     *         treasury when it holds less
     */
    static int payment(final int treasury, final boolean capital) {
        return Math.min(cap(capital), treasury);
    }

    /**
     * A force equal to the defenders' strength is enough; a lower force is
     * refused.
     *
     * @param force   base strength of the demanding force
     * @param defense strength of the actual defenders
     * @return whether the demand is accepted
     */
    static boolean isAccepted(final double force, final double defense) {
        return force >= defense;
    }

    /**
     * @param lastDemand turn of the European's last demand here, if any
     * @param turn       current game turn
     * @return whether the wait since that demand has not yet passed
     */
    static boolean isCooling(final Optional<Integer> lastDemand,
            final int turn) {
        return lastDemand.isPresent()
                && turn < lastDemand.get() + COOLDOWN_TURNS;
    }

}
