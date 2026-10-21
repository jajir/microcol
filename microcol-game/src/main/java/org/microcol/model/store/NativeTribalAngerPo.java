package org.microcol.model.store;

import org.microcol.model.EuropeanNation;
import org.microcol.model.NativeNation;

/**
 * Tribal anger of one native nation toward one European. Every nation and
 * European pair is required in the save, with a value from 0 to 100.
 */
public class NativeTribalAngerPo {

    private NativeNation nation;

    private EuropeanNation european;

    private Double value;

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

    public Double getValue() {
        return value;
    }

    public void setValue(final Double value) {
        this.value = value;
    }

}
