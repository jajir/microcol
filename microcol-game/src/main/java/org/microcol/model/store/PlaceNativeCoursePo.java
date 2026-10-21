package org.microcol.model.store;

/**
 * Saved native course place of a learning colonist. The settlement identity is
 * required; the model checks it against the settlement's saved course.
 */
public final class PlaceNativeCoursePo extends PlacePo {

    private Integer settlementId;

    public Integer getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(final Integer settlementId) {
        this.settlementId = settlementId;
    }

}
