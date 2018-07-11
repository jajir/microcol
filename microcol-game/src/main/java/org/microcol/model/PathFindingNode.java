package org.microcol.model;

import com.google.common.base.Preconditions;

final class PathFindingNode {
    // Parent of start node is null.
    private PathFindingNode parent;
    private final Location location;
    private int cost;
    private final int distance;

    PathFindingNode(final PathFindingNode parent, final Location location, final int distance) {
        this.parent = parent;
        this.location = Preconditions.checkNotNull(location);
        this.cost = calculateCost();
        this.distance = distance;
    }

    private int calculateCost() {
        return parent != null ? parent.calculateCost() + 1 : 0;
    }

    PathFindingNode getParent() {
        return parent;
    }

    void setParent(final PathFindingNode parent) {
        this.parent = parent;
        this.cost = calculateCost();
    }

    Location getLocation() {
        return location;
    }

    int getCost() {
        return cost;
    }

    int getScore() {
        return cost + distance;
    }
}
