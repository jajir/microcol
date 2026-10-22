package org.microcol.model.store;

import org.microcol.model.EuropeanNation;
import org.microcol.model.NativeChiefOutcome;

/**
 * One chief encounter of a visiting European nation with a native settlement
 * (R-0158). The settlement holding the record gives its identity. Both fields
 * are required; an absent list of these records means no encounter yet.
 */
public class NativeChiefEncounterPo {

    private EuropeanNation european;

    private NativeChiefOutcome outcome;

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    /**
     * @return saved outcome of the encounter, required
     */
    public NativeChiefOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(final NativeChiefOutcome outcome) {
        this.outcome = outcome;
    }

}
