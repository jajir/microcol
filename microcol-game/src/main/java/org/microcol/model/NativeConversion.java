package org.microcol.model;

/**
 * Shared rules of a native convert (R-0149): the chance of one conversion
 * event, the expiry of an unassigned convert and the bounded job modifiers.
 */
public final class NativeConversion {

    /** Ordinary missionary chance of one conversion event, in percent. */
    public static final int ORDINARY_CHANCE_PERCENT = 10;

    /** Expert missionary chance of one conversion event, in percent. */
    public static final int EXPERT_CHANCE_PERCENT = 20;

    /** Unassigned owner updates after which a convert returns to its tribe. */
    public static final int RETURN_AFTER_UNASSIGNED_UPDATES = 8;

    /** Output bonus of a convert on an outdoor job with positive base yield. */
    public static final int OUTDOOR_BONUS = 1;

    /** Output penalty of a convert on an indoor non-manufacturing job. */
    public static final int INDOOR_PENALTY = 1;

    private NativeConversion() {
    }

    /**
     * @param expert    whether the mission's missionary acts as an expert
     * @param sepulveda whether the owner holds Juan de Sepulveda
     * @return the chance of one conversion, as a fraction capped at one
     */
    public static double chance(final boolean expert, final boolean sepulveda) {
        final int percent = expert ? EXPERT_CHANCE_PERCENT
                : ORDINARY_CHANCE_PERCENT;
        return Math.min(1.0, (sepulveda ? 2 : 1) * percent / 100.0);
    }
}
