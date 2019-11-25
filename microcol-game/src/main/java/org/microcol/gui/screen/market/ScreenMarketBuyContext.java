package org.microcol.gui.screen.market;

import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds data for correct buying goods at market.
 */
public class ScreenMarketBuyContext {
    private final Goods maxPossibleGoodsToBuy;
    private final CargoSlot targetCargoSlot;

    public ScreenMarketBuyContext(final Goods maxPossibleGoodsToBuy, final CargoSlot targetCargoSlot) {
        this.maxPossibleGoodsToBuy = Preconditions.checkNotNull(maxPossibleGoodsToBuy);
        this.targetCargoSlot = Preconditions.checkNotNull(targetCargoSlot);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ScreenMarketBuyContext.class)
                .add("maxPossibleGoodsToBuy", maxPossibleGoodsToBuy)
                .add("targetCargoSlot", targetCargoSlot).toString();
    }

    public Goods getMaxPossibleGoodsToBuy() {
        return maxPossibleGoodsToBuy;
    }

    public CargoSlot getTargetCargoSlot() {
        return targetCargoSlot;
    }

    public GoodsType getGoodsType() {
        return maxPossibleGoodsToBuy.getType();
    }

    public int getAmountOfMaxPossibleGoodsToBuy() {
        return maxPossibleGoodsToBuy.getAmount();
    }
}
