package org.akropia.model;

/**
 * Test-only access to events that can invalidate an open proposal: a lost unit
 * under a cargo proposal, or political support lost beneath an independence
 * confirmation.
 */
public final class ConcurrentModelEvents {

    private ConcurrentModelEvents() {
    }

    public static void destroyUnit(final Model model, final Unit unit) {
        model.destroyUnit(unit);
    }

    /** Remove current support while a proposal is already open. */
    public static void removeRebelSupport(final Model model,
            final Player player) {
        try {
            final java.lang.reflect.Field liberty = Colony.class
                    .getDeclaredField("liberty");
            liberty.setAccessible(true);
            for (final Colony colony : model.getColonies(player)) {
                liberty.setLong(colony, 0);
            }
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }
}
