package org.microcol.model.store;

import org.microcol.model.EuropeanNation;
import org.microcol.model.NativeNation;

/**
 * Saved order of one native tribe after a successful paid incitement (R-0166):
 * the European power it pursues and its native action turns left. A tribe with
 * no active order has no entry.
 */
public class NativeIncitementPo {

    private NativeNation nation;

    private EuropeanNation target;

    private Integer remainingTurns;

    public NativeNation getNation() {
        return nation;
    }

    public void setNation(final NativeNation nation) {
        this.nation = nation;
    }

    public EuropeanNation getTarget() {
        return target;
    }

    public void setTarget(final EuropeanNation target) {
        this.target = target;
    }

    public Integer getRemainingTurns() {
        return remainingTurns;
    }

    public void setRemainingTurns(final Integer remainingTurns) {
        this.remainingTurns = remainingTurns;
    }

}
