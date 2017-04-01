package org.microcol.model;

import com.google.common.base.Preconditions;

class PathFindingNode {
	// Parent of start node is null.
	private PathFindingNode parent;
	private final Location location;
	private final int distance;

	PathFindingNode(final PathFindingNode parent, final Location location, final int distance) {
		this.parent = parent;
		this.location = Preconditions.checkNotNull(location);
		this.distance = distance;
	}

	PathFindingNode getParent() {
		return parent;
	}

	void setParent(final PathFindingNode parent) {
		this.parent = parent;
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
}
