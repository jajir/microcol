package org.microcol.model;

/**
 * What happened to one configured goods entry of a processed route stop. The
 * names say what limited the transfer, so the player can tell a finite no-op
 * from a rejection.
 */
public enum TradeRouteTransferReason {
    /**
     * Everything on board (unload), all warehouse stock (load from a colony) or
     * a filled hold (load in Europe) was moved; nothing limited it.
     */
    MOVED,
    /** Unload: the destination took only part of what was on board. */
    PARTIAL_DESTINATION_FULL,
    /** Load: the holds filled up before the warehouse stock was exhausted. */
    PARTIAL_HOLD_FULL,
    /** Load: the treasury ran out before the hold was filled. */
    PARTIAL_FUNDS,
    /** Unload: the carrier held none of this goods. */
    NOTHING_ON_BOARD,
    /** Unload: the colony warehouse had no free room. */
    DESTINATION_FULL,
    /** Load: no hold had room for this goods. */
    HOLD_FULL,
    /** Load: the colony warehouse held none of this goods. */
    SOURCE_EMPTY,
    /** Load in Europe: not even one unit was affordable. */
    INSUFFICIENT_FUNDS,
    /** Europe: the player no longer trades there. */
    EUROPE_CLOSED,
    /** Europe: this goods is boycotted until its arrears are paid. */
    BOYCOTTED,
    /** A transfer rule refused this entry; its detail text says why. */
    REJECTED,
    /** An earlier entry was rejected, so this one was not attempted. */
    NOT_ATTEMPTED
}
