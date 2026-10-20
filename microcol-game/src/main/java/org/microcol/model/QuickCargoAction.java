package org.microcol.model;

/**
 * Direction of one quick cargo transfer between a colony and its carrier.
 */
public enum QuickCargoAction {

    /** Move the best-valued stock from the colony warehouse into a hold. */
    LOAD,

    /** Move the first goods hold into the colony warehouse. */
    UNLOAD

}
