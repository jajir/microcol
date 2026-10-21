package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Persistent assignment of one carrier to a saved route. It keeps the stable
 * route ID, the stop the carrier is heading to or standing at, the reason it
 * waits, if any, and the route stops the carrier accepted at its last boundary.
 * The route and stop processing keep their own rules; the assignment stores
 * only progress and that boundary copy, so a later route edit reaches this
 * carrier only at its next stop.
 */
public final class TradeRouteAssignment {

    private final int routeId;
    private int stopIndex;
    private TradeRoutePause pause;
    private List<TradeRouteStop> stops;

    TradeRouteAssignment(final int routeId, final int stopIndex,
            final TradeRoutePause pause, final List<TradeRouteStop> stops) {
        Preconditions.checkArgument(
                routeId >= 0 && stopIndex >= 0 && stops != null
                        && stopIndex < stops.size(),
                "Invalid trade route assignment");
        this.routeId = routeId;
        this.stopIndex = stopIndex;
        this.pause = pause;
        this.stops = ImmutableList.copyOf(stops);
    }

    public int getRouteId() {
        return routeId;
    }

    /** @return index of the stop the carrier is heading to or standing at */
    public int getStopIndex() {
        return stopIndex;
    }

    /** @return the reason the carrier waits, or empty while it may continue */
    public Optional<TradeRoutePause> getPause() {
        return Optional.ofNullable(pause);
    }

    /**
     * @return the route stops as this carrier accepted them at its last
     *         boundary, in the same order and count as the saved route
     */
    public List<TradeRouteStop> getStops() {
        return stops;
    }

    void setStopIndex(final int stopIndex) {
        this.stopIndex = stopIndex;
    }

    void setPause(final TradeRoutePause pause) {
        this.pause = pause;
    }

    void setStops(final List<TradeRouteStop> stops) {
        this.stops = ImmutableList.copyOf(stops);
    }
}
