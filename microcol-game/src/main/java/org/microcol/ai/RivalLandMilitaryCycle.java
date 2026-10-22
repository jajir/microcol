package org.microcol.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.microcol.model.AttackOrder;
import org.microcol.model.Colony;
import org.microcol.model.ColonySighting;
import org.microcol.model.Location;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;
import org.slf4j.LoggerFactory;

/**
 * A rival's bounded land military policy for one owner turn. A wartime enemy
 * unit in present sight within two squares of an owned colony threatens that
 * colony. Every threatened colony first gets one armed defender, nearest first,
 * then the other spare armed units reinforce the least garrisoned colony. The
 * offensive force moves only after each threatened colony holds a defender. An
 * attack needs an adjacent wartime enemy at the shared attacker win probability
 * of at least 60 percent.
 *
 * Targets are wartime units in present sight and wartime colonies the rival
 * knows, including remembered ones. Each target is reached through a legal
 * approach square beside it with a known route. For a target in sight every
 * approach carries the shared model quote of the battle fought from that
 * square, so square-dependent strength, such as artillery leaving its fortified
 * colony, is judged where the attack would happen; an approach below the
 * threshold is not used. Approaches are ranked by route length, then by the
 * quote, then by the defender's id. A remembered colony out of sight has no
 * quote yet; it ranks after quoted targets at the same distance and is quoted
 * when it comes into sight.
 *
 * Nothing is saved: every owner turn derives the policy from live state, and
 * every move or attack spends movement or changes state, so a Continue replans
 * without repeating an action. The final attack is prepared and executed
 * through the shared model, which revalidates it.
 */
final class RivalLandMilitaryCycle {

    /** A wartime enemy unit this near an owned colony threatens it. */
    private static final int THREAT_DISTANCE = 2;

    /** An attack is made only at this shared attacker win probability. */
    private static final double MINIMUM_WIN_PROBABILITY = 0.6;

    /** Each step spends movement, so this is a safety bound only. */
    private static final int MAX_STEPS = 64;

    /** Tie-break id of a remembered colony, which has no visible defender. */
    private static final int UNKNOWN_DEFENDER_ID = Integer.MAX_VALUE;

    private static final Comparator<Location> COORDINATES = Comparator
            .comparingInt(Location::getX).thenComparingInt(Location::getY);

    /** Shorter known route first, then higher win probability, then id. */
    private static final Comparator<Approach> APPROACH_ORDER = Comparator
            .comparingInt((Approach approach) -> approach.distance)
            .thenComparing(Comparator
                    .comparingDouble(
                            (Approach approach) -> approach.probability)
                    .reversed())
            .thenComparingInt(approach -> approach.defenderId)
            .thenComparing(approach -> approach.target, COORDINATES)
            .thenComparing(approach -> approach.square, COORDINATES);

    private final AbstractRobotPlayer robot;

    /**
     * Armed units the policy considered this turn. The ordinary unit loop
     * leaves them alone, so it never attacks below the win probability
     * threshold.
     */
    private final Set<Integer> handledUnitIds = new HashSet<>();

    /**
     * Armed units and the colony defenders that defense committed, kept for the
     * offence half of the same owner turn. Null unless offence is allowed.
     */
    private List<Unit> offenseArmed;

    private Set<Integer> offenseCommitted;

    RivalLandMilitaryCycle(final AbstractRobotPlayer robot) {
        this.robot = robot;
    }

    /**
     * @param unit required unit of this robot
     * @return whether this turn's land policy owns the unit
     */
    boolean isParticipant(final Unit unit) {
        return handledUnitIds.contains(unit.getId());
    }

    /**
     * @param winProbability required shared attacker win probability
     * @return whether an attack at that probability is allowed
     */
    static boolean isWinning(final double winProbability) {
        return winProbability >= MINIMUM_WIN_PROBABILITY;
    }

    /**
     * One owner-turn pass: defenders first, then the remaining armed force,
     * which moves only when every threatened colony holds a defender.
     */
    void advance() {
        advanceDefense();
        advanceOffense();
    }

