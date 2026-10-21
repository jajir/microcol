package org.microcol.model.generation;

/**
 * Share of the whole map covered by land in a generated New World. The
 * generator aims at this share exactly; the accepted tolerance is
 * {@link #TOLERANCE_PERCENT} percentage points.
 */
public enum GeneratedLandShare {

    LOW(25), MEDIUM(40), HIGH(55);

    /** Accepted difference between the generated and the chosen share. */
    public static final int TOLERANCE_PERCENT = 5;

    private final int percent;

    GeneratedLandShare(final int percent) {
        this.percent = percent;
    }

    /**
     * @return chosen share of the map in whole percent
     */
    public int getPercent() {
        return percent;
    }

    /**
     * @param area number of squares of the map
     * @return number of land squares aimed at for a map of that area
     */
    int landSquares(final int area) {
        return (int) Math.round(area * percent / 100.0);
    }

}
