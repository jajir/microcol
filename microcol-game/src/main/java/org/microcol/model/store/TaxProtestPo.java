package org.microcol.model.store;

import org.microcol.model.GoodsType;

/** Frozen consequence of protesting a Crown tax demand. */
public final class TaxProtestPo {

    private GoodsType goods;

    private Integer colonyId;

    private Integer quantity;

    private Integer bid;

    /** @return the commodity that is destroyed and boycotted */
    public GoodsType getGoods() {
        return goods;
    }

    public void setGoods(final GoodsType goods) {
        this.goods = goods;
    }

    /** @return identity of the colony that loses the stock */
    public Integer getColonyId() {
        return colonyId;
    }

    public void setColonyId(final Integer colonyId) {
        this.colonyId = colonyId;
    }

    /** @return destroyed quantity, 1 to 100 */
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    /** @return the European bid quoted when the demand was issued */
    public Integer getBid() {
        return bid;
    }

    public void setBid(final Integer bid) {
        this.bid = bid;
    }
}
