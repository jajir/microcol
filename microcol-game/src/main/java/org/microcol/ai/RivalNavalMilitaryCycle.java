package org.microcol.ai;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.model.AttackOrder;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.unit.Ship;
import org.slf4j.LoggerFactory;

/**
 * A colonial rival's bounded naval military policy for one owner turn (R-0171).
 * A spare armed ship chooses among the enemy ships it can see right now. An
 * unarmed enemy carrier ranks before any armed enemy; an armed enemy is a
 * target only at the shared attacker win probability of at least 60 percent.
 * Equal priority goes to the shorter known route to the approach square, then
 * to the lower target id, then to the lower approach coordinates.
 *
 * An approach is a known sea square beside the target, reached by a known
 * route. Each one carries the shared quote of the battle from that square, so
 * the fight the rival launches is the one it judged. Peace bars an ordinary
 * warship and a privateer uses the model's privateer exception without
 * declaring war; allies and the war wait never qualify. Hidden ships and cargo
 * are never read.
 *
 * With no target, a ship patrols the known sea within five squares of an owned
 * coastal colony along routes that stay inside that band, heading for the band
 * square that the played-turn number selects. A rival without a port has no
 * band, so its spare warships only pursue. Reserved trade and expansion
 * carriers belong to their own cycles, and the first-colony transport stays
 * with that planner even when armed or loaded; none is claimed here. A patrol
 * step beside an eligible hostile armed ship is checked by the model's own
 * interception draw, which may slow the ship but starts no battle.
 *
 * Nothing is saved: every owner turn derives the policy from live state, and
 * every move or attack spends movement or changes state, so a Continue replans
 * without repeating an action. The attack is prepared and executed through the
 * shared model, which revalidates it.
 */
final class RivalNavalMilitaryCycle {

    /** Patrol keeps within this many squares of an owned coastal colony. */
    private static final int PATROL_RADIUS = 5;

    /** Each step spends movement, so this is a safety bound only. */
    private static final int MAX_STEPS = 64;

    private static final Comparator<Location> COORDINATES = Comparator
            .comparingInt(Location::getX).thenComparingInt(Location::getY);

    /** Unarmed carriers first, then the shorter route, id and square. */
    private static final Comparator<Candidate> CANDIDATE_ORDER = Comparator
            .comparingInt((Candidate candidate) -> candidate.armed ? 1 : 0)
            .thenComparingInt(candidate -> candidate.route.size())
            .thenComparingInt(candidate -> candidate.target.getId())
            .thenComparing(candidate -> candidate.square, COORDINATES);

    private final AbstractRobotPlayer robot;

    private final RivalTradeCycle rivalTrade;

    private final RivalExpansionCycle rivalExpansion;

    /**
     * Warships the policy considered this turn. The ordinary unit loop leaves
     * them alone, so they never fight below the threshold through that loop.
     */
    private final Set<Integer> handledUnitIds = new HashSet<>();

    RivalNavalMilitaryCycle(final AbstractRobotPlayer robot,
            final RivalTradeCycle rivalTrade,
            final RivalExpansionCycle rivalExpansion) {
        this.robot = robot;
        this.rivalTrade = rivalTrade;
        this.rivalExpansion = rivalExpansion;
    }

    /**
     * @param unit required unit of this robot
     * @return whether this turn's naval policy owns the unit
     */
    boolean isParticipant(final Unit unit) {
        return handledUnitIds.contains(unit.getId());
    }

    /**
     * One owner-turn pass: every spare warship is claimed, then pursues or
     * patrols in identity order. A rival without a colony still governs its
     * spare warships, but it has no port to patrol from, so they only pursue.
     *
     * @param firstColonyTransport the ship that carries the first-colony
     *                             founder (R-0027), which stays with that
     *                             planner even when it is armed or loaded
     */
    void advance(final Optional<Unit> firstColonyTransport) {
        handledUnitIds.clear();
        if (!robot.isActiveTurn()) {
            return;
        }
        final List<Ship> warships = spareWarships().stream()
                .filter(ship -> firstColonyTransport
                        .map(transport -> transport.getId() != ship.getId())
                        .orElse(true))
                .collect(Collectors.toList());
        warships.forEach(ship -> handledUnitIds.add(ship.getId()));
        for (final Ship ship : warships) {
            if (!robot.isActiveTurn()) {
                return;
            }
            pursue(ship);
        }
    }

    private Player player() {
        return robot.getPlayer();
    }

    private Model model() {
        return robot.getModel();
    }

