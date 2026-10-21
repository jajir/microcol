package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Result of one configured goods entry at a processed route stop: what was
 * moved, the treasury change it caused in Europe and the reason. Settled
 * quantities stay reported even when the entry was rejected part-way.
 */
public final class TradeRouteTransfer {

    private final boolean unload;
    private final GoodsType goodsType;
    private final int amount;
    private final int goldChange;
    private final TradeRouteTransferReason reason;
    private final String detail;

    TradeRouteTransfer(final boolean unload, final GoodsType goodsType,
            final int amount, final int goldChange,
            final TradeRouteTransferReason reason, final String detail) {
        Preconditions.checkArgument(amount >= 0, "Negative amount");
        this.unload = unload;
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.amount = amount;
        this.goldChange = goldChange;
        this.reason = Preconditions.checkNotNull(reason);
        this.detail = detail;
    }

    /** @return whether this entry was an unload, otherwise a load */
    public boolean isUnload() {
        return unload;
    }

    public GoodsType getGoodsType() {
        return goodsType;
    }

    /** @return units actually moved, possibly zero */
    public int getAmount() {
        return amount;
    }

    /**
     * @return signed treasury change of this entry: a sale is positive and a
     *         purchase negative; zero outside Europe
     */
    public int getGoldChange() {
        return goldChange;
    }

    public TradeRouteTransferReason getReason() {
        return reason;
    }

    /** @return the rule's message for a rejected entry, otherwise empty */
    public Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }
}
