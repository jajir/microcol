package org.akropia.model;

/**
 * Route-wide rule for how long a carrier stays at a stop. Both modes unload
 * before they load and handle goods in the fixed colony goods order.
 */
public enum TradeRouteMode {
    /**
     * Make the transfers available now and continue as soon as movement allows.
     */
    TOUCH_AND_GO,
    /**
     * Stay at the stop across turns until the goods marked for unloading are
     * gone and the free capacity is filled with goods marked for loading.
     */
    WAIT_FOR_COMPLETION
}
