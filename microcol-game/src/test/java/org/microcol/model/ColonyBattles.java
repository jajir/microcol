package org.microcol.model;

import java.lang.reflect.Field;

import org.microcol.model.unit.UnitFreeColonist;

/**
 * Fixture helper that takes a colony only through the real defense battle: an
 * armed hostile force next to the colony attacks its center and the saved
 * combat state makes it win. There is no other way to take a colony.
 */
final class ColonyBattles {

    private ColonyBattles() {
    }

    /**
     * Make the force an armed attacker next to the colony, win the defense
     * battle with a controlled draw, and restore the current player.
     *
     * @param model  required running model
     * @param colony required colony to take
     * @param force  required unit, owned by the future owner of the colony
     */
    static void capture(final Model model, final Colony colony,
            final Unit force) {
        final Player owner = force.getOwner();
        final boolean wasRunning = model.isGameRunning();
        if (!wasRunning) {
            setGameManagerField(model, "started", true);
        }
        final Player previous = model.getCurrentPlayer();
        setCurrentPlayer(model, owner);
        try {
            if (!model.areHostile(owner, colony.getOwner())) {
                model.declareHostilities(owner, colony.getOwner());
            }
            if (force instanceof UnitFreeColonist) {
                ((UnitFreeColonist) force).setHoldingGuns(true);
            }
            final Location from = colony.getLocation().getNeighbors().stream()
                    .filter(model.getMap()::isValid)
                    .filter(at -> force.getType().canMoveAtTerrain(
                            model.getMap().getTerrainTypeAt(at)))
                    .filter(at -> model.getColonyAt(at).isEmpty())
                    .filter(at -> model.getUnitsAt(at).stream()
                            .allMatch(unit -> unit.getOwner() == owner))
                    .findFirst().orElseThrow(() -> new AssertionError(
                            "No free square next to " + colony));
            force.placeToLocation(from);
            ((AbstractUnit) force).setActionPoints(force.getSpeed());
            owner.revealMapForUnit(force);
            winFrom(model, force, colony);
        } finally {
            setCurrentPlayer(model, previous);
            if (!wasRunning) {
                setGameManagerField(model, "started", false);
            }
        }
    }

    /** The adjacent attacker wins the defense battle of the colony. */
    static void winFrom(final Model model, final Unit attacker,
            final Colony colony) {
        final AttackOrder order = attacker
                .prepareAttack(colony.getLocation(), false)
                .orElseThrow(() -> new AssertionError(
                        "No legal battle for " + colony));
        setState(model, CombatDraws.stateFor(order.getCombatStrengths()
                .orElseThrow().getAttackerWinProbability(), true));
        order.execute();
    }

    static void setState(final Model model, final long state) {
        try {
            final Field random = Model.class
                    .getDeclaredField("combatRandomState");
            random.setAccessible(true);
            random.setLong(model, state);
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }

    /** Fixture-only: whose turn it is; no gameplay precondition changes. */
    static void setCurrentPlayer(final Model model, final Player player) {
        setGameManagerField(model, "currentPlayer", player);
    }

    private static void setGameManagerField(final Model model,
            final String name, final Object value) {
        try {
            final Field manager = Model.class.getDeclaredField("gameManager");
            manager.setAccessible(true);
            final Object gameManager = manager.get(model);
            final Field field = gameManager.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(gameManager, value);
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }
}
