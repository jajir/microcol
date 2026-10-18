package org.microcol.model.store;

import java.util.Map;

/**
 * Persistent tax state of one colonial player: the single authoritative rate,
 * cadence counters, an optional pending demand and commodity arrears. Every
 * field except {@code pending} is required when a game is loaded.
 */
public final class TaxPo {

    private Integer rate;

    private Boolean activated;

    private Integer updates;

    private Integer issued;

    private TaxDemandPo pending;

    private Map<String, Integer> arrears;

    /** @return current tax percentage, 0 to 75 */
    public Integer getRate() {
        return rate;
    }

    public void setRate(final Integer rate) {
        this.rate = rate;
    }

    /** @return whether the nation has ever owned a colony */
    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(final Boolean activated) {
        this.activated = activated;
    }

    /** @return own updates counted toward the next demand */
    public Integer getUpdates() {
        return updates;
    }

    public void setUpdates(final Integer updates) {
        this.updates = updates;
    }

    /** @return number of demands issued so far; identifies the pending one */
    public Integer getIssued() {
        return issued;
    }

    public void setIssued(final Integer issued) {
        this.issued = issued;
    }

    /** @return the unresolved demand, or null when there is none */
    public TaxDemandPo getPending() {
        return pending;
    }

    public void setPending(final TaxDemandPo pending) {
        this.pending = pending;
    }

    /** @return owed gold by goods name; a present entry is a boycott */
    public Map<String, Integer> getArrears() {
        return arrears;
    }

    public void setArrears(final Map<String, Integer> arrears) {
        this.arrears = arrears;
    }
}
