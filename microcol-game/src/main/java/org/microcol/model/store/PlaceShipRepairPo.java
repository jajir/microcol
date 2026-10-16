package org.microcol.model.store;

import org.microcol.model.Location;

/** The sole persistent state of an off-map damaged ship. */
public final class PlaceShipRepairPo extends PlacePo {
    private Location origin;
    private Boolean originWasPort;
    private Boolean europeDestination;
    private Integer destinationColonyId;
    private String phase;
    private Integer remainingPhaseUpdates;
    private Integer repairDuration;

    public Boolean isOriginWasPort() {
        return originWasPort;
    }

    public void setOriginWasPort(Boolean value) {
        originWasPort = value;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location value) {
        origin = value;
    }

    public Boolean isEuropeDestination() {
        return europeDestination;
    }

    public void setEuropeDestination(Boolean value) {
        europeDestination = value;
    }

    public Integer getDestinationColonyId() {
        return destinationColonyId;
    }

    public void setDestinationColonyId(Integer value) {
        destinationColonyId = value;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String value) {
        phase = value;
    }

    public Integer getRemainingPhaseUpdates() {
        return remainingPhaseUpdates;
    }

    public void setRemainingPhaseUpdates(Integer value) {
        remainingPhaseUpdates = value;
    }

    public Integer getRepairDuration() {
        return repairDuration;
    }

    public void setRepairDuration(Integer value) {
        repairDuration = value;
    }
}