    /**
     * Own idle armed ships on the map that no trade or expansion objective
     * reserves, in identity order. Loaded ships are included: their cargo is
     * then part of the shared battle, and a damaged ship is not on the map at
     * all, so it never reaches the battle through this pass.
     */
    private List<Ship> spareWarships() {
        return player().getUnits().stream().filter(Unit::isShip)
                .map(Ship.class::cast)
                .filter(ship -> ship.canAttack() && ship.isAtPlaceLocation()
                        && ship.getAction().isNoAction())
                .filter(ship -> !rivalTrade.isCarrier(ship)
                        && !rivalTrade.isParked(ship)
                        && !rivalExpansion.isParticipant(ship)
                        && !rivalExpansion.isHeldCarrier(ship))
                .sorted(Comparator.comparingInt(Ship::getId))
                .collect(Collectors.toList());
    }

    /** On the map, idle, still this rival's, and with movement left. */
    private boolean isAvailable(final Ship ship) {
        return model().tryGetUnitById(ship.getId()).filter(unit -> unit == ship)
                .isPresent() && ship.isAtPlaceLocation()
                && ship.getAction().isNoAction()
                && ship.getMovementThirds() > 0;
    }

    /**
     * Attacks when standing beside the best target, steps toward it otherwise,
     * and patrols while no target is known. Each pass rechecks the live state,
     * so a vanished target, a changed relation or a refused step ends the pass.
     */
    private void pursue(final Ship ship) {
        Optional<Location> waypoint = Optional.empty();
        boolean planned = false;
        for (int step = 0; step < MAX_STEPS && robot.isActiveTurn()
                && isAvailable(ship); step++) {
            final Optional<Candidate> best = bestCandidate(ship);
            if (best.isEmpty()) {
                if (!planned) {
                    waypoint = patrolWaypoint(ship);
                    planned = true;
                }
                if (waypoint.isEmpty() || !patrolStep(ship, waypoint.get())) {
                    return;
                }
            } else if (best.get().route.isEmpty()) {
                attack(ship, best.get());
                return;
            } else if (!stepTo(ship, best.get().route.get(0))) {
                return;
            }
        }
    }

