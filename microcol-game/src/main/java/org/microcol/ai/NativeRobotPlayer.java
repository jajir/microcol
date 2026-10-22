package org.microcol.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.AttackOrder;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeIncitementOrder;
import org.microcol.model.NativeSettlement;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitNativeBrave;

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
        if (!pursueIncited(brave) && !raid(brave)) {
            patrolHome(brave);
        }
    }

    /**
     * Pursues the selected European power of a paid incitement (R-0166). The
     * candidates are the known units of that power with a legal route beside
     * them, nearest by the cost of that route and then by stable unit ID. The
     * brave walks as much of the route as its movement pays and attacks only
     * when it then stands beside the target. Once such a target exists the
     * pursuit owns the turn, even if no step is affordable now, so a distant
     * target never hands the brave back to the ordinary raid. Without an order
     * or without a known reachable unit it returns false and the ordinary raid
     * follows.
     *
     * @return whether the brave pursued a target
     */
    private boolean pursueIncited(final UnitNativeBrave brave) {
        final Optional<NativeIncitementOrder> order = getModel()
                .getNativeIncitementOrder(brave.getNation());
        if (order.isEmpty()) {
            return false;
        }
        for (final Pursuit pursuit : incitedPursuits(brave,
                order.get().getTarget())) {
            if (!isStillHostileEnemy(brave, pursuit.target)) {
                continue;
            }
            if (pursuit.route.isEmpty()) {
                if (attackAdjacent(brave, pursuit.target)) {
                    return true;
                }
                // This unit cannot be fought now; the next nearest is tried.
                continue;
            }
            walk(brave, pursuit.route);
            if (brave.isAtPlaceLocation() && brave.getLocation()
                    .isNeighbor(pursuit.target.getLocation())) {
                attackAdjacent(brave, pursuit.target);
            }
            return true;
        }
        return false;
    }

    /**
     * Known units of the selected European power that this brave may attack by
     * the model's type rule, each with the cheapest legal route beside it. Only
     * present sight of the whole nation counts, as in the ordinary raid, so a
     * hidden unit is never chosen, and a unit without a legal route, such as
     * one on an unreachable islet, is left out. The nearest by route cost comes
     * first, and equal costs go by stable unit ID.
     */
    private List<Pursuit> incitedPursuits(final UnitNativeBrave brave,
            final EuropeanNation european) {
        final List<Pursuit> pursuits = new ArrayList<>();
        for (final Unit unit : getModel().getAllUnits()) {
            if (!unit.isAtPlaceLocation() || unit.isShip()
                    || !brave.getType().getAttackableUnitType()
                            .contains(unit.getType())
                    || !isNation(unit.getOwner(), european)
                    || !getPlayer().isCurrentlyVisible(unit.getLocation())) {
                continue;
            }
            routeBeside(brave, unit).ifPresent(route -> pursuits
                    .add(new Pursuit(unit, route, routeCost(brave, route))));
        }
        pursuits.sort(
                Comparator.comparingInt((Pursuit pursuit) -> pursuit.costThirds)
                        .thenComparingInt(pursuit -> pursuit.target.getId()));
        return pursuits;
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
     * @return whether this owner is the European power of that nation
     */
    private static boolean isNation(final Player owner,
            final EuropeanNation european) {
        return EuropeanNation.findByPlayerName(owner.getName())
                .equals(Optional.of(european));
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
     * Tries the nearest known hostile enemy first, then the next one, so a
     * blocked nearer enemy never hides a reachable farther one.
     *
     * @return whether the brave moved or attacked
     */
    private boolean raid(final UnitNativeBrave brave) {
        for (final Unit target : hostileTargets(brave)) {
            if (!isStillHostileEnemy(brave, target)) {
                continue;
            }
            if (brave.getLocation().isNeighbor(target.getLocation())) {
                if (attackAdjacent(brave, target)) {
                    return true;
                }
            } else if (approach(brave, target)) {
                // One legal approach ends the choice; an attack follows only if
                // the remaining movement still allows it.
                attackAdjacent(brave, target);
                return true;
            }
        }
        return false;
    }

    /**
     * Colonial units of a hostile nation within the raid radius that this brave
     * may attack by the model's own type rule, nearest first and then by stable
     * unit ID. Only present sight of the whole nation counts, so a hidden enemy
     * is never selected, and ships or other types the brave cannot attack never
     * distract it.
     */
    private List<Unit> hostileTargets(final UnitNativeBrave brave) {
        final Location at = brave.getLocation();
        final List<Unit> targets = new ArrayList<>();
        for (int dx = -RAID_RADIUS; dx <= RAID_RADIUS; dx++) {
            for (int dy = -RAID_RADIUS; dy <= RAID_RADIUS; dy++) {
                final Location square = Location.of(at.getX() + dx,
                        at.getY() + dy);
                if (square.equals(at) || !getModel().getMap().isValid(square)) {
                    continue;
                }
                getPlayer().getObservedUnitsAt(square).stream()
                        .filter(unit -> !unit.isShip()
                                && brave.getType().getAttackableUnitType()
                                        .contains(unit.getType()))
                        .filter(unit -> isHostile(brave, unit.getOwner()))
                        .forEach(targets::add);
            }
        }
        targets.sort(Comparator.comparingInt(
                (Unit unit) -> unit.getLocation().getDistanceChebyshev(at))
                .thenComparingInt(Unit::getId));
        return targets;
    }

    /**
     * Rechecked before every action: the unit may be gone, its owner may no
     * longer be hostile, or the nation may have lost sight of it since the list
     * was made.
     */
    private boolean isStillHostileEnemy(final UnitNativeBrave brave,
            final Unit target) {
        return brave.isAtPlaceLocation() && target.isAtPlaceLocation()
                && isHostile(brave, target.getOwner())
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
     * Moves the brave to the square beside the target with the shortest
     * affordable path, ties broken by coordinate order.
     *
     * @return whether the brave moved
     */
    private boolean approach(final UnitNativeBrave brave, final Unit target) {
        Optional<Location> best = Optional.empty();
        int bestSteps = Integer.MAX_VALUE;
        for (final Location square : target.getLocation().getNeighbors()
                .stream().sorted(COORDINATE_ORDER)
                .collect(Collectors.toList())) {
            if (!getModel().getMap().isValid(square)) {
                continue;
            }
            final Optional<List<Location>> path = brave.getPath(square);
            if (path.isPresent() && !path.get().isEmpty()
                    && brave.canAffordPath(path.get())
                    && path.get().size() < bestSteps) {
                best = Optional.of(square);
                bestSteps = path.get().size();
            }
        }
        return best.isPresent() && moveTo(brave, best.get());
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
