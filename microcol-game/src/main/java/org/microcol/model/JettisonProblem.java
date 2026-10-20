package org.microcol.model;

/** Reasons why an owned ship may not jettison goods at the moment. */
public enum JettisonProblem {

    /** The game, the turn preparation or the ending of the turn forbids it. */
    WRONG_PHASE,

    /** The carrier belongs to a player who is not the current player. */
    NOT_OWNER,

    /** The carrier is not a registered ship that could hold goods. */
    UNAVAILABLE,

    /** The ship is damaged, in a colony dock, in Europe or on land. */
    NOT_AT_SEA,

    /** The ship has no hold with goods, only passengers or empty holds. */
    NO_GOODS

}
