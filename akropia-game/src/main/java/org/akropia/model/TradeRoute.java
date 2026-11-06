package org.akropia.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Saved named itinerary of one European player. The stable ID, not the name,
 * identifies the route; stops keep their order and one route-wide mode decides
 * whether a carrier waits at a stop.
 */
public final class TradeRoute {

    private final int id;
    private final Player owner;
    private final String name;
    private final TradeRouteKind kind;
    private final int startColonyId;
    private final List<TradeRouteStop> stops;
    private final TradeRouteMode mode;

    TradeRoute(final int id, final Player owner, final String name,
            final TradeRouteKind kind, final int startColonyId,
            final List<TradeRouteStop> stops, final TradeRouteMode mode) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.kind = kind;
        this.startColonyId = startColonyId;
        this.stops = ImmutableList.copyOf(stops);
        this.mode = Preconditions.checkNotNull(mode, "Mode is null");
    }

    public int getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    /** @return normalized display name */
    public String getName() {
        return name;
    }

    public TradeRouteKind getKind() {
        return kind;
    }

    public int getStartColonyId() {
        return startColonyId;
    }

    /** @return stops in travel order, at least one */
    public List<TradeRouteStop> getStops() {
        return stops;
    }

    /** @return route-wide rule for how long a carrier stays at a stop */
    public TradeRouteMode getMode() {
        return mode;
    }
}
