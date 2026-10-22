package org.microcol.model;

/**
 * Actual outcome of a confirmed tribute demand (R-0162). An accepted demand may
 * pay nothing when the treasury is empty; a refused one never pays.
 */
public final class NativeTributeResult {

    private final boolean accepted;

    private final int gold;

    private NativeTributeResult(final boolean accepted, final int gold) {
        this.accepted = accepted;
        this.gold = gold;
    }

    static NativeTributeResult of(final boolean accepted, final int gold) {
        return new NativeTributeResult(accepted, gold);
    }

    /**
     * @return whether the force reached the defenders' strength
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * @return gold that moved from the settlement to the player, zero when the
     *         demand was refused or the treasury was empty
     */
    public int getGold() {
        return gold;
    }

}
