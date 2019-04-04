package org.microcol.model.store;

import org.microcol.model.GoodsType;

import com.google.common.base.MoreObjects;

public final class CargoSlotPo {

    /**
     * In this cargo slot could be stored this unit.
     */
    private Integer unitId;

    private Integer amount;

    private GoodsType goodsType;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CargoPo.class).add("unitId", unitId).add("amount", amount)
                .add("goodsType", goodsType).toString();
    }

    public boolean containsGood() {
        return goodsType != null && amount != null;
    }

    /**
     * @return the unitId
     */
    public Integer getUnitId() {
        return unitId;
    }

    /**
     * @param unitId
     *            the unitId to set
     */
    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    /**
     * @return the amount
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * @param amount
     *            the amount to set
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * @return the goodsType
     */
    public GoodsType getGoodsType() {
        return goodsType;
    }

    /**
     * @param goodsType
     *            the goodsType to set
     */
    public void setGoodsType(GoodsType goodsType) {
        this.goodsType = goodsType;
    }

}
