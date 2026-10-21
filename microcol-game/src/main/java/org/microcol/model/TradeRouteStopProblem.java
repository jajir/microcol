package org.microcol.model;

/**
 * Why a carrier's route stop was not processed. The first failing check is
 * reported; nothing is changed when any problem exists.
 */
public enum TradeRouteStopProblem {
    /** Not a live, owned, undamaged carrier of the route's kind at the stop. */
    CARRIER_UNAVAILABLE,
    /**
     * Route is not a stored route of the carrier's owner, or is out of date.
     */
    ROUTE_UNAVAILABLE,
    /** Stop index is out of range, or the carrier is not at that stop. */
    STOP_UNAVAILABLE,
    /** The carrier already processed a stop and has not left it since. */
    VISIT_ALREADY_PROCESSED,
    /** Turn preparation is still running, so no trade can happen. */
    TURN_BUSY
}
