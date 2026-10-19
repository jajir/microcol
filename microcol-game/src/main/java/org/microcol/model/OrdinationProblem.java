package org.microcol.model;

/** Why a colonist cannot become a missionary right now. */
public enum OrdinationProblem {
    /** Stale or foreign unit, not the owner's turn, or a foreign colony. */
    UNAVAILABLE,
    /** The colonist already has the missionary role or is a Jesuit. */
    ALREADY_MISSIONARY,
    /** The colonist works a field or building and must leave that job first. */
    ASSIGNED_WORKER,
    /**
     * The colonist has another order, such as sentry, fortify or pioneer work.
     */
    ACTIVE_ORDER,
    /**
     * The colonist is neither on the European pier nor on an owned colony tile.
     */
    NOT_IN_EUROPE_OR_COLONY,
    /** The colony has no church or cathedral; a chapel alone is not enough. */
    NO_CHURCH
}
