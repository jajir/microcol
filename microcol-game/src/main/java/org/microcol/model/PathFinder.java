package org.microcol.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

final class PathFinder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Unit unit;
    private final Location start;
    private final Location destination;
    private final boolean excludeDestination;

    PathFinder(final Unit unit, final Location start, final Location destination,
            final boolean excludeDestination) {
        this.unit = Preconditions.checkNotNull(unit);
        this.start = Preconditions.checkNotNull(start);
        this.destination = Preconditions.checkNotNull(destination);
        this.excludeDestination = excludeDestination;

        Preconditions.checkArgument(!start.equals(destination),
                "Start and destination must be different (%s).", start);
    }

    List<Location> find() {
        long startTime = System.currentTimeMillis();

        if (!excludeDestination && !unit.isPossibleToMoveAt(destination)) {
            logger.debug("Path finding finished in {} ms.", System.currentTimeMillis() - startTime);

            return null;
        }

        final List<PathFindingNode> openList = new ArrayList<>();
        openList.add(new PathFindingNode(null, start, start.getDistanceManhattan(destination)));
        final Set<Location> closedSet = new HashSet<>();
        PathFindingNode current = null;

        while (!openList.isEmpty()) {
            current = findLowest(openList);
            if (excludeDestination && current.getLocation().isNeighbor(destination)
                    || current.getLocation().equals(destination)) {
                logger.debug("Path finding finished in {} ms.",
                        System.currentTimeMillis() - startTime);

                return createList(current);
            }
            openList.remove(current);
            closedSet.add(current.getLocation());
            final List<Location> neighbors = current.getLocation().getNeighbors();
            for (final Location neighbor : neighbors) {
                if (!closedSet.contains(neighbor) && unit.isPossibleToMoveAt(neighbor)) {
                    final PathFindingNode oldNode = get(openList, neighbor);
                    final PathFindingNode newNode = new PathFindingNode(current, neighbor,
                            neighbor.getDistanceManhattan(destination));
                    if (oldNode == null) {
                        openList.add(newNode);
                    } else {
                        if (newNode.getCost() < oldNode.getCost()) {
                            oldNode.setParent(newNode.getParent());
                        }
                    }
                }
            }
        }

        logger.debug("Path finding finished in {} ms.", System.currentTimeMillis() - startTime);

        return null;
    }

    private PathFindingNode findLowest(final Collection<PathFindingNode> nodes) {
        PathFindingNode lowestNode = null;
        for (final PathFindingNode node : nodes) {
            if (lowestNode == null || node.getScore() < lowestNode.getScore()) {
                lowestNode = node;
            }
        }

        return lowestNode;
    }

    private PathFindingNode get(final Collection<PathFindingNode> nodes, final Location location) {
        for (final PathFindingNode node : nodes) {
            if (node.getLocation().equals(location)) {
                return node;
            }
        }

        return null;
    }

    private List<Location> createList(PathFindingNode node) {
        final List<Location> list = new ArrayList<>();

        while (node != null && node.getParent() != null) {
            list.add(node.getLocation());
            node = node.getParent();
        }
        Collections.reverse(list);

        return list;
    }
}
