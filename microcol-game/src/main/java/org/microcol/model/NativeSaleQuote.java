package org.microcol.model;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * Immutable terms of one sale or gift of carried goods to a native settlement
 * (R-0160), exactly as the player was shown them. Every term that a state
 * change can alter is part of the value: the carried amount, the treasury, the
 * Europe bid, the haggle state, the attitude of the natives, the room the
 * village has for the goods, and the trade's transaction revision. The revision
 * rises with every expedition start, haggle draw and completed sale or gift, so
 * an offer never matches again once it has been used or overtaken, even when a
 * later state reads the same values. A confirmation of a stale offer is refused
 * and nothing is traded or drawn twice.
 */
public final class NativeSaleQuote {

    /** Haggle state that the quote was computed with. */
    public enum Haggle {

        /** One haggle may still be tried in this expedition. */
        OFFERED,

        /** The one haggle of this expedition succeeded for these goods. */
        ACCEPTED,

        /**
         * The one haggle of this expedition failed; the original offer stays.
         */
        REFUSED,

        /**
         * No haggle applies: a gift, or the expedition's haggle was spent on
         * other goods.
         */
        UNAVAILABLE

    }

    private final int carrierId;

    private final Location settlement;

    private final GoodsType goods;

    private final int quantity;

    private final boolean gift;

    private final int gold;

    private final Haggle haggle;

    private final int held;

    private final int treasury;

    private final int europeBid;

    private final boolean wanted;

    private final boolean ship;

    private final NativeHostility hostility;

    private final int room;

    private final int revision;

    NativeSaleQuote(final int carrierId, final Location settlement,
            final GoodsType goods, final int quantity, final boolean gift,
            final int gold, final Haggle haggle, final int held,
            final int treasury, final int europeBid, final boolean wanted,
            final boolean ship, final NativeHostility hostility, final int room,
            final int revision) {
        this.carrierId = carrierId;
        this.settlement = Preconditions.checkNotNull(settlement);
        this.goods = Preconditions.checkNotNull(goods);
        this.quantity = quantity;
        this.gift = gift;
        this.gold = gold;
        this.haggle = Preconditions.checkNotNull(haggle);
        this.held = held;
        this.treasury = treasury;
        this.europeBid = europeBid;
        this.wanted = wanted;
        this.ship = ship;
        this.hostility = Preconditions.checkNotNull(hostility);
        this.room = room;
        this.revision = revision;
    }

    /**
     * @return goods offered, one tradable type
     */
    public GoodsType getGoods() {
        return goods;
    }

    /**
     * @return pieces the carrier gives, positive
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return whether the goods are a gift, which pays no gold
     */
    public boolean isGift() {
        return gift;
    }

    /**
     * @return gold the carrier receives; zero for a gift
     */
    public int getGold() {
        return gold;
    }

    /**
     * @return haggle state these terms were computed with
     */
    public Haggle getHaggle() {
        return haggle;
    }

    /**
     * @return pieces of these goods the carrier held when quoted
     */
    public int getHeld() {
        return held;
    }

    /**
     * @return gold of the settlement when quoted
     */
    public int getTreasury() {
        return treasury;
    }

    /**
     * @return Europe bid of the goods when quoted
     */
    public int getEuropeBid() {
        return europeBid;
    }

    /**
     * @return whether the settlement wants these goods, the ×1.5 factor
     */
    public boolean isWanted() {
        return wanted;
    }

    /**
     * @return whether the carrier is a ship, the ×0.8 factor
     */
    public boolean isShip() {
        return ship;
    }

    /**
     * @return attitude of the natives toward the carrier when quoted
     */
    public NativeHostility getHostility() {
        return hostility;
    }

    /**
     * @return most pieces the village could still count for these goods when
     *         quoted, the room below the largest storable amount
     */
    public int getRoom() {
        return room;
    }

    /**
     * @return transaction revision of the trade when quoted
     */
    public int getRevision() {
        return revision;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NativeSaleQuote)) {
            return false;
        }
        final NativeSaleQuote that = (NativeSaleQuote) other;
        return carrierId == that.carrierId && settlement.equals(that.settlement)
                && goods.equals(that.goods) && quantity == that.quantity
                && gift == that.gift && gold == that.gold
                && haggle == that.haggle && held == that.held
                && treasury == that.treasury && europeBid == that.europeBid
                && wanted == that.wanted && ship == that.ship
                && hostility == that.hostility && room == that.room
                && revision == that.revision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(carrierId, settlement, goods, quantity, gift, gold,
                haggle, held, treasury, europeBid, wanted, ship, hostility,
                room, revision);
    }

}
