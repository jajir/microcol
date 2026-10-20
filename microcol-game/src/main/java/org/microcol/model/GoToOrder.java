package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Persistent go-to order of one unit. It keeps the destination square, the
 * identity and name of a named friendly colony when the order targets one, and
 * whether the route is currently paused. Progress is the unit's own location
 * and movement, so the order stores no route.
 */
final class GoToOrder {
    private final Location destination;
    /** Identity of the named colony, or {@code null} for a map square. */
    private final Integer colonyId;
    /** Name of the colony, or the square coordinates, for notices. */
    private final String destinationName;
    private boolean paused;

    GoToOrder(final Location destination, final Integer colonyId,
            final String destinationName, final boolean paused) {
        this.destination = Preconditions.checkNotNull(destination,
                "Go-to destination is null");
        this.colonyId = colonyId;
        this.destinationName = Preconditions.checkNotNull(destinationName,
                "Go-to destination name is null");
        this.paused = paused;
    }

    Location getDestination() {
        return destination;
    }

    Integer getColonyId() {
        return colonyId;
    }

    String getDestinationName() {
        return destinationName;
    }

    boolean isPaused() {
        return paused;
    }

    void setPaused(final boolean paused) {
        this.paused = paused;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("destination", destination)
                .add("colonyId", colonyId).add("paused", paused).toString();
    }
}
