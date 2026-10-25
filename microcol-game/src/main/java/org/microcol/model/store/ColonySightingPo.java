package org.microcol.model.store;

import org.microcol.model.Location;

/**
 * Persistent form of one colony remembered by a player. It holds only what the
 * player observed at the recorded turn.
 */
public final class ColonySightingPo {

    private Location location;

    private String colonyName;

    private String ownerName;

    private Integer population;
    private Integer fortificationLevel;
    private Integer observedTurn;

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(final Integer population) {
        this.population = population;
    }

    public Integer getFortificationLevel() {
        return fortificationLevel;
    }

    public void setFortificationLevel(final Integer fortificationLevel) {
        this.fortificationLevel = fortificationLevel;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public String getColonyName() {
        return colonyName;
    }

    public void setColonyName(final String colonyName) {
        this.colonyName = colonyName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getObservedTurn() {
        return observedTurn;
    }

    public void setObservedTurn(final Integer observedTurn) {
        this.observedTurn = observedTurn;
    }
}
