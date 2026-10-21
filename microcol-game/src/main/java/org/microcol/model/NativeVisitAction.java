package org.microcol.model;

/**
 * Action a visitor may choose at a native settlement it has learned. The
 * chooser lists every action; each one's effect belongs to its own owner.
 */
public enum NativeVisitAction {

    /** Visit the chief for information, gold or a risk of death. */
    SCOUT_CHIEF,

    /** Learn the settlement's offered skill over several owner updates. */
    TEACHING,

    /** Trade from a loaded wagon or coastal ship. */
    TRADE,

    /** Demand tribute with armed force. */
    TRIBUTE,

    /** Establish a missionary mission. */
    MISSION,

    /** Attack the settlement with armed force. */
    ATTACK

}
