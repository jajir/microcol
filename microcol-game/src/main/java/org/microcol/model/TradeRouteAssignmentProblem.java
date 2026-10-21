package org.microcol.model;

/**
 * Why a carrier cannot take a trade route assignment now. The first failing
 * check is reported; nothing is changed when any problem exists.
 */
public enum TradeRouteAssignmentProblem {
    NOT_OWNER, NOT_CARRIER, CARRIER_BUSY, ROUTE_UNAVAILABLE, STOP_UNAVAILABLE,
    STOP_NOT_REACHABLE, TURN_BUSY, NO_ASSIGNMENT
}
