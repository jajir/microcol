package org.microcol.model.store;

import java.util.List;

/**
 * Saved persistent top ten (R-0197). The entries keep their stored order, which
 * decides the tie order of equal totals.
 */
public final class HallOfFamePo {

    private List<HallOfFameEntryPo> entries;

    /**
     * @return the entries in ranking order
     */
    public List<HallOfFameEntryPo> getEntries() {
        return entries;
    }

    /**
     * @param entries the entries to set, in ranking order
     */
    public void setEntries(final List<HallOfFameEntryPo> entries) {
        this.entries = entries;
    }

}
