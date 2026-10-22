package org.microcol.model;

/**
 * Why a trade with an adjacent foreign colony is not legal now (R-0167). The
 * first failing condition is reported, and {@link #NONE} means the trade may be
 * confirmed. Access problems are checked before goods, so the player sees the
 * most basic reason first.
 */
public enum ForeignColonyTradeProblem {
    /** The trade is legal now. */
    NONE,
    /**
     * Not the current colonial owner, not adjacent, or not a registered
     * carrier.
     */
    NO_ACCESS,
    /** The visiting nation has not recruited Jan de Witt. */
    FOUNDER_ABSENT,
    /** The nations are at war, so the visit cannot trade. */
    AT_WAR,
    /** A damaged ship has no accessible cargo. */
    DAMAGED_CARRIER,
    /** The carrier holds no goods to offer. */
    EMPTY_CARGO,
    /** Barter needs two different commodities. */
    SAME_GOODS,
    /** A quantity is not positive or the carrier does not hold it. */
    INVALID_QUANTITY,
    /** The colony warehouse cannot store the incoming goods. */
    HOST_CAPACITY,
    /** The colony warehouse does not hold the outgoing goods. */
    HOST_STOCK,
    /** The carrier has no room for the outgoing goods. */
    CARRIER_CAPACITY,
    /** The colony owner cannot pay the net gold. */
    HOST_GOLD,
    /** The visiting nation cannot pay the net gold. */
    VISITOR_GOLD
}
