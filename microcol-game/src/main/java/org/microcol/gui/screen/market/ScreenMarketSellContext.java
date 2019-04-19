package org.microcol.gui.screen.market;

import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds data for correct selling goods at market.
 */
public class ScreenMarketSellContext {
    private final Goods maxPossibleGoodsToBuy;
    private final CargoSlot sourceCargoSlot;

    public ScreenMarketSellContext(final Goods maxPossibleGoodsToBuy, final CargoSlot sourceCargoSlot) {
        this.maxPossibleGoodsToBuy = Preconditions.checkNotNull(maxPossibleGoodsToBuy);
        this.sourceCargoSlot = Preconditions.checkNotNull(sourceCargoSlot);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ScreenMarketSellContext.class)
                .add("maxPossibleGoodsToBuy", maxPossibleGoodsToBuy)
                .add("sourceCargoSlot", sourceCargoSlot).toString();
    }

    public Goods getMaxPossibleGoodsToBuy() {
        return maxPossibleGoodsToBuy;
    }

    public CargoSlot getSourceCargoSlot() {
        return sourceCargoSlot;
    }

    public GoodsType getGoodsType() {
        return maxPossibleGoodsToBuy.getType();
    }

    public int getAmountOfMaxPossibleGoodsToBuy() {
        return maxPossibleGoodsToBuy.getAmount();
    }
}
