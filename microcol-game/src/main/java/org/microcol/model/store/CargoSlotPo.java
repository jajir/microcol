package org.microcol.model.store;

import org.microcol.model.GoodType;

import com.google.common.base.MoreObjects;

public class CargoSlotPo {

    /**
     * In this cargo slot could be stored this unit.
     */
    private Integer unitId;

    private Integer amount;

    private GoodType goodType;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CargoPo.class).add("unitId", unitId).add("amount", amount)
                .add("goodType", goodType).toString();
    }

    public boolean containsGood() {
        return goodType != null && amount != null;
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
     * @return the goodType
     */
    public GoodType getGoodType() {
        return goodType;
    }

    /**
     * @param goodType
     *            the goodType to set
     */
    public void setGoodType(GoodType goodType) {
        this.goodType = goodType;
    }

}
