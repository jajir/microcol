package org.microcol.model;

/**
 * Reason a player cannot declare independence right now. Every constant is a
 * model rule; the interface only translates it.
 */
public enum IndependenceProblem {

    /** The player is not a registered colonial nation. */
    UNAVAILABLE,

    /** Independence was already declared. */
    ALREADY_DECLARED,

    /** Spring 1800 or later: the declaration window is closed. */
    TOO_LATE,

    /** The nation owns no coastal colony. */
    NO_COASTAL_COLONY,

    /** Exact national support is below 50 percent. */
    INSUFFICIENT_SUPPORT
}
