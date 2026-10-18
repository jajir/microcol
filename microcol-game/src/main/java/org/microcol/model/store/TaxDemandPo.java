package org.microcol.model.store;

/** Frozen, unresolved Crown tax demand. */
public final class TaxDemandPo {

    private Integer proposedRate;

    private TaxProtestPo protest;

    /** @return the rate that acceptance would set */
    public Integer getProposedRate() {
        return proposedRate;
    }

    public void setProposedRate(final Integer proposedRate) {
        this.proposedRate = proposedRate;
    }

    /** @return frozen protest consequence, or null when no stock is eligible */
    public TaxProtestPo getProtest() {
        return protest;
    }

    public void setProtest(final TaxProtestPo protest) {
        this.protest = protest;
    }
}
