package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * Moves a carrier with a persistent trade route assignment during its owner's
 * preparation. Every step is the carrier's ordinary step under the shared
 * movement rules, every arrival runs {@link TradeRouteStopProcessor} once, and
 * each sea leg uses the real departure into the high sea or the real return
 * from Europe. The navigator reads routes and never changes them.
 */
final class TradeRouteNavigator {

    /** Outcome of one pass over the current stop. */
    private enum Progress {
        /** The current stop was reached and processed or skipped. */
        REACHED,
        /** A step or a departure used movement or started a voyage. */
        MOVED,
        /** Nothing more can happen in this owner update. */
        STOPPED
    }

    private final Model model;
    private final TradeRouteRules rules;

    TradeRouteNavigator(final Model model, final TradeRouteRules rules) {
        this.model = Preconditions.checkNotNull(model);
        this.rules = Preconditions.checkNotNull(rules);
    }

    /**
     * Advances the assignment of one carrier as far as this owner update
     * allows. A stop passed without moving, including one already processed
     * from this place, counts as idle; a full lap of idle stops ends the
     * update, so a single or coincident-stop route cannot spin.
     *
     * @param carrier required carrier of the current owner
     */
    void advance(final AbstractUnit carrier) {
        if (carrier.getTradeRouteAssignment().isEmpty()) {
            return;
        }
        final boolean wasPaused = isPaused(carrier);
        final List<Location> walked = new ArrayList<>();
        int idle = 0;
        while (idle <= TradeRoutes.MAX_STOPS) {
            final Progress progress = advanceOnce(carrier, walked);
            if (progress == Progress.STOPPED) {
                break;
            }
            idle = progress == Progress.MOVED ? 0 : idle + 1;
        }
        if (!walked.isEmpty()) {
            model.fireUnitMovedFinished(carrier, Path.of(walked));
        }
        if (wasPaused != isPaused(carrier)) {
            model.fireActionWasStarted(carrier);
        }
    }

    /**
     * Whether the carrier, standing on the map or in Europe, can reach one stop
     * with the rules that later movement uses. Reads only.
     *
     * @param carrier        required carrier of a valid kind
     * @param stop           required stop of the route
     * @param colonyLocation map square of a colony stop; ignored for Europe
     * @return whether the stop is reachable now
     */
    boolean canReachStop(final AbstractUnit carrier, final TradeRouteStop stop,
            final Location colonyLocation) {
        if (stop.isEurope()) {
            return carrier.isAtEuropePort() || carrier.isAtPlaceLocation()
                    && (rules.isDepartureSquare(carrier.getLocation())
                            || findDepartureRoute(carrier).isPresent());
        }
        if (carrier.isAtEuropePort()) {
            return returnOrigin((Ship) carrier)
                    .map(origin -> rules.isReachable(carrier.getOwner(),
                            TradeRouteKind.SEA, origin, colonyLocation))
                    .orElse(false);
        }
        return carrier.isAtPlaceLocation()
                && (carrier.getLocation().equals(colonyLocation)
                        || carrier.findGoToRoute(colonyLocation).isPresent());
    }

    /**
     * Whether the carrier has not started the leg to its current stop: it waits
     * with a reason, so a repair applies, or it still stands where it processed
     * its last stop of this route. Once it has moved on, its visit receipt is
     * gone and the leg it accepted is kept to the next stop.
     *
     * @param carrier    required assigned carrier
     * @param assignment required assignment of that carrier
     * @return whether an edit of the route now reaches this carrier
     */
    static boolean takesLatestPlan(final AbstractUnit carrier,
            final TradeRouteAssignment assignment) {
        return assignment.getPause().isPresent() || carrier
                .getProcessedTradeRouteVisit()
                .filter(visit -> visit.getRouteId() == assignment.getRouteId())
                .isPresent();
    }

    private static boolean isPaused(final AbstractUnit carrier) {
        return carrier.getTradeRouteAssignment()
                .flatMap(TradeRouteAssignment::getPause).isPresent();
    }

