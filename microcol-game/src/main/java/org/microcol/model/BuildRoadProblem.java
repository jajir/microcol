package org.microcol.model;

/**
 * Reason why a unit cannot start building a road at its current square.
 */
public enum BuildRoadProblem {

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

    /** The square is water, where no road can be built. */
    SEA,

    /** The square already has a road. */
    ALREADY_ROAD
}
