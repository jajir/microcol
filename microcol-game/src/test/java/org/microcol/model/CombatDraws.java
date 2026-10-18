package org.microcol.model;

/**
 * Saved combat random states with a known result, so that a test controls a
 * battle through the saved state instead of weakening any precondition.
 */
final class CombatDraws {
    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    private CombatDraws() {
    }

    /** The draw a model makes after the given saved state. */
    static double draw(final long savedState, final int ordinal) {
        long z = savedState + ordinal * GAMMA;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

    /**
     * A state whose first draw wins any ordinary land battle with at least a
     * 70% chance and whose second draw promotes nobody.
     */
    static long attackerWins() {
        for (long state = 0;; state++) {
            if (draw(state, 1) < 0.3 && draw(state, 2) >= 0.25) {
                return state;
            }
        }
    }

    /**
     * A state whose first draw loses any ordinary land battle for the attacker
     * unless it is a certain win.
     */
    static long attackerLoses() {
        for (long state = 0;; state++) {
            if (draw(state, 1) > 0.99 && draw(state, 2) >= 0.25) {
                return state;
            }
        }
    }

    /** A state for the exact quoted odds; the winner is not promoted. */
    static long stateFor(final double attackerWinProbability,
            final boolean attackerWins) {
        return stateFor(attackerWinProbability, attackerWins, false);
    }

    /**
     * A state for the exact quoted odds whose second draw does, or does not,
     * give the winner the ordinary 25% promotion.
     */
    static long stateFor(final double attackerWinProbability,
            final boolean attackerWins, final boolean promoted) {
        for (long state = 0;; state++) {
            if ((draw(state, 1) < attackerWinProbability) == attackerWins
                    && (draw(state, 2) < 0.25) == promoted) {
                return state;
            }
        }
    }
}
