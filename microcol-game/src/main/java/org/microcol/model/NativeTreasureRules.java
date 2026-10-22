package org.microcol.model;

import java.util.Optional;
import java.util.function.DoubleSupplier;

import com.google.common.base.Preconditions;

/**
 * Pure rules of the treasure train that one native conquest may leave (R-0175).
 * The model takes the saved random draw and creates the unit; this class only
 * maps the conquered settlement and the Congress to the face value. Without
 * Cortes one saved draw in two leaves a train worth 500 gold, or 1000 for a
 * capital. With Cortes exactly one train is left, worth double, and no draw is
 * taken.
 */
public final class NativeTreasureRules {

    /** Base face value of a train from an ordinary settlement, in gold. */
    public static final int ORDINARY_VALUE = 500;

    /** Base face value of a train from a native capital, in gold. */
    public static final int CAPITAL_VALUE = 1000;

    /** Cortes doubles the base face value. */
    public static final int CORTES_FACTOR = 2;

    /** Without Cortes the saved draw leaves a train when it is below this. */
    public static final double CHANCE = 0.5;

    private NativeTreasureRules() {
    }

    /**
     * @param capital whether the conquered settlement is a native capital
     * @param cortes  whether Hernan Cortes is a member of the owner's Congress
     * @param draw    the saved random draw in [0, 1); taken only without Cortes
     * @return face value of the train, empty when no train is left
     */
    public static Optional<Integer> value(final boolean capital,
            final boolean cortes, final DoubleSupplier draw) {
        Preconditions.checkNotNull(draw);
        final int base = capital ? CAPITAL_VALUE : ORDINARY_VALUE;
        if (cortes) {
            return Optional.of(base * CORTES_FACTOR);
        }
        return draw.getAsDouble() < CHANCE ? Optional.of(base)
                : Optional.empty();
    }

}
