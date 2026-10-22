package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Persisted target of a successful paid incitement (R-0166): the European power
 * that one native tribe now pursues, and the native action turns left before
 * the order expires. Immutable; a new success replaces the whole order.
 */
public final class NativeIncitementOrder {

    private final EuropeanNation target;

    private final int remainingTurns;

    NativeIncitementOrder(final EuropeanNation target,
            final int remainingTurns) {
        Preconditions.checkArgument(target != null,
                "Native incitement target is missing");
        Preconditions.checkArgument(
                remainingTurns >= 1
                        && remainingTurns <= NativeIncitementRules.ORDER_TURNS,
                "Native incitement turns are out of range");
        this.target = target;
        this.remainingTurns = remainingTurns;
    }

    /**
     * @return the European power that the tribe pursues
     */
    public EuropeanNation getTarget() {
        return target;
    }

    /**
     * @return native action turns still to come, from 1 to 20
     */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    NativeIncitementOrder afterTurn() {
        return new NativeIncitementOrder(target, remainingTurns - 1);
    }

}
