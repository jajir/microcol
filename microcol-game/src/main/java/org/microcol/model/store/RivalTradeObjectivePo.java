package org.microcol.model.store;

/**
 * Saved trade objective of a rival colonial player. It keeps the colony and
 * carrier identities and the leg the carrier is on; the model checks ownership
 * and existence again before any step.
 */
public final class RivalTradeObjectivePo {

    private Integer colonyId;

    private Integer carrierId;

    /** Name of the leg destination, such as {@code TO_EUROPE}. */
    private String stage;

    public Integer getColonyId() {
        return colonyId;
    }

    public void setColonyId(final Integer colonyId) {
        this.colonyId = colonyId;
    }

    public Integer getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(final Integer carrierId) {
        this.carrierId = carrierId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(final String stage) {
        this.stage = stage;
    }
}
