package org.microcol.model;

/**
 * Reason why the selected unit cannot be disbanded now.
 */
public enum DisbandProblem {

    /** The unit is missing, stale or no longer registered in this game. */
    UNAVAILABLE,

    /** The game is not running, a turn is being prepared or it is ending. */
    WRONG_PHASE,

    /** The unit belongs to another player than the current one. */
    NOT_OWNER,

    /** The ship is at sea on an Atlantic crossing. */
    VOYAGE,

    /** The ship is in a repair dock. */
    IN_REPAIR,

    /** The passenger is aboard a carrier; it must be unloaded first. */
    ABOARD_CARRIER,

    /** A ship or wagon still holds passengers or goods. */
    LOADED_CARRIER,

    /** The colony that would receive the equipment is no longer the owner's. */
    RECEIVING_COLONY_INVALID,

    /** The unit is the last resident; leaving would destroy the colony. */
    LAST_COLONY_RESIDENT,

    /** The colony's minimum resident rule keeps this unit in place. */
    MINIMUM_RESIDENTS
}
