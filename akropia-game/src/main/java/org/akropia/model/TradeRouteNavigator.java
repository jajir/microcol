package org.akropia.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.akropia.model.unit.Ship;
import org.akropia.model.unit.UnitWithCargo;

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
        final Optional<TradeRoutePause> previousPause = carrier
                .getTradeRouteAssignment()
                .flatMap(TradeRouteAssignment::getPause);
        final boolean wasPaused = isPaused(carrier);
        final List<Location> walked = new ArrayList<>();
        // A full lap over the longer of the accepted and the saved stops.
        final int lap = Math.max(
                carrier.getTradeRouteAssignment().orElseThrow().getStops()
                        .size(),
                model.findTradeRoute(carrier.getTradeRouteAssignment()
                        .orElseThrow().getRouteId())
                        .map(route -> route.getStops().size()).orElse(0));
        int idle = 0;
        while (idle <= lap) {
            final Progress progress = advanceOnce(carrier, walked);
            if (progress == Progress.STOPPED) {
                break;
            }
            idle = progress == Progress.MOVED ? 0 : idle + 1;
        }
        if (!walked.isEmpty()) {
            model.fireUnitMovedFinished(carrier, Path.of(walked));
        }
        final Optional<TradeRouteAssignment> finalAssignment = carrier
                .getTradeRouteAssignment();
        final Optional<TradeRoutePause> finalPause = finalAssignment
                .flatMap(TradeRouteAssignment::getPause);
        // Waiting for goods is the route's own plan, shown on the route
        // screen, not a problem reported as a turn notice.
        if (carrier.isShip() && finalPause.isPresent()
                && !finalPause.get().isWaiting()
                && !finalPause.equals(previousPause)) {
            final TradeRouteAssignment assignment = finalAssignment
                    .orElseThrow();
            final TradeRouteStop target = assignment.getStops()
                    .get(assignment.getStopIndex());
            final String destination = target.isEurope() ? "Home port"
                    : model.getColonyById(target.getColonyId())
                            .map(Colony::getName)
                            .orElse("#" + target.getColonyId());
            model.addTurnEvent(org.akropia.model.turnevent.TurnEventProvider
                    .getGoToOrder(carrier.getOwner(),
                            org.akropia.model.turnevent.TurnEventGoTo.Kind
                                    .valueOf("ROUTE_"
                                            + finalPause.orElseThrow().name()),
                            carrier.getId(), carrier.getType(), destination)
                    .destination(
                            target.isEurope() ? null : target.getColonyId(),
                            target.isEurope()));
        }
        if (wasPaused != isPaused(carrier)) {
            model.fireActionWasStarted(carrier);
        }
    }

    /**
     * Approach cost uses the same explored paths as route travel, without
     * moving.
     */
    java.util.OptionalInt approachCost(final AbstractUnit carrier,
            final TradeRouteStop stop) {
        if (!carrier.isAtPlaceLocation() && !carrier.isAtEuropePort()) {
            return java.util.OptionalInt.empty();
        }
        if (stop.isEurope() && carrier.isAtEuropePort()) {
            return java.util.OptionalInt.of(0);
        }
        final Optional<Location> origin = carrier.isAtEuropePort()
                ? returnOrigin((Ship) carrier)
                : Optional.of(carrier.getLocation());
        if (origin.isEmpty()) {
            return java.util.OptionalInt.empty();
        }
        final MovementProfile profile = MovementProfile.of(carrier);
        final PathFinder finder = new PathFinder(profile, true,
                carrier.getOwner()::isVisible);
        final List<Location> path;
        if (stop.isEurope()) {
            if (rules.isDepartureSquare(origin.get())) {
                return java.util.OptionalInt.of(0);
            }
            path = finder.findNearest(origin.get(), rules::isDepartureSquare);
        } else {
            final Optional<Colony> colony = model
                    .getColonyById(stop.getColonyId());
            if (colony.isEmpty()) {
                return java.util.OptionalInt.empty();
            }
            if (origin.get().equals(colony.get().getLocation())) {
                return java.util.OptionalInt.of(0);
            }
            path = finder.find(origin.get(), colony.get().getLocation(), false);
        }
        if (path == null) {
            return java.util.OptionalInt.empty();
        }
        int cost = 0;
        Location previous = origin.get();
        for (final Location next : path) {
            cost += profile.getEntryCostThirds(previous, next);
            previous = next;
        }
        return java.util.OptionalInt.of(cost);
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
                && (carrier.getLocation().equals(colonyLocation) || carrier
                        .findKnownGoToRoute(colonyLocation).isPresent());
    }

    /**
     * Whether the carrier has not started the leg to its current stop: it waits
     * with a problem, so a repair applies, or it still stands where it
     * processed its last stop of this route. Once it has moved on, its visit
     * receipt is gone and the leg it accepted is kept to the next stop. A
     * carrier that waits for goods at its stop keeps the instructions of that
     * visit; the edit reaches it at its next stop.
     *
     * @param carrier    required assigned carrier
     * @param assignment required assignment of that carrier
     * @return whether an edit of the route now reaches this carrier
     */
    static boolean takesLatestPlan(final AbstractUnit carrier,
            final TradeRouteAssignment assignment) {
        if (assignment.getPause().filter(TradeRoutePause::isWaiting)
                .isPresent()) {
            return false;
        }
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
                        route.get().getKind())) {
            return pause(assignment.get(), TradeRoutePause.NO_ROUTE);
        }
        // A carrier that has not started its leg takes the saved route as it
        // now stands; one already on its way keeps the leg it accepted.
        if (takesLatestPlan(carrier, assignment.get())) {
            assignment.get()
                    .setStopIndex(remapTarget(assignment.get().getStops(),
                            assignment.get().getStopIndex(),
                            route.get().getStops()));
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
        // Explored squares only, as legality and the map preview use.
        return walk(carrier, assignment.get(),
                carrier.findKnownGoToRoute(square), walked);
    }

    /**
     * Processes the stop once for this place and moves the assignment to the
     * next ordered stop. A visit already marked here, possibly for a coincident
     * stop, is not repeated. In waiting mode an unfinished stop keeps the
     * carrier there with its reason and the instructions of this visit; it is
     * processed again at most once in each later owner turn, from the cargo,
     * stock and treasury that remain.
     */
    private Progress reach(final AbstractUnit carrier, final TradeRoute route,
            final TradeRouteStop stop, final TradeRouteAssignment assignment) {
        final int index = assignment.getStopIndex();
        final int turn = model.getCalendar().getNumberOfPlayedTurns();
        final Optional<TradeRouteVisit> visit = carrier
                .getProcessedTradeRouteVisit();
        final boolean waiting = assignment.getPause()
                .filter(TradeRoutePause::isWaiting).isPresent();
        List<TradeRouteTransfer> transfers = null;
        if (visit.isEmpty()) {
            transfers = new TradeRouteStopProcessor(model,
                    (UnitWithCargo) carrier, route, index, stop,
                    new TradeRouteVisit(route.getId(),
                            stop.isEurope() ? null : carrier.getLocation(),
                            turn),
                    true).run().getTransfers();
        } else if (waiting && visit.get().getRouteId() == route.getId()
                && visit.get().isKindOf(stop) && visit.get().getTurn() < turn) {
            // The waiting carrier stands at the place of its receipt; the
            // receipt is renewed for this turn before anything is transferred.
            final TradeRouteVisit renewed = visit.get().renewedIn(turn);
            carrier.renewProcessedTradeRouteVisit(renewed);
            transfers = new TradeRouteStopProcessor(model,
                    (UnitWithCargo) carrier, route, index, stop, renewed, true)
                    .run().getTransfers();
        }
        // A place passed under an earlier receipt, for example after a
        // reassignment, is not processed now and so is not waited at either.
        if (route.getMode() == TradeRouteMode.WAIT_FOR_COMPLETION
                && (transfers != null || waiting)) {
            final Optional<TradeRoutePause> reason = TradeRouteStopProcessor
                    .getWaitingReason((UnitWithCargo) carrier, stop,
                            transfers == null ? List.of() : transfers);
            if (reason.isPresent()) {
                // Without processing in this turn, the reason found at the
                // last processing still says what the carrier waits for.
                if (transfers == null && waiting) {
                    return Progress.STOPPED;
                }
                // The visit keeps its instructions while the carrier waits.
                final List<TradeRouteStop> snapshot = new ArrayList<>(
                        assignment.getStops());
                snapshot.set(index, stop);
                assignment.setStops(snapshot);
                assignment.setPause(reason.get());
                return Progress.STOPPED;
            }
        }
        assignment.setStopIndex(
                nextIndex(assignment.getStops(), index, route.getStops()));
        // The next leg starts at this boundary, so it takes the route as saved
        // now, including edits made while this carrier was on the way.
        assignment.setStops(route.getStops());
        assignment.setPause(null);
        return Progress.REACHED;
    }

    /**
     * Index in the saved stops of the stop that follows a processed one. A
     * processed stop replaced since is followed by the stop after its
     * replacement; one removed since is followed by the stop now at its place.
     *
     * @param accepted required stops the carrier accepted
     * @param index    index of the processed stop in {@code accepted}
     * @param saved    required stops as saved now
     * @return index in {@code saved}
     */
    static int nextIndex(final List<TradeRouteStop> accepted, final int index,
            final List<TradeRouteStop> saved) {
        if (accepted.equals(saved)) {
            return (index + 1) % saved.size();
        }
        final int at = index < saved.size()
                && saved.get(index).equals(accepted.get(index)) ? index
                        : saved.indexOf(accepted.get(index));
        if (at >= 0) {
            return (at + 1) % saved.size();
        }
        if (accepted.size() == saved.size()) {
            return (index + 1) % saved.size();
        }
        return Math.min(index, saved.size() - 1);
    }

    /**
     * Index in the saved stops of the stop a carrier that has not started its
     * leg heads to: the stop after the one it came from when that one is still
     * saved, otherwise the same stop, otherwise the stop now at the same place
     * in the order.
     *
     * @param accepted required stops the carrier accepted
     * @param index    index of its current stop in {@code accepted}
     * @param saved    required stops as saved now
     * @return index in {@code saved}
     */
    static int remapTarget(final List<TradeRouteStop> accepted, final int index,
            final List<TradeRouteStop> saved) {
        if (accepted.equals(saved)) {
            return index;
        }
        final TradeRouteStop target = accepted.get(index);
        final TradeRouteStop previous = accepted
                .get((index + accepted.size() - 1) % accepted.size());
        final int from = saved.indexOf(previous);
        if (from >= 0 && !previous.equals(target)) {
            return (from + 1) % saved.size();
        }
        final int same = saved.indexOf(target);
        return same >= 0 ? same : Math.min(index, saved.size() - 1);
    }

    /**
     * The stop an assigned carrier is heading to or standing at, without
     * changing its assignment.
     */
    static TradeRouteStop headingStop(final AbstractUnit carrier,
            final TradeRouteAssignment assignment, final TradeRoute route) {
        if (takesLatestPlan(carrier, assignment)) {
            return route.getStops().get(remapTarget(assignment.getStops(),
                    assignment.getStopIndex(), route.getStops()));
        }
        return legStop(route, assignment);
    }

    /**
     * The stop the current leg targets. Its identity is the one the carrier
     * accepted at its last boundary, so an edit never redirects a leg in
     * progress. Its cargo policy is the saved route's while the identity is
     * still saved, so a cargo edit applies when the carrier reaches the stop. A
     * carrier waiting for goods keeps the instructions of its visit.
     */
    private static TradeRouteStop legStop(final TradeRoute route,
            final TradeRouteAssignment assignment) {
        final TradeRouteStop accepted = assignment.getStops()
                .get(assignment.getStopIndex());
        if (assignment.getPause().filter(TradeRoutePause::isWaiting)
                .isPresent()) {
            return accepted;
        }
        final int index = assignment.getStopIndex();
        if (index < route.getStops().size()
                && route.getStops().get(index).equals(accepted)) {
            return route.getStops().get(index);
        }
        final int saved = route.getStops().indexOf(accepted);
        return saved >= 0 ? route.getStops().get(saved) : accepted;
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
     * Cheapest route over explored squares to the nearest departure square. It
     * is one search of the shared finder, so a step costs the same as any
     * go-to, and the map preview finds the same departure.
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

    /**
     * Square a ship in or bound for Europe returns to: its recorded departure
     * square, or the default arrival square when it never left the map. The
     * route preview starts its return leg here.
     */
    Optional<Location> returnOrigin(final Ship ship) {
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
