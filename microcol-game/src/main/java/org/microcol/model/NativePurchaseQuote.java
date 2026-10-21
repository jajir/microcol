package org.microcol.model;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * Immutable terms of one purchase of offered goods from a native settlement
 * (R-0161), the second stage after a confirmed sale or gift of the same
 * expedition, exactly as the player was shown them. Every term a state change
 * can alter is part of the value: the stock, the native treasury, the buyer's
 * gold, the room the carrier has for the goods, the Europe ask, the attitude of
 * the natives, the request result of the stage, and the transaction revision
 * with the revision that opened the stage. A confirmation of a stale quote is
 * refused, so nothing is bought twice.
 */
public final class NativePurchaseQuote {

    private final int carrierId;

    private final Location settlement;

    private final GoodsType goods;

    private final int quantity;

    private final int gold;

    private final NativeSaleQuote.Haggle haggle;

    private final int stock;

    private final int treasury;

    private final int buyerGold;

    private final int room;

    private final int askPrice;

    private final NativeHostility hostility;

    private final int revision;

    private final int stageRevision;

    NativePurchaseQuote(final int carrierId, final Location settlement,
            final GoodsType goods, final int quantity, final int gold,
            final NativeSaleQuote.Haggle haggle, final int stock,
            final int treasury, final int buyerGold, final int room,
            final int askPrice, final NativeHostility hostility,
            final int revision, final int stageRevision) {
        this.carrierId = carrierId;
        this.settlement = Preconditions.checkNotNull(settlement);
        this.goods = Preconditions.checkNotNull(goods);
        this.quantity = quantity;
        this.gold = gold;
        this.haggle = Preconditions.checkNotNull(haggle);
        this.stock = stock;
        this.treasury = treasury;
        this.buyerGold = buyerGold;
        this.room = room;
        this.askPrice = askPrice;
        this.hostility = Preconditions.checkNotNull(hostility);
        this.revision = revision;
        this.stageRevision = stageRevision;
    }

    /**
     * @return goods bought, one tradable type the settlement offers
     */
    public GoodsType getGoods() {
        return goods;
    }

    /**
     * @return pieces the carrier receives, positive
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return gold the buyer pays for the complete quote, rounded once
     */
    public int getGold() {
        return gold;
    }

    /**
     * @return request result of the purchase stage these terms were computed
     *         with
     */
    public NativeSaleQuote.Haggle getHaggle() {
        return haggle;
    }

    /**
     * @return pieces of the goods the settlement offered when quoted
     */
    public int getStock() {
        return stock;
    }

    /**
     * @return gold of the settlement when quoted
     */
    public int getTreasury() {
        return treasury;
    }

    /**
     * @return Europe ask of one piece when quoted, taxed by nothing
     */
    public int getAskPrice() {
        return askPrice;
    }

    /**
     * @return attitude of the natives toward the carrier when quoted
     */
    public NativeHostility getHostility() {
        return hostility;
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
        if (!(other instanceof NativePurchaseQuote)) {
            return false;
        }
        final NativePurchaseQuote that = (NativePurchaseQuote) other;
        return carrierId == that.carrierId && settlement.equals(that.settlement)
                && goods.equals(that.goods) && quantity == that.quantity
                && gold == that.gold && haggle == that.haggle
                && stock == that.stock && treasury == that.treasury
                && buyerGold == that.buyerGold && room == that.room
                && askPrice == that.askPrice && hostility == that.hostility
                && revision == that.revision
                && stageRevision == that.stageRevision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(carrierId, settlement, goods, quantity, gold,
                haggle, stock, treasury, buyerGold, room, askPrice, hostility,
                revision, stageRevision);
    }

}
