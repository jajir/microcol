package org.microcol.model.store;

import org.microcol.model.EuropeanNation;

/**
 * Local alarm of one native settlement toward one European. The settlement id
 * refers to {@link NativeSettlementPo#getId()} in the same save. Every
 * settlement and European pair is required, with a value from 0 to 100.
 * <p>
 * The previous value is the alarm at the start of the last peaceful update. It
 * is optional: an absent value means no observation was saved, and the loaded
 * alarm is then taken as its own previous value, so the rate reads as stable.
 * </p>
 */
public class NativeLocalAlarmPo {

    private Integer settlementId;

    private EuropeanNation european;

    private Double value;

    private Double previousValue;

    public Integer getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(final Integer settlementId) {
        this.settlementId = settlementId;
    }

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(final Double value) {
        this.value = value;
    }

    /**
     * @return alarm at the start of the last peaceful update, or null when none
     *         was saved
     */
    public Double getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(final Double previousValue) {
        this.previousValue = previousValue;
    }

}