    /**
     * First half of the owner-turn pass: claim the armed units and cover every
     * threatened colony. Offence is prepared only when each threatened colony
     * holds a defender, and runs later in {@link #advanceOffense()}, after the
     * supply and expansion objectives have reserved their units.
     */
    void advanceDefense() {
        handledUnitIds.clear();
        offenseArmed = null;
        offenseCommitted = null;
        if (!robot.isActiveTurn()) {
            return;
        }
        final List<Colony> threatened = player().getColonies().stream()
                .filter(Colony::isValid).filter(this::isThreatened)
                .sorted(Comparator.comparingInt(Colony::getId))
                .collect(Collectors.toList());
        final List<Unit> armed = armedOnMap();
        armed.forEach(unit -> handledUnitIds.add(unit.getId()));
        releaseUnthreatenedDefenders(armed, threatened);
        final Set<Integer> committed = new HashSet<>();
        final Map<Integer, Integer> sent = new HashMap<>();
        for (final Unit unit : armed) {
            if (isOnColony(unit, threatened)) {
                committed.add(unit.getId());
                fortifyIfPossible(unit);
            }
        }
        // Pass one: while a threatened colony has no defender, the globally
        // shortest pair of such a colony and a spare unit is sent, ties by
        // colony id and then unit id, so a single spare goes to the colony it
        // can reach first and no colony is left empty while another has many.
        final List<Colony> undefended = threatened.stream()
                .filter(colony -> countDefenders(colony) == 0)
                .collect(Collectors.toCollection(ArrayList::new));
        while (robot.isActiveTurn() && !undefended.isEmpty()) {
            final Optional<Assignment> next = nearestPair(undefended, armed,
                    committed);
            if (next.isEmpty()) {
                break;
            }
            undefended.remove(next.get().colony);
            send(next.get().unit, next.get().colony, committed, sent);
        }
        if (!robot.isActiveTurn()) {
            return;
        }
        // Pass two: the remaining spare units reinforce the least garrisoned
        // threatened colony they can reach.
        for (final Unit unit : armed) {
            if (!robot.isActiveTurn()) {
                return;
            }
            if (!committed.contains(unit.getId()) && isAvailable(unit)) {
                leastGarrisoned(unit, threatened, sent).ifPresent(
                        colony -> send(unit, colony, committed, sent));
            }
        }
        if (!robot.isActiveTurn() || threatened.stream()
                .anyMatch(colony -> countDefenders(colony) == 0)) {
            return;
        }
        offenseArmed = armed;
        offenseCommitted = committed;
    }

    /**
     * Second half of the owner-turn pass: the armed units that defense left
     * free advance on offence, but only when defense prepared it this turn.
     */
    void advanceOffense() {
        final List<Unit> armed = offenseArmed;
        final Set<Integer> committed = offenseCommitted;
        offenseArmed = null;
        offenseCommitted = null;
        if (armed == null || committed == null) {
            return;
        }
        for (final Unit unit : armed) {
            if (!robot.isActiveTurn()) {
                return;
            }
            if (!committed.contains(unit.getId()) && isAvailable(unit)) {
                advanceOffense(unit);
            }
        }
    }

    private Model model() {
        return robot.getModel();
    }

    private Player player() {
        return robot.getPlayer();
    }

    /**
     * Wartime means a colonial pair at war without a war-declaration wait. The
     * Crown, natives, allies and peacetime rivals never count.
     */
    private boolean isWartimeEnemy(final Player other) {
        return other != player() && player().isColonialNation()
                && other.isColonialNation()
                && model().areHostile(player(), other)
                && !model().isAttackRestrained(player(), other);
    }

    /** A wartime unit on the map that this rival can see right now. */
    private boolean isThreatened(final Colony colony) {
        return player().getEnemyUnits().stream().filter(Unit::isAtPlaceLocation)
                .filter(unit -> isWartimeEnemy(unit.getOwner()))
                .filter(unit -> player().isCurrentlyVisible(unit.getLocation()))
                .anyMatch(unit -> colony.getLocation().getDistanceChebyshev(
                        unit.getLocation()) <= THREAT_DISTANCE);
    }

