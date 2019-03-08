package org.microcol.model;

import com.google.common.base.MoreObjects;

public final class ConstructionTurnProduction {

    //TODO should be optional. in some cases it's not existing.
    private final Goods consumedGoods;

    private final Goods producedGoods;

    private final Goods blockedGoods;

    ConstructionTurnProduction(final Goods consumedGoods, final Goods producedGoods,
            final Goods blockedGoods) {
        this.consumedGoods = consumedGoods;
        this.producedGoods = producedGoods;
        this.blockedGoods = blockedGoods;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("consumedGoods", consumedGoods)
                .add("producedGoods", producedGoods).add("blockedGoods", blockedGoods).toString();
    }

    /**
     * @return the consumedGoods
     */
    public Goods getConsumedGoods() {
        return consumedGoods;
    }

    /**
     * @return the producedGoods
     */
    public Goods getProducedGoods() {
        return producedGoods;
    }

    /**
     * @return the blockedGoods
     */
    public Goods getBlockedGoods() {
        return blockedGoods;
    }

}
