package org.microcol.model.store;

/**
 * Saved native mission place of a stationed missionary. The settlement identity
 * is required; the model checks it against the settlement's saved mission.
 */
public final class PlaceNativeMissionPo extends PlacePo {

    private Integer settlementId;

    public Integer getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(final Integer settlementId) {
        this.settlementId = settlementId;
    }

}
