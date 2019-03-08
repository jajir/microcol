package org.microcol.model.event;

import org.microcol.model.Goods;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;

/**
 * Event is triggered when player sell goods in Europe port.
 */
public final class GoodsWasSoldInEuropeEvent extends AbstractModelEvent {

    private final Goods goods;

    public GoodsWasSoldInEuropeEvent(final Model model, final Goods goods) {
        super(model);
        this.goods = Preconditions.checkNotNull(goods);
    }

    /**
     * @return the goods
     */
    public Goods getGoods() {
        return goods;
    }

}
