package org.microcol.model;

/**
 * Result of the one chief encounter of a visiting European nation with a native
 * settlement (R-0158). It is drawn once and kept in the settlement's record.
 */
public enum NativeChiefOutcome {

    /** The chief tells about the settlement: its goods, skill and terrain. */
    INFORMATION,

    /** The settlement pays gold from its treasury to the visiting nation. */
    GOLD,

    /** The visiting scout is killed and leaves the game. */
    DEATH

}
