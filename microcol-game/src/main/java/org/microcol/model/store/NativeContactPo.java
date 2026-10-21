package org.microcol.model.store;

import org.microcol.model.EuropeanNation;
import org.microcol.model.NativeNation;

/**
 * One land contact: a colonial European nation has introduced itself to a whole
 * native tribe. Both fields are required; an absent list of these records means
 * no contact yet.
 */
public class NativeContactPo {

    private EuropeanNation european;

    private NativeNation nation;

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    public NativeNation getNation() {
        return nation;
    }

    public void setNation(final NativeNation nation) {
        this.nation = nation;
    }

}
