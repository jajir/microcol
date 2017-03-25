package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

class PathFinder {
	private static class Node {
		final Node parent;
		final Location location;
		final int distance;

		Node(final Node parent, final Location location, final int distance) {
			this.parent = parent;
			this.location = location;
			this.distance = distance;
		}

		Node getParent() {
			return parent;
		}

		Location getLocation() {
			return location;
		}

		int getCost() {
			return parent != null ? parent.getCost() + 1 : 0;
		}

		int getDistance() {
			return distance;
		}

		int getScore() {
			return getCost() + getDistance();
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}

		@Override
		public boolean equals(final Object object) {
			return location.equals(object);
		}
	}

	final Ship ship;
	final Location start;
	final Location destination;

	PathFinder(final Ship ship, final Location start, final Location destination) {
		this.ship = Preconditions.checkNotNull(ship);
		this.start = Preconditions.checkNotNull(start);
		this.destination = Preconditions.checkNotNull(destination);
	}

	List<Location> search() {
		Set<Node> openSet = new HashSet<>();
		openSet.add(new Node(null, start, start.getDistance(destination)));
		Set<Node> closedSet = new HashSet<>();
		Node current = null;

		while (!openSet.isEmpty()) {
			current = findNext(openSet);
			if (current.getLocation().equals(destination)) {
				break;
			}
			openSet.remove(current);
			closedSet.add(current);
			List<Location> neighbors = current.getLocation().getNeighbors();
			for (Location neighbor : neighbors) {
				if (!contains(closedSet, neighbor) && ship.isMoveable(neighbor)) {
					Node node = null;
					Iterator<Node> iter = openSet.iterator();
					while (iter.hasNext()) {
						node = iter.next();
						if (node.getLocation().equals(neighbor)) {
							iter.remove(); // FIXME JKA CO KDYZ SE NA TO NECO ODKAZUJE?
							break;
						}
						node = null;
					}
					Node nnn = new Node(current, neighbor, neighbor.getDistance(destination));
					if (node == null) {
						openSet.add(nnn);
					} else {
						if (nnn.getCost() < node.getCost()) {
							openSet.add(nnn);
						} else {
							openSet.add(node);
						}
					}
				}
			}
		}

		return getList(current);
	}

	private Node findNext(final Set<Node> openSet) {
		Node found = null;
		for (Node node : openSet) {
			if (found == null || node.getScore() < found.getScore()) {
				found = node;
			}
		}

		return found;
	}

	private boolean contains(final Set<Node> closedSet, final Location location) {
		for (Node node : closedSet) {
			if (node.getLocation().equals(location)) {
				return true;
			}
		}

		return false;
	}

	private List<Location> getList(Node node) {
		List<Location> list = new ArrayList<>();

		while (node != null) {
			list.add(node.getLocation());
			node = node.getParent();
		}
		Collections.reverse(list);

		return list;
	}
}
