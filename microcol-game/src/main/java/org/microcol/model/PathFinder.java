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
 * {@link Unit#canAffordPath}.
 */
final class PathFinder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AbstractUnit unit;

    PathFinder(final AbstractUnit unit) {
        this.unit = Preconditions.checkNotNull(unit);
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
        final long startTime = System.currentTimeMillis();
        final Location start = unit.getLocation();
        Preconditions.checkNotNull(destination);
        Preconditions.checkArgument(!start.equals(destination),
                "Start and destination must be different (%s).", start);

        if (!excludeDestination && !unit.isPossibleToMoveAt(destination)) {
            logger.debug("Path finding finished in {} ms.",
                    System.currentTimeMillis() - startTime);
            return null;
        }
        final Predicate<Location> isTarget = location -> excludeDestination
                && location.isNeighbor(destination)
                || location.equals(destination);
        final Optional<Node> affordable = search(unit.getMovementThirds(),
                unit.hasFullMovement(), isTarget, new HashSet<>());
        final Optional<Node> found = affordable.isPresent() ? affordable
                : search(Integer.MAX_VALUE, false, isTarget, new HashSet<>());
        logger.debug("Path finding finished in {} ms.",
                System.currentTimeMillis() - startTime);
        return found.map(this::createList).orElse(null);
    }

    /**
     * Find all squares the unit can end its move on in this turn, excluding its
     * own square.
     *
     * @return reachable locations; the returned set can be changed by caller
     */
    Set<Location> findReachable() {
        final Set<Location> reached = new HashSet<>();
        search(unit.getMovementThirds(), unit.hasFullMovement(),
                location -> false, reached);
        return reached;
    }

    /**
     * Dijkstra search by entry cost. A square costing more than the allowance
     * is entered only as a terminal step from a full allowance.
     *
     * @param allowance     movement thirds that routes must not exceed
     * @param fullAllowance whether the unit may take one terminal expensive
     *                      step from its start
     * @param isTarget      stops the search at the first matching square
     * @param reached       receives every square the search settled, except the
     *                      start
     * @return the target node, or empty when no target was settled
     */
    private Optional<Node> search(final int allowance,
            final boolean fullAllowance, final Predicate<Location> isTarget,
            final Set<Location> reached) {
        final PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.cost));
        final Set<Location> closed = new HashSet<>();
        open.add(new Node(unit.getLocation(), 0, null, false));
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
                if (closed.contains(neighbor)
                        || !unit.isPossibleToMoveAt(neighbor)) {
                    continue;
                }
                final int cost = current.cost
                        + unit.getEntryCostThirds(current.location, neighbor);
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
