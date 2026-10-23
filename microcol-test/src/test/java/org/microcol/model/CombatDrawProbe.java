package org.microcol.model;

/**
 * Test-only access to the saved combat random state: choose the state whose
 * first draw gives the wanted result for the quoted odds and whose second draw
 * would not promote a winner.
 */
public final class CombatDrawProbe {

    private CombatDrawProbe() {
    }

    public static void choose(final Model model, final double attackerChance,
            final boolean attackerWins) throws ReflectiveOperationException {
        final java.lang.reflect.Field field = Model.class
                .getDeclaredField("combatRandomState");
        field.setAccessible(true);
        final long saved = field.getLong(model);
        try {
            for (long state = 0; state < 4096; state++) {
                field.setLong(model, state);
                final boolean wins = model.nextCombatRandom() < attackerChance;
                final boolean promoted = model.nextCombatRandom() < 0.25;
                if (wins == attackerWins && !promoted) {
                    field.setLong(model, state);
                    return;
                }
            }
        } catch (final RuntimeException | Error failure) {
            field.setLong(model, saved);
            throw failure;
        }
        field.setLong(model, saved);
        throw new IllegalStateException("No suitable saved draw");
    }

    /**
     * Choose the state for a naval battle, which takes two draws: the first
     * decides the winner and the second sinks the loser below one half.
     */
    public static void chooseNaval(final Model model,
            final double attackerChance, final boolean attackerWins,
            final boolean loserSinks) throws ReflectiveOperationException {
        final java.lang.reflect.Field field = Model.class
                .getDeclaredField("combatRandomState");
        field.setAccessible(true);
        final long saved = field.getLong(model);
        try {
            for (long state = 0; state < 4096; state++) {
                field.setLong(model, state);
                final boolean wins = model.nextCombatRandom() < attackerChance;
                final boolean sinks = model.nextCombatRandom() < 0.5;
                if (wins == attackerWins && sinks == loserSinks) {
                    field.setLong(model, state);
                    return;
                }
            }
        } catch (final RuntimeException | Error failure) {
            field.setLong(model, saved);
            throw failure;
        }
        field.setLong(model, saved);
        throw new IllegalStateException("No suitable saved naval draw");
    }
}