    private Progress advanceOnce(final AbstractUnit carrier,
            final List<Location> walked) {
        final Optional<TradeRouteAssignment> assignment = carrier
                .getTradeRouteAssignment();
        // A voyage or a repair yard waits; the carrier resumes on its own turn.
        if (assignment.isEmpty()) {
            return Progress.STOPPED;
        }
        // A damaged ship shows its reason instead of waiting silently. Other
        // reasons are recomputed on the next pass, so only this one is cleared.
        if (carrier instanceof Ship && ((Ship) carrier).isDamaged()) {
            return pause(assignment.get(), TradeRoutePause.CARRIER_DAMAGED);
        }
        if (assignment.get().getPause()
                .orElse(null) == TradeRoutePause.CARRIER_DAMAGED) {
            assignment.get().setPause(null);
        }
        // A voyage waits for its own countdown; the carrier resumes on arrival.
        if (!carrier.isAtPlaceLocation() && !carrier.isAtEuropePort()) {
            return Progress.STOPPED;
        }
        final Optional<TradeRoute> route = model
                .findTradeRoute(assignment.get().getRouteId());
        if (route.isEmpty() || route.get().getOwner() != carrier.getOwner()
                || !Model.isTradeRouteCarrierKind(carrier,
                        route.get().getKind())
                || assignment.get().getStopIndex() >= route.get().getStops()
                        .size()) {
            return pause(assignment.get(), TradeRoutePause.NO_ROUTE);
        }
        // A carrier that has not started its leg takes the saved route as it
        // now stands; one already on its way keeps the leg it accepted.
        if (takesLatestPlan(carrier, assignment.get())) {
            assignment.get().setStops(route.get().getStops());
        }
        final TradeRouteStop stop = legStop(route.get(), assignment.get());
        final Player owner = carrier.getOwner();
        if (stop.isEurope()) {
            if (!owner.canTradeInEurope()) {
                return pause(assignment.get(), TradeRoutePause.EUROPE_CLOSED);
            }
            if (carrier.isAtEuropePort()) {
                return reach(carrier, route.get(), stop, assignment.get());
            }
            return travelToEurope(carrier, assignment.get(), walked);
        }
        // A Crown-seized ship in the closed home port cannot leave it (R-0187).
        if (carrier.isCrownSeized()) {
            return pause(assignment.get(), TradeRoutePause.EUROPE_CLOSED);
        }
        final Optional<Colony> colony = model.getColonyById(stop.getColonyId())
                .filter(Colony::isValid)
                .filter(candidate -> candidate.getOwner() == owner);
        if (colony.isEmpty()) {
            return pause(assignment.get(), TradeRoutePause.STOP_LOST);
        }
        final Location square = colony.get().getLocation();
        if (carrier.isAtPlaceLocation()
                && carrier.getLocation().equals(square)) {
            return reach(carrier, route.get(), stop, assignment.get());
        }
        if (carrier.isAtEuropePort()) {
            return departFromEurope(carrier, assignment.get(), square);
        }
        return walk(carrier, assignment.get(), carrier.findGoToRoute(square),
                walked);
    }

    /**
     * Processes the stop once for this place and moves the assignment to the
     * next ordered stop. A visit already marked here, possibly for a coincident
     * stop, is not repeated.
     */
    private Progress reach(final AbstractUnit carrier, final TradeRoute route,
            final TradeRouteStop stop, final TradeRouteAssignment assignment) {
        final int index = assignment.getStopIndex();
        if (carrier.getProcessedTradeRouteVisit().isEmpty()) {
            new TradeRouteStopProcessor(model, (UnitWithCargo) carrier, stop,
                    new TradeRouteVisit(route.getId(), index,
                            stop.isEurope() ? null : carrier.getLocation()))
                    .run();
        }
        assignment.setStopIndex((index + 1) % route.getStops().size());
        // The next leg starts at this boundary, so it takes the route as saved
        // now, including edits made while this carrier was on the way.
        assignment.setStops(route.getStops());
        assignment.setPause(null);
        return Progress.REACHED;
    }

