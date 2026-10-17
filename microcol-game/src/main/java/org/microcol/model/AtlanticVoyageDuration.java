package org.microcol.model;

import com.google.common.base.Preconditions;

/** The ordinary crossing length determined at departure. */
final class AtlanticVoyageDuration {
    private AtlanticVoyageDuration() {
    }

    static int calculate(final int weather, final boolean toEurope,
            final Direction departureDirection, final int baseSpeed) {
        Preconditions.checkArgument(weather >= -1 && weather <= 1,
                "Weather must be -1, 0 or 1");
        Preconditions.checkNotNull(departureDirection);
        Preconditions.checkArgument(baseSpeed > 0, "Invalid ship speed");
        final int westward = toEurope ? 0 : 1;
        final int westernEdge = toEurope && departureDirection == Direction.west
                ? 1
                : 0;
        final int fast = baseSpeed >= 6 ? 1 : 0;
        return Math.max(1,
                Math.min(4, 2 + weather + westward + westernEdge - fast));
    }

    static int calculate(final int weather, final boolean toEurope,
            final Direction departureDirection, final int baseSpeed,
            final boolean magellan) {
        final int ordinary = calculate(weather, toEurope, departureDirection,
                baseSpeed);
        return magellan && toEurope && departureDirection == Direction.west
                ? Math.max(1, (ordinary + 1) / 2)
                : ordinary;
    }
}
