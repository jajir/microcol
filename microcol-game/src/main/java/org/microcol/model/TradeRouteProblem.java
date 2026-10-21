package org.microcol.model;

/**
 * Why a trade route cannot be created now. The first failing check is reported;
 * nothing is changed when any problem exists.
 */
public enum TradeRouteProblem {
    NAME_EMPTY, NAME_TOO_LONG, NAME_INVALID, NAME_DUPLICATE, NOT_OWNER,
    ID_EXHAUSTED, STOP_COUNT, START_UNAVAILABLE, START_NOT_PORT,
    STOP_UNAVAILABLE, EUROPE_BY_LAND, SAME_STOP, STOP_NOT_REACHABLE,
    ROUTE_UNAVAILABLE, TURN_BUSY
}
