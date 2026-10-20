package org.microcol.model;

/**
 * Why a quick cargo action is not available now. A refused action changes
 * nothing.
 */
public enum QuickCargoProblem {

    /**
     * The carrier is missing, stale, damaged, off the colony port, not owned by
     * the current player, or the colony is not available to it.
     */
    UNAVAILABLE,

    /** No goods that quick loading may move are in the colony warehouse. */
    NO_STOCK,

    /** Some goods are in stock, but no hold can receive any of them. */
    NO_FREE_HOLD,

    /** The carrier has no goods hold; passengers are never unloaded here. */
    NO_GOODS,

    /** The warehouse has no free room for the first goods hold. */
    WAREHOUSE_FULL

}
