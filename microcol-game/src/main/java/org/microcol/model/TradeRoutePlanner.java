package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * Computes {@link TradeRoutePreview} paths with the finder, costs and explored
 * squares that {@link TradeRouteNavigator} walks with. Without a carrier the
 * existing wagon and galleon type profiles are tried; no unit is created, moved
 * or numbered, and nothing in the model changes.
 */
final class TradeRoutePlanner {

    private final Model model;
    private final TradeRouteRules rules;
    private final TradeRouteNavigator navigator;

    TradeRoutePlanner(final Model model, final TradeRouteRules rules,
            final TradeRouteNavigator navigator) {
        this.model = Preconditions.checkNotNull(model);
        this.rules = Preconditions.checkNotNull(rules);
        this.navigator = Preconditions.checkNotNull(navigator);
    }

    /** One way of moving that a preview tries for every leg. */
    private static final class Transport {
        private final TradeRouteKind kind;
        private final MovementProfile profile;

        private Transport(final TradeRouteKind kind,
                final MovementProfile profile) {
            this.kind = kind;
            this.profile = profile;
        }
    }

    /**
     * @param owner required route owner
     * @param route required route as saved now
     * @return the preview of every leg of the route
     */
    TradeRoutePreview preview(final Player owner, final TradeRoute route) {
        // The lowest-ID assigned carrier of the route's kind stands for all;
        // each carrier still walks by its own rules.
        final Optional<UnitWithCargo> carrier = model
                .getTradeRouteAssignedCarriers(owner, route).stream()
                .filter(unit -> Model.isTradeRouteCarrierKind(unit,
                        route.getKind()))
                .findFirst();
        final List<Transport> transports = new ArrayList<>();
        if (carrier.isPresent()) {
            // The carrier's own step rules, exactly those its travel uses.
            transports.add(new Transport(route.getKind(),
                    MovementProfile.of(carrier.get())));
        } else {
            transports.add(new Transport(TradeRouteKind.LAND,
                    MovementProfile.of(model, UnitType.WAGON, owner)));
            transports.add(new Transport(TradeRouteKind.SEA,
                    MovementProfile.of(model, UnitType.GALLEON, owner)));
        }
        final List<TradeRoutePreview.Leg> legs = new ArrayList<>();
        for (final List<TradeRouteStop> pair : legPairs(route)) {
            legs.add(leg(owner, pair.get(0), pair.get(1), transports,
                    carrier.orElse(null)));
        }
        return new TradeRoutePreview(route, carrier.orElse(null), legs);
    }

    /**
     * The leg from the starting colony to the first stop, the legs between
     * successive stops, and the leg back from the last stop to the first one,
     * as a repeating carrier travels them. A stop pair is listed once.
     */
    private static List<List<TradeRouteStop>> legPairs(final TradeRoute route) {
        final List<TradeRouteStop> stops = route.getStops();
        final List<List<TradeRouteStop>> pairs = new ArrayList<>();
        addPair(pairs, TradeRouteStop.ofColony(route.getStartColonyId()),
                stops.get(0));
        for (int index = 0; index + 1 < stops.size(); index++) {
            addPair(pairs, stops.get(index), stops.get(index + 1));
        }
        if (stops.size() > 1) {
            addPair(pairs, stops.get(stops.size() - 1), stops.get(0));
        }
        return pairs;
    }

    private static void addPair(final List<List<TradeRouteStop>> pairs,
            final TradeRouteStop from, final TradeRouteStop to) {
        final List<TradeRouteStop> pair = List.of(from, to);
        if (!from.equals(to) && !pairs.contains(pair)) {
            pairs.add(pair);
        }
    }