    /** Enemy ships standing on the map that this rival can see right now. */
    private List<Unit> visibleEnemyShips() {
        return player().getEnemyUnits().stream().filter(Unit::isShip)
                .filter(Unit::isAtPlaceLocation)
                .filter(unit -> player().isCurrentlyVisible(unit.getLocation()))
                .sorted(Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    /**
     * The best approach to a visible enemy ship, or empty when none is legal.
     */
    private Optional<Candidate> bestCandidate(final Ship ship) {
        Candidate best = null;
        for (final Unit target : visibleEnemyShips()) {
            for (final Location square : target.getLocation().getNeighbors()) {
                final Optional<Candidate> candidate = candidate(ship, target,
                        square);
                if (candidate.isPresent() && (best == null || CANDIDATE_ORDER
                        .compare(candidate.get(), best) < 0)) {
                    best = candidate.get();
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * The approach beside the target with its known route and the shared quote
     * of the battle fought from that square. Empty when the square is not a sea
     * approach, the route is unknown, the quote is refused or an armed target
     * quotes below the threshold. The quote is the model's own, without the
     * declaration, so peace and the alliance stay barred.
     */
    private Optional<Candidate> candidate(final Ship ship, final Unit target,
            final Location square) {
        if (!model().getMap().isValid(square) || model().getMap()
                .getTerrainTypeAt(square) == TerrainType.HIGH_SEA) {
            return Optional.empty();
        }
        final Optional<AttackOrder> order = ship.previewAttack(square,
                target.getLocation());
        if (order.isEmpty()
                || order.get().getDefender().getId() != target.getId()
                || order.get().getCombatStrengths().isEmpty()) {
            return Optional.empty();
        }
        final boolean armed = target.canAttack();
        final double probability = order.get().getCombatStrengths().get()
                .getAttackerWinProbability();
        if (armed && !RivalLandMilitaryCycle.isWinning(probability)) {
            return Optional.empty();
        }
        if (square.equals(ship.getLocation())) {
            return Optional.of(new Candidate(target, square, List.of(), armed));
        }
        final Optional<List<Location>> route = ship.getKnownPath(square, false);
        if (route.isEmpty() || route.get().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Candidate(target, square, route.get(), armed));
    }

    /**
     * Prepares the attack from the ship's own square and executes it only when
     * the same defender still stands there at the threshold. The shared model
     * revalidates the prepared order when it executes it.
     */
    private void attack(final Ship ship, final Candidate candidate) {
        final Optional<AttackOrder> order = ship
                .prepareAttack(candidate.target.getLocation(), false);
        if (order.isEmpty()
                || order.get().getDefender().getId() != candidate.target.getId()
                || order.get().getCombatStrengths().isEmpty()) {
            return;
        }
        final double probability = order.get().getCombatStrengths().get()
                .getAttackerWinProbability();
        if (candidate.armed && !RivalLandMilitaryCycle.isWinning(probability)) {
            return;
        }
        try {
            order.get().execute();
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            // The state changed after the quote; the next turn replans.
            LoggerFactory.getLogger(RivalNavalMilitaryCycle.class)
                    .debug("Rival naval attack was refused", refused);
            return;
        }
        robot.waitForAnimations();
    }

    /**
     * One ordinary step along a known route. Nothing moves when the step is not
     * affordable, lies on the high sea or the model refuses it.
     *
     * @return whether the ship moved
     */
    private boolean stepTo(final Ship ship, final Location step) {
        if (!ship.canAffordStepTo(step) || model().getMap()
                .getTerrainTypeAt(step) == TerrainType.HIGH_SEA) {
            return false;
        }
        try {
            model().moveUnit(ship, Path.of(List.of(step)));
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            // The shared rules refused the step; nothing moved.
            LoggerFactory.getLogger(RivalNavalMilitaryCycle.class)
                    .debug("Rival naval step was refused", refused);
            return false;
        }
        robot.waitForAnimations();
        return true;
    }

    /**
     * The known band square this owner turn's patrol heads for. The squares the
     * ship can reach by a route inside the band are listed in coordinate order
     * and the played-turn number picks one, so successive turns visit different
     * squares without any saved state, and a Continue picks the same one. A
     * rival without a port has no band and no waypoint.
     */
    private Optional<Location> patrolWaypoint(final Ship ship) {
        final List<Colony> ports = ownedCoastalPorts();
        if (ports.isEmpty()) {
            return Optional.empty();
        }
        final List<Location> reachable = patrolSquares(ports).stream()
                .filter(this::isKnownSea)
                .filter(square -> square.equals(ship.getLocation())
                        || isPatrolRoute(ship, square, ports))
                .collect(Collectors.toList());
        if (reachable.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reachable.get(
                Math.floorMod(model().getCalendar().getNumberOfPlayedTurns(),
                        reachable.size())));
    }

    /** A known route to the square that never leaves the patrol band. */
    private boolean isPatrolRoute(final Ship ship, final Location square,
            final List<Colony> ports) {
        return ship.isPossibleToMoveAt(square) && ship
                .getKnownPath(square, false)
                .filter(route -> !route.isEmpty()
                        && route.stream().allMatch(at -> isInBand(at, ports)))
                .isPresent();
    }

    /**
     * One step toward the patrol waypoint along a known route inside the band.
     *
     * @return whether the ship moved; false once it stands on the waypoint
     */
    private boolean patrolStep(final Ship ship, final Location waypoint) {
        if (ship.getLocation().equals(waypoint)) {
            return false;
        }
        final List<Colony> ports = ownedCoastalPorts();
        return ship.getKnownPath(waypoint, false)
                .filter(route -> !route.isEmpty()
                        && route.stream().allMatch(at -> isInBand(at, ports)))
                .map(route -> stepTo(ship, route.get(0))).orElse(false);
    }

    /** Owned colonies that are coastal ports, in identity order. */
    private List<Colony> ownedCoastalPorts() {
        return player().getColonies().stream().filter(Colony::isValid)
                .filter(colony -> model().isOwnedCoastalPort(player(),
                        colony.getLocation()))
                .sorted(Comparator.comparingInt(Colony::getId))
                .collect(Collectors.toList());
    }

    /** Every on-map square within the patrol radius of any port. */
    private List<Location> patrolSquares(final List<Colony> ports) {
        final Set<Location> squares = new HashSet<>();
        for (final Colony port : ports) {
            final Location at = port.getLocation();
            for (int dx = -PATROL_RADIUS; dx <= PATROL_RADIUS; dx++) {
                for (int dy = -PATROL_RADIUS; dy <= PATROL_RADIUS; dy++) {
                    final Location square = Location.of(at.getX() + dx,
                            at.getY() + dy);
                    if (model().getMap().isValid(square)) {
                        squares.add(square);
                    }
                }
            }
        }
        return squares.stream().sorted(COORDINATES)
                .collect(Collectors.toList());
    }

    /** Known sea that a ship may patrol; the high sea is never patrolled. */
    private boolean isKnownSea(final Location square) {
        return player().isVisible(square)
                && model().getMap().getTerrainTypeAt(square).isSea()
                && model().getMap()
                        .getTerrainTypeAt(square) != TerrainType.HIGH_SEA;
    }

    private static boolean isInBand(final Location at,
            final List<Colony> ports) {
        return ports.stream().anyMatch(port -> port.getLocation()
                .getDistanceChebyshev(at) <= PATROL_RADIUS);
    }

    /** A visible enemy ship chosen as a target from one approach square. */
    private static final class Candidate {
        private final Unit target;
        private final Location square;
        private final List<Location> route;
        private final boolean armed;

        private Candidate(final Unit target, final Location square,
                final List<Location> route, final boolean armed) {
            this.target = target;
            this.square = square;
            this.route = route;
            this.armed = armed;
        }
    }
}
