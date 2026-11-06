package org.akropia.model;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Receipt of one carrier's processed route stop: the route it followed, the
 * place where it processed the stop and the played turn of that processing. The
 * place, a map square or the Europe port, identifies the stop; a list position
 * would change when the route's stops are edited, while the place stays true
 * until the carrier really leaves it. So Save and Continue, a repeated command,
 * an edited, shortened or reordered route, a cancellation, a reassignment and a
 * repair in the same port cannot repeat the stop, while a later departure and
 * arrival can be processed again. A carrier that waits at the stop processes it
 * again at most once per later owner turn, from the remaining cargo, stock and
 * treasury.
 */
public final class TradeRouteVisit {

    private final int routeId;
    /** Map square of a colony stop; {@code null} for the Europe port. */
    private final Location location;
    /** Played-turn number when the stop was last processed. */
    private final int turn;

    TradeRouteVisit(final int routeId, final Location location,
            final int turn) {
        Preconditions.checkArgument(routeId >= 0 && turn >= 0,
                "Invalid trade route visit");
        this.routeId = routeId;
        this.location = location;
        this.turn = turn;
    }

    public int getRouteId() {
        return routeId;
    }

    /** @return the map square, or empty when the visit was in Europe */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    /** @return played-turn number of the last processing of this visit */
    public int getTurn() {
        return turn;
    }

    /**
     * @param laterTurn played-turn number of a later processing
     * @return the same visit processed again in that turn
     */
    TradeRouteVisit renewedIn(final int laterTurn) {
        Preconditions.checkArgument(laterTurn > turn,
                "A visit is processed at most once per turn");
        return new TradeRouteVisit(routeId, location, laterTurn);
    }

    /**
     * @param place map square, or {@code null} for the Europe port
     * @return whether this visit happened at that place
     */
    boolean isAt(final Location place) {
        return Objects.equals(location, place);
    }

    /**
     * @param stop required route stop
     * @return whether the stop is the place of this visit: the Europe port for
     *         a Europe visit, otherwise a colony stop; the caller checks that
     *         the carrier stands on that colony's square
     */
    boolean isKindOf(final TradeRouteStop stop) {
        return stop.isEurope() == (location == null);
    }
}
