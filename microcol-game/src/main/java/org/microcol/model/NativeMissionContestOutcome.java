package org.microcol.model;

/**
 * Actual result of one confirmed rival-mission contest (R-0165). Exactly one
 * missionary dies in either result; nothing else changes.
 */
public enum NativeMissionContestOutcome {

    /**
     * The challenging missionary wins: the resident dies and the challenger
     * takes the mission with its clock reset to zero.
     */
    SUCCESS,

    /**
     * The challenging missionary dies; the resident and its mission and clock
     * remain as they were.
     */
    FAILURE

}
