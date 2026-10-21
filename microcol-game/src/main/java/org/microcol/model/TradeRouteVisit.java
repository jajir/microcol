package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Boundary of one carrier's processed route stop: the route and stop index it
 * processed and the place it did so. The carrier keeps it until it really
 * leaves that place, so Save and Continue, a repeated command, a changed route
 * or stop and a repair in the same port cannot repeat the stop, while a later
 * departure and arrival can be processed again.
 */
public final class TradeRouteVisit {

    private final int routeId;
    private final int stopIndex;
    /** Map square of a colony stop; {@code null} for the Europe port. */
    private final Location location;

    TradeRouteVisit(final int routeId, final int stopIndex,
            final Location location) {
        Preconditions.checkArgument(routeId >= 0 && stopIndex >= 0,
                "Invalid trade route visit");
        this.routeId = routeId;
        this.stopIndex = stopIndex;
        this.location = location;
    }

    public int getRouteId() {
        return routeId;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    /** @return the map square, or empty when the visit was in Europe */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * @param place map square, or {@code null} for the Europe port
     * @return whether this visit happened at that place
     */
    boolean isAt(final Location place) {
        return Objects.equals(location, place);
    }
}
