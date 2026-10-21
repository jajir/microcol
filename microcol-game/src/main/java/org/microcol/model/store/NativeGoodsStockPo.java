package org.microcol.model.store;

/**
 * One offered good of a native settlement and its saved stock. Both fields are
 * required; the goods name must name a tradable goods type.
 */
public class NativeGoodsStockPo {

    /** Name of the goods type, as saved. */
    private String goods;

    private Integer stock;

    public String getGoods() {
        return goods;
    }

    public void setGoods(final String goods) {
        this.goods = goods;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(final Integer stock) {
        this.stock = stock;
    }

}
