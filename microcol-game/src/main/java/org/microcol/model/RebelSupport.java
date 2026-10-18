package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Exact rebel support as an integer fraction. A colony with {@code N} residents
 * contributes at most {@code 200 * N} rebel-equivalent points, so summing
 * numerators and denominators weights colonies by population. Only display
 * helpers round, and threshold checks stay exact.
 */
public final class RebelSupport {

    /** Rebel-equivalent points that mean full support for one resident. */
    public static final int POINTS_PER_RESIDENT = 200;

    /** Bolivar adds 20 percentage points, i.e. 40 points per resident. */
    public static final int BOLIVAR_POINTS_PER_RESIDENT = 40;

    public static final RebelSupport NONE = new RebelSupport(0, 0);

    private final long numerator;

    private final long denominator;

    RebelSupport(final long numerator, final long denominator) {
        Preconditions.checkArgument(
                numerator >= 0 && denominator >= 0 && numerator <= denominator
                        && denominator % POINTS_PER_RESIDENT == 0,
                "Invalid rebel support fraction");
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * @return rebel-equivalent points, never more than the denominator
     */
    public long getNumerator() {
        return numerator;
    }

    /**
     * @return {@code 200 * population}; zero when there are no residents
     */
    public long getDenominator() {
        return denominator;
    }

    /**
     * @return number of residents represented by this fraction
     */
    public long getPopulation() {
        return denominator / POINTS_PER_RESIDENT;
    }

    /**
     * @return exact sum of two fractions (population weighted)
     */
    public RebelSupport plus(final RebelSupport other) {
        Preconditions.checkNotNull(other);
        return new RebelSupport(Math.addExact(numerator, other.numerator),
                Math.addExact(denominator, other.denominator));
    }

    /**
     * Exact threshold check without rounding.
     *
     * @param percent threshold from 0 to 100
     * @return {@code true} when support is at least the percentage; with zero
     *         residents only a zero threshold is met
     */
    public boolean isAtLeastPercent(final int percent) {
        Preconditions.checkArgument(percent >= 0 && percent <= 100,
                "Invalid percentage");
        if (denominator == 0) {
            return percent == 0;
        }
        return Math.multiplyExact(numerator, 100L) >= Math
                .multiplyExact(denominator, (long) percent);
    }

    /**
     * Exact share of a head count: {@code floor(count * numerator /
     * denominator)}, never the rounded display percentage.
     *
     * @param count nonnegative number of people
     * @return the rounded-down share; zero with no residents
     */
    public long floorShare(final long count) {
        Preconditions.checkArgument(count >= 0, "Negative count");
        return denominator == 0 ? 0
                : Math.multiplyExact(count, numerator) / denominator;
    }

    /**
     * @return support in percent rounded down, for display only; zero with no
     *         residents
     */
    public int getDisplayPercent() {
        return denominator == 0 ? 0
                : (int) (Math.multiplyExact(numerator, 100L) / denominator);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RebelSupport)) {
            return false;
        }
        final RebelSupport other = (RebelSupport) obj;
        return numerator == other.numerator && denominator == other.denominator;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(numerator) * 31 + Long.hashCode(denominator);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("numerator", numerator)
                .add("denominator", denominator).toString();
    }
}
