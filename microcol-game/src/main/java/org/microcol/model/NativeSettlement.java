package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Native settlement with a stable identity. It belongs to a native nation, not
 * to any European player, and it is never a {@link Colony}.
 */
public final class NativeSettlement {

    /** Additive defense bonus that only a nation's capital receives. */
    static final int CAPITAL_DEFENSE_BONUS_PERCENT = 25;

    /** Initial treasury of an ordinary settlement. */
    static final int ORDINARY_INITIAL_TREASURY = 1000;

    /** Initial treasury of a nation's capital. */
    static final int CAPITAL_INITIAL_TREASURY = 2000;

    private final int id;
    private final NativeNation nation;
    private final NativeSettlementClass settlementClass;
    private final boolean capital;
    private final Location location;
    private int treasury;

    NativeSettlement(final int id, final NativeNation nation,
            final boolean capital, final Location location,
            final int treasury) {
        this.id = id;
        this.nation = nation;
        this.settlementClass = nation.getSettlementClass();
        this.capital = capital;
        this.location = location;
        this.treasury = treasury;
    }

    public int getId() {
        return id;
    }

    public NativeNation getNation() {
        return nation;
    }

    public NativeSettlementClass getSettlementClass() {
        return settlementClass;
    }

    public boolean isCapital() {
        return capital;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * @return additive defense bonus for the real defenders of this settlement
     */
    public int getDefenseBonusPercent() {
        return settlementClass.getDefenseBonusPercent()
                + (capital ? CAPITAL_DEFENSE_BONUS_PERCENT : 0);
    }

    /**
     * @return treasury this settlement starts with; later updates belong to the
     *         trade owner
     */
    public int getInitialTreasury() {
        return capital ? CAPITAL_INITIAL_TREASURY : ORDINARY_INITIAL_TREASURY;
    }

    /**
     * @return gold currently held, as saved; land payments add to it
     */
    public int getTreasury() {
        return treasury;
    }

    /**
     * Replace the treasury with a total the caller has already validated, such
     * as a land payment added without overflow.
     *
     * @param newTreasury nonnegative gold
     */
    void setTreasury(final int newTreasury) {
        Preconditions.checkArgument(newTreasury >= 0,
                "Native treasury cannot be negative");
        treasury = newTreasury;
    }

    /**
     * Land claimed by this settlement: squares at Chebyshev distance one up to
     * its claim radius, the same radius as its home. The settlement's own
     * square is never claimed.
     *
     * @param at required square
     * @return whether this settlement claims the square
     */
    boolean claims(final Location at) {
        final int distance = location.getDistanceChebyshev(at);
        return distance >= 1 && distance <= settlementClass.getHomeRadius();
    }

}
