package org.microcol.gui;

/**
 * Class helps to draw when steps ends during move planning.
 * 
 */
public class StepCounter {

    private final int maxStepsPerTurn;

    private int availableStepsPerTurn;

    public StepCounter(final int maxStepsPerTurn, final int availableStepsPerTurn) {
        this.maxStepsPerTurn = maxStepsPerTurn;
        this.availableStepsPerTurn = availableStepsPerTurn;
        if (this.availableStepsPerTurn <= 0) {
            this.availableStepsPerTurn = maxStepsPerTurn;
        }
    }

    /**
     * Find it this move can be made is same turn.
     * 
     * @param howManyActionPointsItRequires
     *            number of action points required for movement on this tile
     * @return if new turn is required for reaching this step than it return
     *         <code>false</code> otherwise return <code>true</code>
     */
    public boolean canMakeMoveInSameTurn(final int howManyActionPointsItRequires) {
        availableStepsPerTurn -= howManyActionPointsItRequires;
        if (availableStepsPerTurn <= 0) {
            availableStepsPerTurn = maxStepsPerTurn;
            return false;
        } else {
            return true;
        }
    }

}