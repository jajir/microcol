package org.akropia.model.store;

import java.util.List;

import org.akropia.model.Founder;

/** Required saved Congress state for a colonial player. */
public final class CongressPo {
    private Boolean activated;
    private Integer owls;
    private List<Founder> members;
    private List<Founder> candidates;
    private Founder selected;
    private Integer generation;
    private Boolean jonesDeliveryPending;

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(final Boolean value) {
        activated = value;
    }

    public Integer getOwls() {
        return owls;
    }

    public void setOwls(final Integer value) {
        owls = value;
    }

    public List<Founder> getMembers() {
        return members;
    }

    public void setMembers(final List<Founder> value) {
        members = value;
    }

    public List<Founder> getCandidates() {
        return candidates;
    }

    public void setCandidates(final List<Founder> value) {
        candidates = value;
    }

    public Founder getSelected() {
        return selected;
    }

    public void setSelected(final Founder value) {
        selected = value;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(final Integer value) {
        generation = value;
    }

    public Boolean getJonesDeliveryPending() {
        return jonesDeliveryPending;
    }

    public void setJonesDeliveryPending(final Boolean value) {
        jonesDeliveryPending = value;
    }
}
