package org.microcol.model.store;

import java.util.List;

import org.microcol.model.GoodsType;

/**
 * Saved stop of a trade route. Exactly one of {@code europe} or
 * {@code colonyId} is present; the model rejects any other combination. Both
 * goods lists are required, possibly empty, and keep their saved order.
 */
public final class TradeRouteStopPo {

    private Boolean europe;
    private Integer colonyId;
    private List<GoodsType> unloadGoods;
    private List<GoodsType> loadGoods;

    public List<GoodsType> getUnloadGoods() {
        return unloadGoods;
    }

    public void setUnloadGoods(final List<GoodsType> unloadGoods) {
        this.unloadGoods = unloadGoods;
    }

    public List<GoodsType> getLoadGoods() {
        return loadGoods;
    }

    public void setLoadGoods(final List<GoodsType> loadGoods) {
        this.loadGoods = loadGoods;
    }

    public Boolean getEurope() {
        return europe;
    }

    public void setEurope(final Boolean europe) {
        this.europe = europe;
    }

    public Integer getColonyId() {
        return colonyId;
    }

    public void setColonyId(final Integer colonyId) {
        this.colonyId = colonyId;
    }
}
