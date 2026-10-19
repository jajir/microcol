package org.microcol.gui;

/**
 * Class helps to draw when steps ends during move planning. Costs are counted
 * in movement thirds, so a step of any terrain cost is counted exactly.
 */
public final class StepCounter {

    private final int maxMovementThirdsPerTurn;

    private int availableMovementThirdsPerTurn;

    public StepCounter(final int maxMovementThirdsPerTurn,
            final int availableMovementThirdsPerTurn) {
        this.maxMovementThirdsPerTurn = maxMovementThirdsPerTurn;
        this.availableMovementThirdsPerTurn = availableMovementThirdsPerTurn;
        if (this.availableMovementThirdsPerTurn <= 0) {
            this.availableMovementThirdsPerTurn = maxMovementThirdsPerTurn;
        }
    }

    /**
     * Find it this move can be made is same turn.
     *
     * @param movementThirdsItRequires movement in thirds required for movement
     *                                 on this tile
     * @return if new turn is required for reaching this step than it return
     *         <code>false</code> otherwise return <code>true</code>
     */
    public boolean canMakeMoveInSameTurn(final int movementThirdsItRequires) {
        availableMovementThirdsPerTurn -= movementThirdsItRequires;
        if (availableMovementThirdsPerTurn <= 0) {
            availableMovementThirdsPerTurn = maxMovementThirdsPerTurn;
            return false;
        } else {
            return true;
        }
    }

}
