package org.microcol.model;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Immutable snapshot of how many pieces of one commodity a player can buy on
 * the European market into one cargo slot, and why not more. The purchase
 * choice shows it;
 * {@link CargoSlot#buyFromEuropePort(EuropePurchaseLimit, int)} buys only while
 * every captured fact is still current.
 * <p>
 * Quotes derived from the snapshot use the captured unit price and tax, so the
 * shown totals are exactly the totals the confirmed purchase charges.
 */
public final class EuropePurchaseLimit {

    /** Why nothing at all can be bought into the hold. */
    public enum Blocker {
        /** At least one piece can be bought. */
        NONE,
        /** The player has no access to the European market. */
        TRADE_UNAVAILABLE,
        /** The commodity is boycotted. */
        BOYCOTTED,
        /**
         * The hold holds a unit or different goods, or its carrier is
         * inaccessible.
         */
        HOLD_UNAVAILABLE,
        /** The hold already holds the full amount of this commodity. */
        HOLD_FULL,
        /** The treasury does not pay for a single piece including tax. */
        NOT_ENOUGH_GOLD
    }

    private final GoodsType goodsType;
    private final Blocker blocker;
    private final int maximum;
    private final int room;
    private final int gold;
    private final int unitPrice;
    private final int taxPercentage;
    private final boolean limitedByGold;
    private final boolean limitedByRoom;

    EuropePurchaseLimit(final GoodsType goodsType, final Blocker blocker,
            final int maximum, final int room, final int gold,
            final EuropeTradeQuote unitQuote, final boolean limitedByGold,
            final boolean limitedByRoom) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.blocker = Preconditions.checkNotNull(blocker);
        Preconditions.checkArgument(
                blocker == Blocker.NONE ? maximum > 0 : maximum == 0,
                "Maximum %s doesn't match blocker %s", maximum, blocker);
        Preconditions.checkArgument(maximum <= room,
                "Maximum %s exceeds the room %s", maximum, room);
        this.maximum = maximum;
        this.room = room;
        this.gold = gold;
        this.unitPrice = unitQuote.getUnitPrice();
        this.taxPercentage = unitQuote.getTaxPercentage();
        this.limitedByGold = limitedByGold;
        this.limitedByRoom = limitedByRoom;
    }

    /** @return the commodity on offer */
    public GoodsType getGoodsType() {
        return goodsType;
    }

    /** @return why nothing can be bought, {@link Blocker#NONE} otherwise */
    public Blocker getBlocker() {
        return blocker;
    }

    /** @return whether nothing at all can be bought */
    public boolean isBlocked() {
        return blocker != Blocker.NONE;
    }

    /** @return the largest purchase, zero when blocked */
    public Goods getMaximum() {
        return Goods.of(goodsType, maximum);
    }

    /** @return the free space for this commodity in the hold */
    public int getRoom() {
        return room;
    }

    /** @return the treasury when the snapshot was taken */
    public int getGold() {
        return gold;
    }

    /** @return whether the treasury pays for no more than the maximum */
    public boolean isLimitedByGold() {
        return limitedByGold;
    }

    /** @return whether the hold has no room beyond the maximum */
    public boolean isLimitedByRoom() {
        return limitedByRoom;
    }

    /**
     * @param amount required amount from zero to the maximum
     * @return the purchase quote at the captured price and tax
     */
    public EuropeTradeQuote quote(final int amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= maximum,
                "Amount %s is outside 0..%s", amount, maximum);
        return new EuropeTradeQuote(Goods.of(goodsType, amount), true,
                unitPrice, taxPercentage);
    }

    /**
     * @param amount required amount from zero to the maximum
     * @return the treasury left after buying that amount
     */
    public int getGoldAfter(final int amount) {
        return gold - quote(amount).getTotal();
    }

    /**
     * Compare this shown snapshot with the current one, in the order a player
     * can act on: market access, hold, price and tax, treasury.
     *
     * @param current required current snapshot of the same commodity and hold
     * @throws EuropePurchaseChangedException when any fact changed
     */
    void verifyCurrent(final EuropePurchaseLimit current) {
        Preconditions.checkArgument(goodsType == current.goodsType,
                "Snapshots of different goods %s and %s", this, current);
        if (current.blocker == Blocker.TRADE_UNAVAILABLE
                || current.blocker == Blocker.BOYCOTTED) {
            throw new EuropePurchaseChangedException(
                    EuropePurchaseChangedException.Reason.TRADE);
        }
        if (current.blocker == Blocker.HOLD_UNAVAILABLE
                || current.blocker == Blocker.HOLD_FULL
                || current.room != room) {
            throw new EuropePurchaseChangedException(
                    EuropePurchaseChangedException.Reason.HOLD);
        }
        if (current.unitPrice != unitPrice
                || current.taxPercentage != taxPercentage) {
            throw new EuropePurchaseChangedException(
                    EuropePurchaseChangedException.Reason.PRICE);
        }
        if (current.gold != gold || !equals(current)) {
            throw new EuropePurchaseChangedException(
                    EuropePurchaseChangedException.Reason.GOLD);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EuropePurchaseLimit)) {
            return false;
        }
        final EuropePurchaseLimit that = (EuropePurchaseLimit) o;
        return goodsType == that.goodsType && blocker == that.blocker
                && maximum == that.maximum && room == that.room
                && gold == that.gold && unitPrice == that.unitPrice
                && taxPercentage == that.taxPercentage
                && limitedByGold == that.limitedByGold
                && limitedByRoom == that.limitedByRoom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(goodsType, blocker, maximum, room, gold, unitPrice,
                taxPercentage, limitedByGold, limitedByRoom);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(EuropePurchaseLimit.class)
                .add("goodsType", goodsType).add("blocker", blocker)
                .add("maximum", maximum).add("room", room).add("gold", gold)
                .add("unitPrice", unitPrice).add("taxPercentage", taxPercentage)
                .add("limitedByGold", limitedByGold)
                .add("limitedByRoom", limitedByRoom).toString();
    }

}
