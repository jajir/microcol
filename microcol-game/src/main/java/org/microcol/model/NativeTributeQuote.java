package org.microcol.model;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

/**
 * Terms of a native tribute demand that the player may confirm now (R-0162). It
 * freezes the settlement, the demanding unit and the units that add force, the
 * defenders and their strength, the treasury, the relation values and the game
 * turn that decides the wait. Only the model creates it; a confirmation
 * compares the whole quote with the live terms before the first change.
 */
public final class NativeTributeQuote {

    private final NativeSettlement settlement;

    private final int visitorId;

    private final List<Integer> forceUnitIds;

    private final double force;

    private final List<Integer> defenderIds;

    private final double defense;

    private final int treasury;

    private final double tribalAnger;

    private final double localAlarm;

    private final int turn;

    NativeTributeQuote(final NativeSettlement settlement, final int visitorId,
            final List<Integer> forceUnitIds, final double force,
            final List<Integer> defenderIds, final double defense,
            final int treasury, final double tribalAnger,
            final double localAlarm, final int turn) {
        this.settlement = Objects.requireNonNull(settlement);
        this.visitorId = visitorId;
        this.forceUnitIds = ImmutableList.copyOf(forceUnitIds);
        this.force = force;
        this.defenderIds = ImmutableList.copyOf(defenderIds);
        this.defense = defense;
        this.treasury = treasury;
        this.tribalAnger = tribalAnger;
        this.localAlarm = localAlarm;
        this.turn = turn;
    }

    /**
     * @return the settlement the demand is made to
     */
    public NativeSettlement getSettlement() {
        return settlement;
    }

    /**
     * @return identity of the unit that confirmed the demand
     */
    public int getVisitorId() {
        return visitorId;
    }

    /**
     * @return identities of the units that add force, in ascending order
     */
    public List<Integer> getForceUnitIds() {
        return forceUnitIds;
    }

    /**
     * @return base strength of the demanding force, the sum of the force units
     */
    public double getForce() {
        return force;
    }

    /**
     * @return identities of the actual defenders on the settlement square
     */
    public List<Integer> getDefenderIds() {
        return defenderIds;
    }

    /**
     * @return strength of the defenders with the settlement's defense bonus
     */
    public double getDefense() {
        return defense;
    }

    /**
     * @return the settlement's defense bonus in percent, a capital included
     */
    public int getDefenseBonusPercent() {
        return settlement.getDefenseBonusPercent();
    }

    /**
     * @return gold the settlement held when the terms were quoted
     */
    public int getTreasury() {
        return treasury;
    }

    /**
     * @return most gold this settlement can pay for one tribute
     */
    public int getCap() {
        return NativeTributeRules.cap(settlement.isCapital());
    }

    /**
     * @return whether the force reaches the defenders' strength
     */
    public boolean isAccepted() {
        return NativeTributeRules.isAccepted(force, defense);
    }

    /**
     * @return gold the player receives: the cap or the treasury, or nothing
     *         when the demand is refused
     */
    public int getPayment() {
        return isAccepted()
                ? NativeTributeRules.payment(treasury, settlement.isCapital())
                : 0;
    }

    /**
     * @return exact tribal anger of the tribe toward the demanding European
     */
    public double getTribalAnger() {
        return tribalAnger;
    }

    /**
     * @return exact local alarm of the settlement toward the demanding European
     */
    public double getLocalAlarm() {
        return localAlarm;
    }

    /**
     * @return game turn of the quote, which decides the wait of the European
     */
    public int getTurn() {
        return turn;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NativeTributeQuote)) {
            return false;
        }
        final NativeTributeQuote that = (NativeTributeQuote) other;
        return settlement == that.settlement && visitorId == that.visitorId
                && forceUnitIds.equals(that.forceUnitIds)
                && Double.compare(force, that.force) == 0
                && defenderIds.equals(that.defenderIds)
                && Double.compare(defense, that.defense) == 0
                && treasury == that.treasury
                && Double.compare(tribalAnger, that.tribalAnger) == 0
                && Double.compare(localAlarm, that.localAlarm) == 0
                && turn == that.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(settlement), visitorId,
                forceUnitIds, force, defenderIds, defense, treasury,
                tribalAnger, localAlarm, turn);
    }

}
