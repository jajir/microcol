package org.akropia.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.akropia.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Immutable map preview of one saved trade route, computed by the model from
 * the route as saved now. Each leg is a pair of successive stops the carriers
 * travel, including the way back to the first stop. A leg shows one cheapest
 * legal path per shown transport: the route's kind for the assigned carrier
 * with the lowest unit ID, or both land and sea for a route without carrier.
 * Reading a preview changes nothing; it is not an order and does not change the
 * route's kind.
 */
public final class TradeRoutePreview {

    private final TradeRoute route;
    private final UnitWithCargo carrier;
    private final List<Leg> legs;

    TradeRoutePreview(final TradeRoute route, final UnitWithCargo carrier,
            final List<Leg> legs) {
        this.route = Preconditions.checkNotNull(route);
        this.carrier = carrier;
        this.legs = ImmutableList.copyOf(legs);
    }

    public TradeRoute getRoute() {
        return route;
    }

    /**
     * @return the assigned carrier whose movement rules the paths follow, or
     *         empty when no carrier is assigned and both transports are tried
     */
    public Optional<UnitWithCargo> getCarrier() {
        return Optional.ofNullable(carrier);
    }

    /** @return legs in travel order */
    public List<Leg> getLegs() {
        return legs;
    }

    /**
     * The map connections to draw: every shown path once, in travel order. A
     * leg back over the same squares of the same transport, such as the way
     * back between two colonies, is the same connection and is not repeated. A
     * way back over other squares stays a connection of its own.
     *
     * @return immutable distinct paths of the route, each in the direction of
     *         the first leg that travels it
     */
    public List<LegPath> getConnections() {
        final List<LegPath> connections = new ArrayList<>();
        for (final Leg leg : legs) {
            for (final LegPath path : leg.getPaths()) {
                if (connections.stream()
                        .noneMatch(known -> known.isSameConnection(path))) {
                    connections.add(path);
                }
            }
        }
        return ImmutableList.copyOf(connections);
    }

    /**
     * @param location required map square
     * @return whether any shown path of this route passes the square
     */
    public boolean passes(final Location location) {
        Preconditions.checkNotNull(location);
        return legs.stream().flatMap(leg -> leg.getPaths().stream())
                .anyMatch(path -> path.getSquares().contains(location));
    }

    /** One leg between two successive stops. */
    public static final class Leg {

        private final TradeRouteStop from;
        private final TradeRouteStop to;
        private final List<LegPath> paths;
        private final TradeRoutePreviewProblem problem;

        Leg(final TradeRouteStop from, final TradeRouteStop to,
                final List<LegPath> paths,
                final TradeRoutePreviewProblem problem) {
            this.from = Preconditions.checkNotNull(from);
            this.to = Preconditions.checkNotNull(to);
            this.paths = ImmutableList.copyOf(paths);
            Preconditions.checkArgument(
                    this.paths.isEmpty() == (problem != null),
                    "A leg has either paths or a problem");
            this.problem = problem;
        }

        public TradeRouteStop getFrom() {
            return from;
        }

        public TradeRouteStop getTo() {
            return to;
        }

        /** @return one path per reachable shown transport, land first */
        public List<LegPath> getPaths() {
            return paths;
        }

        /** @return why no path is shown, empty when a path exists */
        public Optional<TradeRoutePreviewProblem> getProblem() {
            return Optional.ofNullable(problem);
        }
    }

    /**
     * One cheapest legal path. Its squares run from the start square to the end
     * square, both included; a leg to Europe ends at the high-sea square where
     * the ship departs, and a leg from Europe starts at the square where the
     * ship returns.
     */
    public static final class LegPath {

        private final TradeRouteKind kind;
        private final List<Location> squares;
        private final int costThirds;

        LegPath(final TradeRouteKind kind, final List<Location> squares,
                final int costThirds) {
            this.kind = Preconditions.checkNotNull(kind);
            this.squares = ImmutableList.copyOf(squares);
            this.costThirds = costThirds;
        }

        public TradeRouteKind getKind() {
            return kind;
        }

        public List<Location> getSquares() {
            return squares;
        }

        /** @return total entry cost of the steps, in thirds of a point */
        public int getCostThirds() {
            return costThirds;
        }

        /**
         * @param other required path
         * @return whether the other path has the same transport and the same
         *         squares in either direction
         */
        public boolean isSameConnection(final LegPath other) {
            return kind == other.kind && (squares.equals(other.squares)
                    || squares.equals(Lists.reverse(other.squares)));
        }
    }
}
