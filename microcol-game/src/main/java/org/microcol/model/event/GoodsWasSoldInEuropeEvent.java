package org.microcol.model.event;

import org.microcol.model.GoodsAmount;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;

/**
 * Event is triggered when player sell goods in Europe port.
 */
public final class GoodsWasSoldInEuropeEvent extends AbstractModelEvent {

    private final GoodsAmount goodsAmount;

    public GoodsWasSoldInEuropeEvent(final Model model, final GoodsAmount goodsAmount) {
        super(model);
        this.goodsAmount = Preconditions.checkNotNull(goodsAmount);
    }

    /**
     * @return the goodsAmount
     */
    public GoodsAmount getGoodsAmount() {
        return goodsAmount;
    }

}
