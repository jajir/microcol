package org.microcol.model;

import java.util.Objects;

/**
 * Immutable terms of a paid incitement (R-0166): the visiting missionary, the
 * settlement and its tribe, the selected European power, the visitor's numeric
 * hostility, its mission count in the tribe, its effective expertise, the exact
 * price, the gold held, the chance and the revision of the tribe's order. The
 * confirmation shows these terms; the confirmed attempt runs only when the
 * current terms equal them, so a changed price, purse or order is refused.
 */
public final class NativeIncitementQuote {

    private final int visitorId;

    private final int settlementId;

    private final NativeNation nation;

    private final EuropeanNation target;

    private final double visitorHostility;

    private final int missions;

    private final boolean visitorExpert;

    private final int price;

    private final int gold;

    private final double chance;

    private final int orderRevision;

    NativeIncitementQuote(final int visitorId, final int settlementId,
            final NativeNation nation, final EuropeanNation target,
            final double visitorHostility, final int missions,
            final boolean visitorExpert, final int price, final int gold,
            final double chance, final int orderRevision) {
        this.visitorId = visitorId;
        this.settlementId = settlementId;
        this.nation = Objects.requireNonNull(nation);
        this.target = Objects.requireNonNull(target);
        this.visitorHostility = visitorHostility;
        this.missions = missions;
        this.visitorExpert = visitorExpert;
        this.price = price;
        this.gold = gold;
        this.chance = chance;
        this.orderRevision = orderRevision;
    }

    /**
     * @return identity of the visiting missionary that would incite
     */
    public int getVisitorId() {
        return visitorId;
    }

    /**
     * @return identity of the settlement whose tribe would be incited
     */
    public int getSettlementId() {
        return settlementId;
    }

    /**
     * @return the native tribe that would attack the target
     */
    public NativeNation getNation() {
        return nation;
    }

    /**
     * @return the European power that the tribe would attack
     */
    public EuropeanNation getTarget() {
        return target;
    }

    /**
     * @return the visitor's nation's numeric hostility toward the tribe, from 0
     *         to 100
     */
    public double getVisitorHostility() {
        return visitorHostility;
    }

    /**
     * @return missions the visitor's owner holds in this tribe now
     */
    public int getMissions() {
        return missions;
    }

    /**
     * @return whether the visiting missionary acts as an expert now
     */
    public boolean isVisitorExpert() {
        return visitorExpert;
    }

    /**
     * @return gold the attempt costs, paid once whether it succeeds or fails
     */
    public int getPrice() {
        return price;
    }

    /**
     * @return gold the visitor's owner held when the terms were read
     */
    public int getGold() {
        return gold;
    }

    /**
     * @return chance of success, a fraction: 0.25 ordinary, 0.5 expert
     */
    public double getChance() {
        return chance;
    }

    /**
     * @return revision of the tribe's order when the terms were read
     */
    public int getOrderRevision() {
        return orderRevision;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NativeIncitementQuote)) {
            return false;
        }
        final NativeIncitementQuote that = (NativeIncitementQuote) other;
        return visitorId == that.visitorId && settlementId == that.settlementId
                && nation == that.nation && target == that.target
                && visitorHostility == that.visitorHostility
                && missions == that.missions
                && visitorExpert == that.visitorExpert && price == that.price
                && gold == that.gold && chance == that.chance
                && orderRevision == that.orderRevision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(visitorId, settlementId, nation, target,
                visitorHostility, missions, visitorExpert, price, gold, chance,
                orderRevision);
    }

}
