package org.microcol.model;

/**
 * Movement is stored in thirds of a point, so a fractional entry cost never
 * rounds away. Whole-point rules convert through this class.
 */
public final class MovementPoints {

    /** Number of movement thirds in one whole point. */
    public static final int THIRDS_PER_POINT = 3;

    private MovementPoints() {
    }

    /**
     * @param points required whole movement points
     * @return the same allowance expressed in thirds
     */
    public static int ofPoints(final int points) {
        return points * THIRDS_PER_POINT;
    }

}