    private TradeRoutePreview.Leg leg(final Player owner,
            final TradeRouteStop from, final TradeRouteStop to,
            final List<Transport> transports, final UnitWithCargo carrier) {
        final List<TradeRoutePreview.LegPath> paths = new ArrayList<>();
        final List<TradeRoutePreviewProblem> problems = new ArrayList<>();
        for (final Transport transport : transports) {
            final Optional<TradeRoutePreviewProblem> problem = endsProblem(
                    owner, transport.kind, from, to);
            if (problem.isPresent()) {
                problems.add(problem.get());
                continue;
            }
            final Optional<TradeRoutePreview.LegPath> path = path(owner,
                    transport, from, to, carrier);
            if (path.isPresent()) {
                paths.add(path.get());
            } else {
                problems.add(TradeRoutePreviewProblem.NO_PATH);
            }
        }
        if (!paths.isEmpty()) {
            return new TradeRoutePreview.Leg(from, to, paths, null);
        }
        // One transport explains itself; with both, a lost stop is the reason
        // for each, otherwise neither found a way.
        final TradeRoutePreviewProblem problem = problems.size() == 1
                ? problems.get(0)
                : problems.contains(TradeRoutePreviewProblem.STOP_LOST)
                        ? TradeRoutePreviewProblem.STOP_LOST
                        : TradeRoutePreviewProblem.NO_PATH;
        return new TradeRoutePreview.Leg(from, to, List.of(), problem);
    }

    private Optional<TradeRoutePreviewProblem> endsProblem(final Player owner,
            final TradeRouteKind kind, final TradeRouteStop from,
            final TradeRouteStop to) {
        for (final TradeRouteStop stop : List.of(from, to)) {
            if (!stop.isEurope() && model.getColonyById(stop.getColonyId())
                    .filter(Colony::isValid)
                    .filter(colony -> colony.getOwner() == owner).isEmpty()) {
                return Optional.of(TradeRoutePreviewProblem.STOP_LOST);
            }
        }
        if (kind == TradeRouteKind.LAND && (from.isEurope() || to.isEurope())) {
            return Optional.of(TradeRoutePreviewProblem.EUROPE_BY_LAND);
        }
        for (final TradeRouteStop stop : List.of(from, to)) {
            if (!stop.isEurope()
                    && rules.colonySquare(owner, kind, stop).isEmpty()) {
                return Optional.of(TradeRoutePreviewProblem.NOT_PORT);
            }
        }
        return Optional.empty();
    }

    /**
     * Cheapest legal path of one transport over squares the owner has explored.
     * A ship never plans a departure step on the way; a leg to Europe ends at
     * the cheapest square where it may depart, and a leg from Europe starts at
     * the square the ship returns to.
     */
    private Optional<TradeRoutePreview.LegPath> path(final Player owner,
            final Transport transport, final TradeRouteStop from,
            final TradeRouteStop to, final UnitWithCargo carrier) {
        final Optional<Location> start = from.isEurope() ? returnOrigin(carrier)
                : rules.colonySquare(owner, transport.kind, from);
        if (start.isEmpty() || !owner.isVisible(start.get())) {
            return Optional.empty();
        }
        final PathFinder finder = new PathFinder(transport.profile, true,
                owner::isVisible);
        final List<Location> steps;
        if (to.isEurope()) {
            steps = rules.isDepartureSquare(start.get()) ? List.of()
                    : finder.findNearest(start.get(), rules::isDepartureSquare);
        } else {
            final Location goal = rules.colonySquare(owner, transport.kind, to)
                    .orElseThrow();
            steps = goal.equals(start.get()) ? List.of()
                    : finder.find(start.get(), goal, false);
        }
        if (steps == null) {
            return Optional.empty();
        }
        final List<Location> squares = new ArrayList<>();
        squares.add(start.get());
        squares.addAll(steps);
        int cost = 0;
        for (int index = 1; index < squares.size(); index++) {
            cost += transport.profile.getEntryCostThirds(squares.get(index - 1),
                    squares.get(index));
        }
        return Optional.of(
                new TradeRoutePreview.LegPath(transport.kind, squares, cost));
    }

    /**
     * The assigned ship's real return square, or the default arrival square for
     * a route without a ship.
     */
    private Optional<Location> returnOrigin(final UnitWithCargo carrier) {
        if (carrier instanceof Ship) {
            return navigator.returnOrigin((Ship) carrier);
        }
        return model.getHighSea().getDefaultArrivalLocation();
    }
}
