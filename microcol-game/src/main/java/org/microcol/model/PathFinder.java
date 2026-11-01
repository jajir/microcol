package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Cheapest routes by entry cost in movement thirds. A route the unit can walk
 * in this turn is preferred; otherwise the cheapest route at all is returned so
 * that planning can show it and the move is rejected by
 * {@link Unit#canAffordPath}. A finder for a {@link MovementProfile} without a
 * unit, and one for trade route travel ({@link #cheapestOverall}), has no turn
 * preference and always returns the cheapest route at all. The search is
 * deterministic: unchanged squares, costs and start give the same route,
 * including the choice among routes of equal cost.
 */
final class PathFinder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Moving unit, or {@code null} for a preview without a unit. */
    private final AbstractUnit unit;

    private final MovementProfile profile;

    /**
     * Whether a route the unit can walk within its remaining movement, with one
     * terminal expensive step from a full allowance, is preferred to the
     * cheapest route at all. Manual moves and go-to orders prefer it; trade
     * route travel and previews do not.
     */
    private final boolean preferThisTurn;

    /** Whether a ship route must not contain a high-sea departure step. */
    private final boolean avoidDeparture;

    /**
     * Squares a route may use at all. It is tested before the terrain or the
     * cost of a square is read, so an excluded square never shapes a route.
     */
    private final Predicate<Location> usable;

    PathFinder(final AbstractUnit unit) {
        this(unit, false);
    }

    /**
     * @param unit           required moving unit
     * @param avoidDeparture when {@code true}, a ship route never contains a
     *                       step that would depart to the high sea
     */
    PathFinder(final AbstractUnit unit, final boolean avoidDeparture) {
        this(unit, avoidDeparture, location -> true);
    }

    /**
     * @param unit           required moving unit
     * @param avoidDeparture when {@code true}, a ship route never contains a
     *                       step that would depart to the high sea
     * @param usable         required test of the squares a route may use, such
     *                       as the squares its owner has explored
     */
    PathFinder(final AbstractUnit unit, final boolean avoidDeparture,
            final Predicate<Location> usable) {
        this(Preconditions.checkNotNull(unit), MovementProfile.of(unit),
                avoidDeparture, usable, true);
    }

    /**
     * Finder for automatic trade route travel. It selects the route of lowest
     * total entry cost under the unit's own step rules, exactly as the route
     * preview does, and never prefers a dearer route only because the unit
     * could finish it in this turn. Selecting a route spends nothing: each step
     * is still paid from the unit's remaining movement when it is made.
     *
     * @param unit           required moving unit
     * @param avoidDeparture when {@code true}, a ship route never contains a
     *                       step that would depart to the high sea
     * @param usable         required test of the squares a route may use
     * @return the finder
     */
    static PathFinder cheapestOverall(final AbstractUnit unit,
            final boolean avoidDeparture, final Predicate<Location> usable) {
        return new PathFinder(Preconditions.checkNotNull(unit),
                MovementProfile.of(unit), avoidDeparture, usable, false);
    }

    /**
     * Finder for a route preview. No unit exists, so routes are priced without
     * a turn allowance and nothing in the model is read except the profile.
     *
     * @param profile        required step rule and costs
     * @param avoidDeparture when {@code true}, a ship route never contains a
     *                       step that would depart to the high sea
     * @param usable         required test of the squares a route may use
     */
    PathFinder(final MovementProfile profile, final boolean avoidDeparture,
            final Predicate<Location> usable) {
        this(null, profile, avoidDeparture, usable, false);
    }

    private PathFinder(final AbstractUnit unit, final MovementProfile profile,
            final boolean avoidDeparture, final Predicate<Location> usable,
            final boolean preferThisTurn) {
        this.unit = unit;
        this.preferThisTurn = preferThisTurn;
        this.profile = Preconditions.checkNotNull(profile);
        this.avoidDeparture = avoidDeparture;
        this.usable = Preconditions.checkNotNull(usable);
    }

    /**
     * Find a route from the unit to the destination.
     *
     * @param destination        required destination
     * @param excludeDestination when {@code true}, a route to a square next to
     *                           the destination is enough
     * @return route without the start square, or {@code null} when there is
     *         none
     */
    List<Location> find(final Location destination,
            final boolean excludeDestination) {
        Preconditions.checkState(unit != null, "A preview has no unit");
        return find(unit.getLocation(), destination, excludeDestination);
    }

    /**
     * Find a route from a start square read once by the caller. The unit's
     * place is not read again, so a concurrent move cannot change the start
     * during the search.
     *
     * @param start              required square of the unit's place snapshot
     * @param destination        required destination
     * @param excludeDestination when {@code true}, a route to a square next to
     *                           the destination is enough
     * @return route without the start square, or {@code null} when there is
     *         none
     */
    List<Location> find(final Location start, final Location destination,
            final boolean excludeDestination) {
        final long startTime = System.currentTimeMillis();
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkArgument(!start.equals(destination),
                "Start and destination must be different (%s).", start);

        if (!excludeDestination && (!usable.test(destination)
                || !profile.canEnter(destination))) {
            logger.debug("Path finding finished in {} ms.",
                    System.currentTimeMillis() - startTime);
            return null;
        }
        final Predicate<Location> isTarget = location -> excludeDestination
                && location.isNeighbor(destination)
                || location.equals(destination);
        final Optional<Node> found = cheapest(start, isTarget);
        logger.debug("Path finding finished in {} ms.",
                System.currentTimeMillis() - startTime);
        return found.map(this::createList).orElse(null);
    }

    /**
     * Find the cheapest route to the nearest square that satisfies a goal, with
     * the same cost rules and turn preference as {@link #find}. The start
     * square is never itself the goal, so the caller tests it first.
     *
     * @param start  required square of the unit's place snapshot
     * @param isGoal required test of the squares to reach
     * @return route without the start square, or {@code null} when none exists
     */
    List<Location> findNearest(final Location start,
            final Predicate<Location> isGoal) {
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(isGoal);
        final Predicate<Location> isTarget = location -> !location.equals(start)
                && isGoal.test(location);
        return cheapest(start, isTarget).map(this::createList).orElse(null);
    }

    /**
     * Find all squares the unit can end its move on in this turn, excluding its
     * own square.
     *
     * @return reachable locations; the returned set can be changed by caller
     */
    Set<Location> findReachable() {
        Preconditions.checkState(unit != null, "A preview has no unit");
        final Set<Location> reached = new HashSet<>();
        search(unit.getLocation(), unit.getMovementThirds(),
                unit.hasFullMovement(), location -> false, reached);
        return reached;
    }

    /**
     * A manual move or go-to first looks for a route within the unit's
     * remaining movement; trade route travel and a preview take the cheapest
     * route at once.
     */
    private Optional<Node> cheapest(final Location start,
            final Predicate<Location> isTarget) {
        if (unit != null && preferThisTurn) {
            final Optional<Node> affordable = search(start,
                    unit.getMovementThirds(), unit.hasFullMovement(), isTarget,
                    new HashSet<>());
            if (affordable.isPresent()) {
                return affordable;
            }
        }
        return search(start, Integer.MAX_VALUE, false, isTarget,
                new HashSet<>());
    }

    /**
     * Dijkstra search by entry cost. A square costing more than the allowance
     * is entered only as a terminal step from a full allowance.
     *
     * @param start         required square the search starts from
     * @param allowance     movement thirds that routes must not exceed
     * @param fullAllowance whether the unit may take one terminal expensive
     *                      step from its start
     * @param isTarget      stops the search at the first matching square
     * @param reached       receives every square the search settled, except the
     *                      start
     * @return the target node, or empty when no target was settled
     */
    private Optional<Node> search(final Location start, final int allowance,
            final boolean fullAllowance, final Predicate<Location> isTarget,
            final Set<Location> reached) {
        final PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.cost));
        final Set<Location> closed = new HashSet<>();
        open.add(new Node(start, 0, null, false));
        while (!open.isEmpty()) {
            final Node current = open.poll();
            if (!closed.add(current.location)) {
                continue;
            }
            if (isTarget.test(current.location)) {
                return Optional.of(current);
            }
            if (current.parent != null) {
                reached.add(current.location);
            }
            if (current.terminal) {
                continue;
            }
            for (final Location neighbor : current.location.getNeighbors()) {
                if (closed.contains(neighbor) || !usable.test(neighbor)
                        || !profile.canEnter(neighbor)) {
                    continue;
                }
                if (avoidDeparture && profile.isShip()
                        && profile.getModel().getHighSea()
                                .isDepartureStep(current.location, neighbor)) {
                    continue;
                }
                final int cost = current.cost + profile
                        .getEntryCostThirds(current.location, neighbor);
                if (cost <= allowance) {
                    open.add(new Node(neighbor, cost, current, false));
                } else if (fullAllowance && current.parent == null) {
                    open.add(new Node(neighbor, cost, current, true));
                }
            }
        }
        return Optional.empty();
    }

    private List<Location> createList(final Node node) {
        final List<Location> list = new ArrayList<>();
        Node current = node;
        while (current != null && current.parent != null) {
            list.add(current.location);
            current = current.parent;
        }
        Collections.reverse(list);
        return list;
    }

    /** A settled or queued square with its total cost and route. */
    private static final class Node {
        private final Location location;
        private final int cost;
        private final Node parent;
        /** Terminal nodes end the route, as a full allowance may spend. */
        private final boolean terminal;

        private Node(final Location location, final int cost, final Node parent,
                final boolean terminal) {
            this.location = location;
            this.cost = cost;
            this.parent = parent;
            this.terminal = terminal;
        }
    }
}