    /**
     * Armed land units are the force this policy moves: artillery and colonists
     * holding guns, the same kinds the shared combat rule counts as armed.
     */
    private static boolean isArmed(final Unit unit) {
        return unit instanceof UnitArtillery || unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).isHoldingGuns();
    }

    /** Armed land units standing on the map, in identity order. */
    private List<Unit> armedOnMap() {
        return player().getUnits().stream()
                .filter(RivalLandMilitaryCycle::isArmed)
                .filter(Unit::isAtPlaceLocation)
                .sorted(Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    /** On the map and idle, so it may take a step or fight. */
    private static boolean isAvailable(final Unit unit) {
        return unit.isAtPlaceLocation() && unit.getAction().isNoAction();
    }

    private static boolean isOnColony(final Unit unit,
            final List<Colony> colonies) {
        return colonies.stream().anyMatch(
                colony -> colony.getLocation().equals(unit.getLocation()));
    }

    /** Armed units inside the colony or on its square, of this rival. */
    private int countDefenders(final Colony colony) {
        return (int) (colony.getUnitsInsideColony().stream()
                .filter(RivalLandMilitaryCycle::isArmed).count()
                + model().getUnitsAt(colony.getLocation()).stream().filter(
                        unit -> unit.getOwner() == player() && isArmed(unit))
                        .count());
    }

    /**
     * An armed unit that holds or is starting a fortification on an owned
     * colony square that is not threatened is released only when it has a
     * feasible objective: a known target with a reachable approach at the
     * threshold. Without one it stays fortified, and any fortification
     * elsewhere, such as a player-ordered one in the field, is left alone.
     */
    private void releaseUnthreatenedDefenders(final List<Unit> armed,
            final List<Colony> threatened) {
        for (final Unit unit : armed) {
            if (model().canCancelFortify(unit) && !isOnColony(unit, threatened)
                    && player().getColonies().stream()
                            .anyMatch(colony -> colony.getLocation()
                                    .equals(unit.getLocation()))
                    && bestApproach(unit).isPresent()) {
                model().cancelFortify(unit);
            }
        }
    }

    /**
     * The undefended colony and spare unit with the shortest known route
     * between them; colonies and units are scanned in id order and only a
     * strictly shorter route replaces the choice, so ties keep the lower colony
     * id and then the lower unit id.
     */
    private Optional<Assignment> nearestPair(final List<Colony> undefended,
            final List<Unit> armed, final Set<Integer> committed) {
        Assignment best = null;
        for (final Colony colony : undefended) {
            for (final Unit unit : armed) {
                if (committed.contains(unit.getId()) || !isAvailable(unit)) {
                    continue;
                }
                final Optional<List<Location>> path = unit
                        .getKnownPath(colony.getLocation(), false);
                if (path.isPresent() && (best == null
                        || path.get().size() < best.distance)) {
                    best = new Assignment(colony, unit, path.get().size());
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /** The reachable threatened colony with the fewest defenders, by id. */
    private Optional<Colony> leastGarrisoned(final Unit unit,
            final List<Colony> threatened, final Map<Integer, Integer> sent) {
        return threatened.stream()
                .filter(colony -> unit.getKnownPath(colony.getLocation(), false)
                        .isPresent())
                .min(Comparator
                        .comparingInt((Colony colony) -> countDefenders(colony)
                                + sent.getOrDefault(colony.getId(), 0))
                        .thenComparingInt(Colony::getId));
    }

    private void send(final Unit unit, final Colony colony,
            final Set<Integer> committed, final Map<Integer, Integer> sent) {
        committed.add(unit.getId());
        sent.merge(colony.getId(), 1, Integer::sum);
        if (reach(unit, colony.getLocation())) {
            fortifyIfPossible(unit);
        }
    }

    /**
     * Walks the unit toward the destination while it has movement. Each step is
     * the shared known-route step, executed as an ordinary model move.
     *
     * @return whether the unit stands on the destination afterwards
     */
    private boolean reach(final Unit unit, final Location destination) {
        for (int step = 0; step < MAX_STEPS && robot.isActiveTurn()
                && isAvailable(unit) && unit.getMovementThirds() > 0
                && !unit.getLocation().equals(destination); step++) {
            final Optional<List<Location>> path = unit.getKnownPath(destination,
                    false);
            if (path.isEmpty() || path.get().isEmpty()
                    || !stepTo(unit, path.get().get(0))) {
                break;
            }
        }
        return isAvailable(unit) && unit.getLocation().equals(destination);
    }

    /**
     * One ordinary step. When the step is not affordable or the model refuses
     * it, nothing moves.
     *
     * @return whether the unit moved
     */
    private boolean stepTo(final Unit unit, final Location step) {
        if (!unit.canAffordStepTo(step)) {
            return false;
        }
        try {
            model().moveUnit(unit, Path.of(List.of(step)));
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            // The shared rules refused the step; nothing moved.
            LoggerFactory.getLogger(RivalLandMilitaryCycle.class)
                    .debug("Rival land step was refused", refused);
            return false;
        }
        robot.waitForAnimations();
        return true;
    }

    private void fortifyIfPossible(final Unit unit) {
        if (model().canFortify(unit)) {
            model().fortify(unit);
        }
    }

    /**
     * Attacks when a wartime enemy stands next to the unit at the threshold,
     * and otherwise advances toward the best known target. Each pass rechecks
     * the live state, so a vanished target or a changed relation ends the
     * advance.
     */
    private void advanceOffense(final Unit unit) {
        for (int step = 0; step < MAX_STEPS && robot.isActiveTurn()
                && isAvailable(unit); step++) {
            if (attackBestNeighbor(unit)) {
                return;
            }
            final Optional<Approach> next = bestApproach(unit);
            if (next.isEmpty() || next.get().step.isEmpty()
                    || !stepTo(unit, next.get().step.get())) {
                return;
            }
        }
    }

    /**
     * Attacks the adjacent enemy with the highest shared win probability when
     * that probability reaches the threshold. Declarations are not requested,
     * so a peaceful or allied neighbour is never attacked. The shared model
     * revalidates the prepared order when it executes it.
     *
     * @return whether the attack was made
     */
    private boolean attackBestNeighbor(final Unit unit) {
        AttackOrder best = null;
        double bestProbability = 0;
        for (final Location neighbor : unit.getLocation().getNeighbors()) {
            final Optional<AttackOrder> order = unit.prepareAttack(neighbor,
                    false);
            if (order.isEmpty() || order.get().getCombatStrengths().isEmpty()) {
                continue;
            }
            final double probability = order.get().getCombatStrengths().get()
                    .getAttackerWinProbability();
            if (isWinning(probability)
                    && (best == null || probability > bestProbability
                            || Double.compare(probability, bestProbability) == 0
                                    && order.get().getDefender().getId() < best
                                            .getDefender().getId())) {
                best = order.get();
                bestProbability = probability;
            }
        }
        if (best == null) {
            return false;
        }
        try {
            best.execute();
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            // The state changed after the quote; the next turn replans.
            LoggerFactory.getLogger(RivalLandMilitaryCycle.class)
                    .debug("Rival land attack was refused", refused);
            return false;
        }
        robot.waitForAnimations();
        return true;
    }

    /**
     * The best approach to a known wartime target, or empty when no target has
     * a reachable approach at the threshold.
     */
    private Optional<Approach> bestApproach(final Unit unit) {
        Approach best = null;
        for (final Location target : targets()) {
            final boolean inSight = player().isCurrentlyVisible(target);
            for (final Location square : target.getNeighbors()) {
                final Approach candidate = approach(unit, target, square,
                        inSight);
                if (candidate != null && (best == null
                        || APPROACH_ORDER.compare(candidate, best) < 0)) {
                    best = candidate;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * The legal approach square beside a target with its known route and, for a
     * target in sight, the shared quote of the battle fought from that square.
     * Null when the square is off the map, cannot be reached or quotes below
     * the threshold.
     */
    private Approach approach(final Unit unit, final Location target,
            final Location square, final boolean inSight) {
        if (!model().getMap().isValid(square)) {
            return null;
        }
        double probability = 0;
        int defenderId = UNKNOWN_DEFENDER_ID;
        if (inSight) {
            final Optional<AttackOrder> order = unit.previewAttack(square,
                    target);
            if (order.isEmpty() || order.get().getCombatStrengths().isEmpty()) {
                return null;
            }
            probability = order.get().getCombatStrengths().get()
                    .getAttackerWinProbability();
            if (!isWinning(probability)) {
                return null;
            }
            defenderId = order.get().getDefender().getId();
        }
        if (square.equals(unit.getLocation())) {
            return new Approach(target, square, 0, probability, defenderId,
                    Optional.empty());
        }
        if (!unit.isPossibleToMoveAt(square)) {
            return null;
        }
        final Optional<List<Location>> path = unit.getKnownPath(square, false);
        if (path.isEmpty() || path.get().isEmpty()) {
            return null;
        }
        return new Approach(target, square, path.get().size(), probability,
                defenderId, Optional.of(path.get().get(0)));
    }

    /**
     * Wartime enemy units in present sight and every wartime colony the rival
     * knows, live in sight and remembered outside it. Units out of sight are
     * never listed, so hidden enemies stay unknown.
     */
    private Set<Location> targets() {
        final Set<Location> targets = new TreeSet<>(COORDINATES);
        player().getEnemyUnits().stream().filter(Unit::isAtPlaceLocation)
                .filter(unit -> isWartimeEnemy(unit.getOwner()))
                .filter(unit -> player().isCurrentlyVisible(unit.getLocation()))
                .forEach(unit -> targets.add(unit.getLocation()));
        for (final ColonySighting sighting : player().getKnownColonies()) {
            if (isWartimeEnemy(sighting.getOwner())) {
                targets.add(sighting.getLocation());
            }
        }
        return targets;
    }

    /** A spare unit chosen to defend an undefended threatened colony. */
    private static final class Assignment {
        private final Colony colony;
        private final Unit unit;
        private final int distance;

        private Assignment(final Colony colony, final Unit unit,
                final int distance) {
            this.colony = colony;
            this.unit = unit;
            this.distance = distance;
        }
    }

    /** One approach square beside a target, with its known route and quote. */
    private static final class Approach {
        private final Location target;
        private final Location square;
        private final int distance;
        private final double probability;
        private final int defenderId;
        private final Optional<Location> step;

        private Approach(final Location target, final Location square,
                final int distance, final double probability,
                final int defenderId, final Optional<Location> step) {
            this.target = target;
            this.square = square;
            this.distance = distance;
            this.probability = probability;
            this.defenderId = defenderId;
            this.step = step;
        }
    }
}
