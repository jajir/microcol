package org.microcol.page;

import java.lang.reflect.Field;

import org.microcol.model.AbstractUnit;
import org.microcol.model.AttackOrder;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

/**
 * The royal force takes a colony through the real defense battle: it is armed,
 * stands next to the colony on its own turn and wins with a controlled saved
 * combat state. There is no direct capture command.
 */
public final class RoyalBattlePage extends AbstractScreen {
    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    public RoyalBattlePage(final TestContext context) {
        super(context);
    }

    private static double draw(final long savedState, final int ordinal) {
        long z = savedState + ordinal * GAMMA;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

    private static long winningState(final double probability) {
        long state = 0;
        while (!(draw(state, 1) < probability) || draw(state, 2) < 0.25) {
            state++;
        }
        return state;
    }

    private static void set(final Object target, final Class<?> type,
            final String name, final Object value) {
        try {
            final Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static Object get(final Object target, final Class<?> type,
            final String name) {
        try {
            final Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }

    /**
     * Let the Crown force win the defense battle of the colony.
     *
     * @param colonyAt required colony center
     * @param forceId  required unit that becomes the Crown's armed attacker
     */
    public void captureColony(final Location colonyAt, final int forceId) {
        getRobot().interact(() -> {
            final Model model = getModel();
            final Colony colony = model.getColonyAt(colonyAt).orElseThrow();
            final Unit force = model.getUnitById(forceId);
            final Player king = model.getPlayerByName("Dutch's King");
            force.takeOver(king);
            if (force instanceof UnitFreeColonist) {
                ((UnitFreeColonist) force).setHoldingGuns(true);
            }
            final Location from = colonyAt.getNeighbors().stream()
                    .filter(model.getMap()::isValid)
                    .filter(at -> force.getType().canMoveAtTerrain(
                            model.getMap().getTerrainTypeAt(at)))
                    .filter(at -> model.getColonyAt(at).isEmpty())
                    .filter(at -> model.getUnitsAt(at).stream()
                            .allMatch(unit -> unit.getOwner() == king))
                    .findFirst().orElseThrow(() -> new IllegalStateException(
                            "No free square next to " + colonyAt));
            force.placeToLocation(from);
            ((AbstractUnit) force).setActionPoints(force.getSpeed());
            king.revealMapForUnit(force);
            final Object manager = get(model, Model.class, "gameManager");
            final Object previous = get(manager, manager.getClass(),
                    "currentPlayer");
            set(manager, manager.getClass(), "currentPlayer", king);
            try {
                final AttackOrder order = force.prepareAttack(colonyAt, false)
                        .orElseThrow(() -> new IllegalStateException(
                                "No legal battle for " + colony));
                set(model, Model.class, "combatRandomState",
                        winningState(order.getCombatStrengths().orElseThrow()
                                .getAttackerWinProbability()));
                order.execute();
            } finally {
                set(manager, manager.getClass(), "currentPlayer", previous);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
}
