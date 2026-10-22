package org.microcol.model.store;

/**
 * Saved record of one coastal battery shot (R-0177): the colony that fired, the
 * ship it fired at and the colony owner's name. The record blocks another shot
 * from the same battery at the same ship until the owner's round ends.
 */
public class CoastalBatteryShotPo {

    private Integer colonyId;

    private Integer shipId;

    private String ownerName;

    public Integer getColonyId() {
        return colonyId;
    }

    public void setColonyId(final Integer colonyId) {
        this.colonyId = colonyId;
    }

    public Integer getShipId() {
        return shipId;
    }

    public void setShipId(final Integer shipId) {
        this.shipId = shipId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

}
