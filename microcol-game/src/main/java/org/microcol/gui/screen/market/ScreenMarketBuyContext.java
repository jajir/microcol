package org.microcol.gui.screen.market;

import org.microcol.model.CargoSlot;
import org.microcol.model.EuropePurchaseLimit;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds data for correct buying goods at market: the model purchase snapshot
 * shown to the player and its target hold.
 */
public class ScreenMarketBuyContext {
    private final EuropePurchaseLimit limit;
    private final CargoSlot targetCargoSlot;

    public ScreenMarketBuyContext(final EuropePurchaseLimit limit,
            final CargoSlot targetCargoSlot) {
        this.limit = Preconditions.checkNotNull(limit);
        Preconditions.checkArgument(!limit.isBlocked(),
                "Nothing can be bought for %s", limit);
        this.targetCargoSlot = Preconditions.checkNotNull(targetCargoSlot);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ScreenMarketBuyContext.class)
                .add("limit", limit).add("targetCargoSlot", targetCargoSlot)
                .toString();
    }

    public EuropePurchaseLimit getLimit() {
        return limit;
    }

    public Goods getMaxPossibleGoodsToBuy() {
        return limit.getMaximum();
    }

    public CargoSlot getTargetCargoSlot() {
        return targetCargoSlot;
    }

    public GoodsType getGoodsType() {
        return limit.getGoodsType();
    }

    public int getAmountOfMaxPossibleGoodsToBuy() {
        return limit.getMaximum().getAmount();
    }
}
