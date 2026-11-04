package org.akropia.gui;

/** Exact player-facing movement amounts stored in thirds. */
public final class MovementText {
    private MovementText() {
    }

    public static String format(final int thirds) {
        if (thirds < 0) {
            throw new IllegalArgumentException("Negative movement");
        }
        final int whole = thirds / 3;
        final int fraction = thirds % 3;
        if (fraction == 0) {
            return Integer.toString(whole);
        }
        return (whole == 0 ? "" : whole + " ") + (fraction == 1 ? "⅓" : "⅔");
    }
}