    /**
     * The stop the current leg targets. Its identity is the one the carrier
     * accepted at its last boundary, so an edit never redirects a leg in
     * progress. Its cargo policy is the saved route's while the identity is
     * unchanged, so a cargo edit applies when the carrier reaches the stop.
     */
    private static TradeRouteStop legStop(final TradeRoute route,
            final TradeRouteAssignment assignment) {
        final TradeRouteStop accepted = assignment.getStops()
                .get(assignment.getStopIndex());
        final TradeRouteStop current = route.getStops()
                .get(assignment.getStopIndex());
        return accepted.equals(current) ? current : accepted;
    }

    private Progress travelToEurope(final AbstractUnit ship,
            final TradeRouteAssignment assignment,
            final List<Location> walked) {
        for (final Location neighbor : ship.getLocation().getNeighbors()) {
            if (model.getHighSea().isDepartureMove(ship, neighbor)) {
                return step(ship, assignment, neighbor, walked);
            }
        }
        return walk(ship, assignment, findDepartureRoute(ship), walked);
    }

    /**
     * Cheapest route to the nearest departure square the owner can see. It is
     * one search of the shared finder, so a step costs the same as any go-to.
     */
    private Optional<List<Location>> findDepartureRoute(
            final AbstractUnit ship) {
        return ship.findNearestRoute(rules::isDepartureSquare);
    }

    /**
     * Sails the ship from Europe toward its real return square. The return uses
     * the recorded departure square, or the default arrival square of the sea
     * lane when the ship never left; the voyage then runs its countdown.
     */
    private Progress departFromEurope(final AbstractUnit ship,
            final TradeRouteAssignment assignment, final Location goal) {
        final Optional<Location> origin = returnOrigin((Ship) ship);
        if (origin.isEmpty() || !rules.isReachable(ship.getOwner(),
                TradeRouteKind.SEA, origin.get(), goal)) {
            return pause(assignment, TradeRoutePause.STOP_UNREACHABLE);
        }
        if (!ship.getAction().isNoAction()) {
            return Progress.STOPPED;
        }
        assignment.setPause(null);
        ship.placeToHighSeas(false);
        return Progress.MOVED;
    }

    private Optional<Location> returnOrigin(final Ship ship) {
        return ship.getDepartureLocation()
                .or(() -> model.getHighSea().getDefaultArrivalLocation());
    }

    private Progress walk(final AbstractUnit carrier,
            final TradeRouteAssignment assignment,
            final Optional<List<Location>> path, final List<Location> walked) {
        if (path.isEmpty() || path.get().isEmpty()) {
            return pause(assignment, TradeRoutePause.STOP_UNREACHABLE);
        }
        assignment.setPause(null);
        return step(carrier, assignment, path.get().get(0), walked);
    }

    /**
     * One ordinary step. A step the carrier cannot pay for waits for the next
     * owner turn; a step the shared rules refuse pauses the assignment, and the
     * refused step changes nothing.
     */
    private Progress step(final AbstractUnit carrier,
            final TradeRouteAssignment assignment, final Location next,
            final List<Location> walked) {
        if (!carrier.canAffordStepTo(next)) {
            return Progress.STOPPED;
        }
        try {
            carrier.moveOneStep(next);
        } catch (final IllegalStateException
                | IllegalArgumentException refused) {
            return pause(assignment, TradeRoutePause.STOP_UNREACHABLE);
        }
        if (carrier.isAtPlaceLocation()) {
            walked.add(next);
        }
        return Progress.MOVED;
    }

    private static Progress pause(final TradeRouteAssignment assignment,
            final TradeRoutePause reason) {
        assignment.setPause(reason);
        return Progress.STOPPED;
    }
}
