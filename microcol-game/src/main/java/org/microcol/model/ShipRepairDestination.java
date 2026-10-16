package org.microcol.model;

/** Read-only destination selected from reachable owned repair facilities. */
public final class ShipRepairDestination {
    private final Integer colonyId;
    private final int seaDistance;
    private final int repairDuration;

    private ShipRepairDestination(Integer colonyId, int distance,
            int duration) {
        this.colonyId = colonyId;
        seaDistance = distance;
        repairDuration = duration;
    }

    static ShipRepairDestination colony(Colony colony, int distance) {
        return new ShipRepairDestination(colony.getId(), distance,
                colony.wasConstructionBuilded(ConstructionType.SHIPYARD) ? 2
                        : 3);
    }

    static ShipRepairDestination europe() {
        return new ShipRepairDestination(null, Integer.MAX_VALUE, 2);
    }

    public boolean isEurope() {
        return colonyId == null;
    }

    public Integer getColonyId() {
        return colonyId;
    }

    public int getSeaDistance() {
        return seaDistance;
    }

    public int getRepairDuration() {
        return repairDuration;
    }
}
