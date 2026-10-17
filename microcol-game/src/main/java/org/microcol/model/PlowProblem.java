package org.microcol.model;

/**
 * Reason why a unit cannot start plowing at its current square.
 */
public enum PlowProblem {

    /** The unit is stale, off the map or not owned by the current player. */
    UNAVAILABLE,

    /** The unit cannot do pioneer work. */
    NOT_PIONEER,

    /** The unit carries fewer than the required tools. */
    NOT_ENOUGH_TOOLS,

    /** The unit already has an order. */
    BUSY,

    /** The unit has no movement left. */
    NO_MOVES,

    /** The square is a colony. */
    COLONY,

    /** The terrain cannot hold a field. */
    UNSUITABLE_TERRAIN,

    /** The square is forest and must be cleared first. */
    FOREST,

    /** The square already has a field. */
    ALREADY_PLOWED
}
