package org.microcol.model;

/**
 * Reason why a unit cannot start clearing the forest at its current square.
 */
public enum ClearForestProblem {

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

    /** The square has no forest, because it is another terrain or is clear. */
    NO_FOREST
}
