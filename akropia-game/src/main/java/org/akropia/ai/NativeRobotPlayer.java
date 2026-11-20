package org.akropia.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.screen.game.gamepanel.AnimationLock;
import org.akropia.model.AttackOrder;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.NativeHostility;
import org.akropia.model.NativeSettlement;
import org.akropia.model.Path;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitNativeBrave;

/**
 * Native nation's own turn. Each brave acts once, in the order the model lists
 * it. A brave of a tribe with a paid incitement order (R-0166) first pursues
 * the nearest known legal unit of the selected European power, at any distance:
 * it walks the affordable part of the shortest legal route this turn and
 * attacks only once it stands beside the target with movement left. With no
 * such unit it attacks the nearest known legal land enemy of a colonial
 * European that its nation is hostile to, within {@link #RAID_RADIUS} squares,
 * or else takes the shared home patrol. A calmer brave never raids but takes
 * the same patrol. The model validates every move and attack; this class only
 * chooses among them and treats a rejection as no action. The work is bounded
 * by one brave's movement, the registered units and one route search for each
 * known unit of the selected power.
 */
public final class NativeRobotPlayer extends AbstractRobotPlayer {

    /** Largest Chebyshev distance of a raid target and of the home patrol. */
    static final int RAID_RADIUS = 2;

    /** Fixed coordinate order of every choice: ascending y, then x. */
    private static final Comparator<Location> COORDINATE_ORDER = Comparator
            .comparingInt(Location::getY).thenComparingInt(Location::getX);

    /**
     * A known unit of the paid power with the shortest legal route to a square
     * beside it. The route is empty when the brave already stands beside the
     * unit; the cost is the sum of the entry costs of its steps.
     */
    private static final class Pursuit {

        private final Unit target;

        private final List<Location> route;

        private final int costThirds;

        private Pursuit(final Unit target, final List<Location> route,
                final int costThirds) {
            this.target = target;
            this.route = route;
            this.costThirds = costThirds;
        }
    }

    public NativeRobotPlayer(final Model model, final Player player,
            final AnimationLock animationLock) {
        super(model, player, animationLock);
    }

    @Override
    void turnStarted() {
        // Braves act one by one in moveUnit; nothing else happens at turn
        // start.
    }

    /**
     * A brave pursues the incited target when one is known and reachable, then
     * raids a hostile target when one is legal, and otherwise follows the
     * shared home patrol. Calm braves never raid, but they still patrol.
     */
    @Override
    void moveUnit(final Unit unit) {
        if (!(unit instanceof UnitNativeBrave) || !unit.isAtPlaceLocation()) {
            return;
        }
        final UnitNativeBrave brave = (UnitNativeBrave) unit;
        if (!pursueEnemy(brave)) {
            patrolHome(brave);
        }
    }

