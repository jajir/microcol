package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds information about price of some good in Europe.
 */
public final class GoodsTrade {

    private final GoodsType goodsType;

    /**
     * Player could sell goods for this price.
     */
    private int sellPrice;

    /**
     * Player could buy goods for this price.
     */
    private int buyPrice;

    GoodsTrade(final GoodsType goodsType, final int sellPrice, final int buyPrice) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
    }

    /**
     * Return how much units of given goods could be bought for given gold.
     * 
     * @param availableGold
     *            required gold
     * @return Return goods amount which could be bought
     */
    public Goods getAvailableAmountFor(final int availableGold) {
        return new Goods(goodsType,
                Math.min(availableGold / buyPrice, CargoSlot.MAX_CARGO_SLOT_CAPACITY));
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }

    public GoodsType getGoodsType() {
        return goodsType;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(int buyPrice) {
        this.buyPrice = buyPrice;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("buyPrice", buyPrice).add("goodsType", goodsType)
                .add("sellPrice", sellPrice).toString();
    }

}
