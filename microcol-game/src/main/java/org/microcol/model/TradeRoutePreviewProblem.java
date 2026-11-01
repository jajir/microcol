package org.microcol.model;

/** Why a leg of a trade route preview has no path to show. */
public enum TradeRoutePreviewProblem {
    /** A colony stop is no longer an owned colony. */
    STOP_LOST,
    /** A ship needs a coastal port at a colony stop. */
    NOT_PORT,
    /** A wagon cannot travel to or from Europe. */
    EUROPE_BY_LAND,
    /** The explored map has no legal path for any shown transport. */
    NO_PATH
}
