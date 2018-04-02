package org.microcol.model;

import com.google.common.base.MoreObjects;

public class ConstructionTurnProduction {

    public final static ConstructionTurnProduction EMPTY = new ConstructionTurnProduction(0, 0, 0);

    private final int consumedGoods;

    private final int producedGoods;

    private final int blockedGoods;

    ConstructionTurnProduction(final int consumedGoods, final int producedGoods,
            final int blockedGoods) {
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
    public int getConsumedGoods() {
        return consumedGoods;
    }

    /**
     * @return the producedGoods
     */
    public int getProducedGoods() {
        return producedGoods;
    }

    /**
     * @return the blockedGoods
     */
    public int getBlockedGoods() {
        return blockedGoods;
    }

}