    /**
     * Pursue a visible enemy in the settlement defense radius, using normal
     * paths.
     */
    private boolean pursueEnemy(final UnitNativeBrave brave) {
        final List<Pursuit> pursuits = new ArrayList<>();
        for (final Unit target : getModel().getAllUnits()) {
            if (!getModel().canNativeAttack(brave, target)
                    || !getPlayer().isCurrentlyVisible(target.getLocation())) {
                continue;
            }
            routeBeside(brave, target).ifPresent(route -> pursuits
                    .add(new Pursuit(target, route, routeCost(brave, route))));
        }
        pursuits.sort(
                Comparator.comparingInt((Pursuit pursuit) -> pursuit.costThirds)
                        .thenComparingInt(pursuit -> pursuit.target.getId()));
        for (final Pursuit pursuit : pursuits) {
            if (pursuit.route.isEmpty()) {
                if (attackAdjacent(brave, pursuit.target)) {
                    return true;
                }
            } else {
                walk(brave, pursuit.route);
                if (brave.isAtPlaceLocation() && brave.getLocation()
                        .isNeighbor(pursuit.target.getLocation())) {
                    attackAdjacent(brave, pursuit.target);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @return the shortest legal route over squares the nation has explored to
     *         a square beside the target: empty when the brave already stands
     *         beside it, and no value when no such known route exists. A hidden
     *         shortcut is never used, but a longer known route still counts.
     *         The route is the full one, even where this turn's movement cannot
     *         pay all of it.
     */
    private Optional<List<Location>> routeBeside(final UnitNativeBrave brave,
            final Unit target) {
        if (brave.getLocation().isNeighbor(target.getLocation())) {
            return Optional.of(List.of());
        }
        return brave.getKnownPath(target.getLocation(), true)
                .filter(route -> !route.isEmpty());
    }

    /**
     * @return the movement cost of the whole route, in thirds of a point
     */
    private static int routeCost(final UnitNativeBrave brave,
            final List<Location> route) {
        int cost = 0;
        Location at = brave.getLocation();
        for (final Location step : route) {
            cost += brave.getEntryCostThirds(at, step);
            at = step;
        }
        return cost;
    }

    /**
     * Moves the brave along the longest start of the route that its remaining
     * movement pays, by the model's own move command. A rejection, or a route
     * whose first step is not affordable, means no move.
     *
     * @return whether the brave moved
     */
    private boolean walk(final UnitNativeBrave brave,
            final List<Location> route) {
        if (!brave.isAtPlaceLocation()) {
            return false;
        }
        int steps = 0;
        while (steps < route.size()
                && brave.canAffordPath(route.subList(0, steps + 1))) {
            steps++;
        }
        if (steps == 0) {
            return false;
        }
        try {
            getModel().moveUnit(brave,
                    Path.of(new ArrayList<>(route.subList(0, steps))));
        } catch (final IllegalStateException | IllegalArgumentException e) {
            // The model validates the whole prefix first, so it did not move.
            return false;
        }
        waitForAnimations();
        return true;
    }

    /**
     * Whether the brave's nation is hostile to this colonial European, read
     * from the home settlement of the brave, or the tribal anger of a nation
     * whose brave is homeless.
     */
    private boolean isHostile(final UnitNativeBrave brave,
            final Player player) {
        return player.isColonialNation()
                && getModel().getNativeBraveAttitude(brave, player)
                        .equals(NativeHostility.HOSTILE);
    }

    /**
     * Rechecked before every action: the unit may be gone, its owner may no
     * longer be hostile, or the nation may have lost sight of it since the list
     * was made.
     */
    private boolean isStillHostileEnemy(final UnitNativeBrave brave,
            final Unit target) {
        return brave.isAtPlaceLocation()
                && getModel().canNativeAttack(brave, target)
                && getPlayer().isCurrentlyVisible(target.getLocation());
    }

    /**
     * Attacks a unit on an adjacent square through the model's own quote. The
     * model picks the defender on that square, so that defender must be hostile
     * too. A rejection because the state changed means no attack.
     *
     * @return whether the attack was made
     */
    private boolean attackAdjacent(final UnitNativeBrave brave,
            final Unit target) {
        if (!isStillHostileEnemy(brave, target)) {
            return false;
        }
        final Optional<AttackOrder> order = brave
                .prepareAttack(target.getLocation(), false);
        if (order.isEmpty()
                || !isHostile(brave, order.get().getDefender().getOwner())) {
            return false;
        }
        try {
            order.get().execute();
        } catch (final IllegalStateException | IllegalArgumentException e) {
            // The model validates before it mutates, so nothing has changed.
            return false;
        }
        waitForAnimations();
        return true;
    }

    /**
     * A brave outside the patrol radius returns toward its home over the
     * nearest reachable square. Inside the radius it takes one step to the
     * first reachable neighbor in coordinate order that stays within the
     * radius. A homeless brave without a target ends its turn here.
     */
    private void patrolHome(final UnitNativeBrave brave) {
        final Optional<NativeSettlement> home = getModel()
                .getNativeBraveHome(brave);
        if (home.isEmpty() || !brave.isAtPlaceLocation()
                || brave.getMovementThirds() <= 0) {
            return;
        }
        final Location center = home.get().getLocation();
        final Location at = brave.getLocation();
        final List<Location> reachable = brave.getAvailableLocations().stream()
                .filter(square -> !square.equals(at))
                .collect(Collectors.toList());
        final int distance = at.getDistanceChebyshev(center);
        if (distance > RAID_RADIUS) {
            reachable.stream().filter(
                    square -> square.getDistanceChebyshev(center) < distance)
                    .min(Comparator
                            .comparingInt((Location square) -> square
                                    .getDistanceChebyshev(center))
                            .thenComparing(COORDINATE_ORDER))
                    .ifPresent(square -> moveTo(brave, square));
        } else {
            reachable.stream().filter(at::isNeighbor)
                    .filter(square -> square
                            .getDistanceChebyshev(center) <= RAID_RADIUS)
                    .min(COORDINATE_ORDER)
                    .ifPresent(square -> moveTo(brave, square));
        }
    }

    /**
     * Moves the brave along its path to the square, if the model still allows
     * that path with the remaining movement. A rejection means no move.
     *
     * @return whether the brave moved
     */
    private boolean moveTo(final UnitNativeBrave brave, final Location square) {
        if (!brave.isAtPlaceLocation()) {
            return false;
        }
        final Optional<List<Location>> path = brave.getPath(square);
        if (path.isEmpty() || path.get().isEmpty()
                || !brave.canAffordPath(path.get())) {
            return false;
        }
        try {
            getModel().moveUnit(brave, Path.of(path.get()));
        } catch (final IllegalStateException | IllegalArgumentException e) {
            // The model validates the whole route first, so it did not move.
            return false;
        }
        waitForAnimations();
        return true;
    }

}
