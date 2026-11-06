package org.akropia.model;

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
    CARRIER_DAMAGED,
    /**
     * Waiting mode: goods marked for unloading are still on board, because the
     * warehouse is full or the home port cannot take them now.
     */
    WAITING_UNLOAD,
    /**
     * Waiting mode: free capacity is left and the stop has no more goods marked
     * for loading.
     */
    WAITING_LOAD,
    /**
     * Waiting mode at the home port: free capacity is left and the treasury
     * cannot pay for more goods marked for buying.
     */
    WAITING_FUNDS;

    /**
     * @return whether the carrier stands at its stop in waiting mode, keeping
     *         the instructions of its current visit
     */
    public boolean isWaiting() {
        return this == WAITING_UNLOAD || this == WAITING_LOAD
                || this == WAITING_FUNDS;
    }
}
