package org.microcol.gui.screen.menu;

/**
 * Helps to linear oscillation between two values.
 */
class ScaleLinear {

    private final int maxValue;
    private final int minValue;
    private int actualSize;
    private boolean goUp = true;

    /**
     * Initialize minimal and max values.
     * 
     * @param minValue
     *            required minimal value
     * @param maxValue
     *            required maximal value
     */
    public ScaleLinear(final int minValue, final int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        actualSize = nextSize(minValue);
    }

    public int getNext() {
        int out = actualSize;
        actualSize = nextSize(actualSize);
        return out;
    }

    private int nextSize(int current) {
        if (current == maxValue) {
            current--;
            goUp = false;
        }
        if (current == minValue) {
            current++;
            goUp = true;
        }
        if (goUp) {
            current++;
        } else {
            current--;
        }
        return current;
    }
}