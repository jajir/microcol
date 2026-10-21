package org.microcol.model.store;

import org.microcol.model.Location;

/**
 * Saved second-colony expansion of a rival colonial player. It keeps the
 * colony, founder, carrier and site identities and the stage of the journey;
 * the model checks ownership and existence again before any step.
 */
public final class RivalExpansionObjectivePo {

    private Integer colonyId;

    private Integer founderId;

    private Integer carrierId;

    /** Known legal site chosen when the founder was released. */
    private Location site;

    /** Name of the stage, such as {@code BOARDING}. */
    private String stage;

    public Integer getColonyId() {
        return colonyId;
    }

    public void setColonyId(final Integer colonyId) {
        this.colonyId = colonyId;
    }

    public Integer getFounderId() {
        return founderId;
    }

    public void setFounderId(final Integer founderId) {
        this.founderId = founderId;
    }

    public Integer getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(final Integer carrierId) {
        this.carrierId = carrierId;
    }

    public Location getSite() {
        return site;
    }

    public void setSite(final Location site) {
        this.site = site;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(final String stage) {
        this.stage = stage;
    }
}
