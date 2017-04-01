package org.microcol.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

class PathFinder {
	final Ship ship;
	final Location start;
	final Location destination;
	final boolean excludeDestination;

	PathFinder(final Ship ship, final Location start, final Location destination, final boolean excludeDestination) {
		this.ship = Preconditions.checkNotNull(ship);
		this.start = Preconditions.checkNotNull(start);
		this.destination = Preconditions.checkNotNull(destination);
		this.excludeDestination = excludeDestination;

		Preconditions.checkArgument(!start.equals(destination), "Start and destination must be different (%s).", start);
	}

	List<Location> find() {
		final Set<PathFindingNode> openSet = new HashSet<>();
		openSet.add(new PathFindingNode(null, start, start.getDistance(destination)));
		final Set<Location> closedSet = new HashSet<>();
		PathFindingNode current = null;

		while (!openSet.isEmpty()) {
			current = findLowest(openSet);
			if ((excludeDestination && current.getLocation().isAdjacent(destination))
				|| current.getLocation().equals(destination)) {
				return createList(current);
			}
			openSet.remove(current);
			closedSet.add(current.getLocation());
			final List<Location> neighbors = current.getLocation().getNeighbors();
			for (final Location neighbor : neighbors) {
				if (!closedSet.contains(neighbor) && ship.isMoveable(neighbor)) {
					final PathFindingNode oldNode = get(openSet, neighbor);
					final PathFindingNode newNode = new PathFindingNode(current, neighbor, neighbor.getDistance(destination));
					if (oldNode == null) {
						openSet.add(newNode);
					} else {
						if (newNode.getCost() < oldNode.getCost()) {
							oldNode.setParent(newNode.getParent());
						}
					}
				}
			}
		}

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
