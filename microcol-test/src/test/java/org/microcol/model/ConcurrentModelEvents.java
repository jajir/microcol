package org.microcol.model;

/**
 * Test-only access to events that can invalidate an open proposal: a lost unit
 * under a cargo proposal, or a calendar that moves on beneath an independence
 * confirmation.
 */
public final class ConcurrentModelEvents {

    private ConcurrentModelEvents() {
    }

    public static void destroyUnit(final Model model, final Unit unit) {
        model.destroyUnit(unit);
    }

    /** Move the calendar one turn on, as a concurrent round boundary would. */
    public static void advanceCalendar(final Model model) {
        model.getCalendar().endRound();
    }
}
