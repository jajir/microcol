package org.microcol.model;

/**
 * Hostility shown for a native settlement toward one European. It is read from
 * the larger of tribal anger and local alarm, so each band is a single shared
 * calculation for the interface and the AI.
 */
public enum NativeHostility {

    CONTENT, UNEASY, ANGRY, HOSTILE;

    /**
     * @param value required measure in 0..100, where tribal anger and local
     *              alarm are compared
     * @return content below 25, uneasy below 50, angry below 75, hostile from
     *         75
     */
    public static NativeHostility of(final double value) {
        if (value < 25) {
            return CONTENT;
        }
        if (value < 50) {
            return UNEASY;
        }
        if (value < 75) {
            return ANGRY;
        }
        return HOSTILE;
    }

}
