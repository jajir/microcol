package org.microcol.model;

/** Test-only access to the event that can invalidate an open cargo proposal. */
public final class ConcurrentModelEvents {

    private ConcurrentModelEvents() {
    }

    public static void destroyUnit(final Model model, final Unit unit) {
        model.destroyUnit(unit);
    }
}
