package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds information about price of some good in one nation's European market.
 * <p>
 * Pressure is kept in eighths of a goods unit: a full own-market unit is 8, a
 * quarter-strength cross-market unit is 2. One step of bid is 800 eighths,
 * which is the accepted +100/-100 pressure.
 */
public final class GoodsTrade {

    /** Eighths of pressure that move the bid by one step. */
    static final int PRESSURE_PER_STEP = 800;

    static final int MIN_BID = 1;

    static final int MAX_BID = 20;

    private final GoodsType goodsType;

    /** Initial bid, which quiet recovery moves back toward. */
    private final int initialBid;

    /** Constant difference between ask and bid. */
    private final int spread;

    /**
     * Player could sell goods for this price.
     */
    private int sellPrice;

    /**
     * Player could buy goods for this price.
     */
    private int buyPrice;

    /** Exact signed pressure remainder, always within one step. */
    private int pressure;

    /**
     * Whether any own or cross-market trade affected this good since update.
     */
    private boolean tradedSinceUpdate;

    GoodsTrade(final GoodsType goodsType, final int sellPrice,
            final int buyPrice) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.initialBid = sellPrice;
        this.spread = buyPrice - sellPrice;
    }

    /**
     * Adds signed pressure and moves the bid by every whole step. Sales are
     * positive and lower the bid; purchases are negative and raise it.
     *
     * @param eighths required signed pressure in eighths of a unit
     */
    void addPressure(final long eighths) {
        // Long arithmetic cannot overflow for int-sized trades, and every
        // result is computed before the first field changes, so this method
        // never leaves a market half updated.
        final long total = pressure + eighths;
        long newBid = sellPrice - total / PRESSURE_PER_STEP;
        int remainder = (int) (total % PRESSURE_PER_STEP);
        // At a bound, pressure that would move the bid further outward is
        // discarded so that it cannot bank a later reversal.
        if (newBid <= MIN_BID) {
            newBid = MIN_BID;
            remainder = Math.min(remainder, 0);
        } else if (newBid >= MAX_BID) {
            newBid = MAX_BID;
            remainder = Math.max(remainder, 0);
        }
        tradedSinceUpdate = true;
        pressure = remainder;
        sellPrice = (int) newBid;
        buyPrice = sellPrice + spread;
    }

    /**
     * Quiet recovery at this nation's update: without any affecting trade, the
     * bid moves one step toward its initial value. The flag always restarts.
     */
    void recoverAtUpdate() {
        final boolean quiet = !tradedSinceUpdate;
        tradedSinceUpdate = false;
        if (quiet) {
            if (sellPrice < initialBid) {
                sellPrice++;
            } else if (sellPrice > initialBid) {
                sellPrice--;
            }
            buyPrice = sellPrice + spread;
        }
    }

    /**
     * Restores saved state after validating it; the initial bid and spread
     * remain the catalogue values.
     *
     * @param bid           saved bid, between 1 and 20
     * @param pressureValue saved pressure, strictly within one step
     * @param traded        saved traded-since-update flag
     * @throws IllegalArgumentException when the state is outside its bounds
     */
    void restoreState(final int bid, final int pressureValue,
            final boolean traded) {
        Preconditions.checkArgument(bid >= MIN_BID && bid <= MAX_BID,
                "Market bid %s is outside %s-%s", bid, MIN_BID, MAX_BID);
        // Direct signed bounds; Math.abs(Integer.MIN_VALUE) is still negative.
        Preconditions.checkArgument(
                pressureValue > -PRESSURE_PER_STEP
                        && pressureValue < PRESSURE_PER_STEP,
                "Market pressure %s is outside one step", pressureValue);
        Preconditions.checkArgument(bid != MIN_BID || pressureValue <= 0,
                "Outward pressure cannot be kept at minimum bid");
        Preconditions.checkArgument(bid != MAX_BID || pressureValue >= 0,
                "Outward pressure cannot be kept at maximum bid");
        this.sellPrice = bid;
        this.pressure = pressureValue;
        this.tradedSinceUpdate = traded;
        this.buyPrice = bid + spread;
    }

    int getPressure() {
        return pressure;
    }

    boolean isTradedSinceUpdate() {
        return tradedSinceUpdate;
    }

    /**
     * Return how much units of given goods could be bought for given gold.
     *
     * @param availableGold required gold
     * @return Return goods amount which could be bought
     */
    public Goods getAvailableAmountFor(final int availableGold) {
        return new Goods(goodsType, Math.min(availableGold / buyPrice,
                CargoSlot.MAX_CARGO_SLOT_CAPACITY));
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
        return MoreObjects.toStringHelper(this).add("buyPrice", buyPrice)
                .add("goodsType", goodsType).add("sellPrice", sellPrice)
                .toString();
    }

}
