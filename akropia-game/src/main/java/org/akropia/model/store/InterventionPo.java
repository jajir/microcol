package org.akropia.model.store;

/**
 * Saved foreign-intervention target of a declared colonial nation: the fixed
 * target, the cumulative owls counted from its first owner update, the earned
 * flag (R-0189) and the delivered flag of the earned forces (R-0190).
 */
public final class InterventionPo {

    private Integer target;

    private Long owls;

    private Boolean earned;

    private Boolean delivered;

    public Integer getTarget() {
        return target;
    }

    public void setTarget(final Integer target) {
        this.target = target;
    }

    public Long getOwls() {
        return owls;
    }

    public void setOwls(final Long owls) {
        this.owls = owls;
    }

    public Boolean getEarned() {
        return earned;
    }

    public void setEarned(final Boolean earned) {
        this.earned = earned;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(final Boolean delivered) {
        this.delivered = delivered;
    }
}
