package org.microcol.model;

/**
 * Test-only bridge for tests in other packages. Colony destruction, unit
 * destruction and the real defense battle are package-private model operations;
 * this class exposes exactly those, so a rival's retirement tests use the same
 * paths as the game and no second implementation.
 */
public final class RivalTradeFixtures {

    private RivalTradeFixtures() {
    }

    /** Destroy the colony as the model does; its owner's ships move to sea. */
    public static void destroyColony(final Model model, final Colony colony) {
        model.destroyColony(colony);
    }

    /** Destroy the unit with its cargo as the model does. */
    public static void destroyUnit(final Model model, final Unit unit) {
        model.destroyUnit(unit);
    }

    /**
     * Take the colony through the real defense battle with the armed force.
     *
     * @see ColonyBattles#capture(Model, Colony, Unit)
     */
    public static void captureColony(final Model model, final Colony colony,
            final Unit force) {
        ColonyBattles.capture(model, colony, force);
    }
}
