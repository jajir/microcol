package org.microcol.model;

/**
 * Why an assigned carrier waits. The reason is a model fact; the interface only
 * shows it and never substitutes another destination.
 */
public enum TradeRoutePause {
    /**
     * The saved route is gone or no longer fits the carrier's owner or kind.
     */
    NO_ROUTE,
    /** The stop cannot be reached now by the shared movement rules. */
    STOP_UNREACHABLE,
    /** The stop colony is gone or no longer belongs to the carrier's owner. */
    STOP_LOST,
    /** The owner may not trade in Europe, so the Europe stop waits. */
    EUROPE_CLOSED,
    /**
     * The carrier is damaged in a repair yard and cannot sail until repaired.
     */
    CARRIER_DAMAGED
}
