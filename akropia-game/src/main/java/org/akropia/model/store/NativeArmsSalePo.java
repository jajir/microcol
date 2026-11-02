package org.akropia.model.store;

import org.akropia.model.EuropeanNation;
import org.akropia.model.NativeNation;

/**
 * Weapons and horses sold to one native nation by one European, kept below the
 * 50-unit step that gives tribal anger. Only nonzero remainders are saved; an
 * absent entry means none is retained.
 */
public class NativeArmsSalePo {

    private NativeNation nation;

    private EuropeanNation european;

    private Integer quantity;

    public NativeNation getNation() {
        return nation;
    }

    public void setNation(final NativeNation nation) {
        this.nation = nation;
    }

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

}
