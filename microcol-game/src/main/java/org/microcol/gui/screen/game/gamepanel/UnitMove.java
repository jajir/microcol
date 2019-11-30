package org.microcol.gui.screen.game.gamepanel;

import java.util.Collections;
import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Encapsulate moving unit, move target location and path to it.
 */
final class UnitMove {

    private final Unit unit;

    private final List<Location> path;

    UnitMove(final Unit unit, final Location targetLocation) {
        this.unit = Preconditions.checkNotNull(unit);
        Preconditions.checkNotNull(targetLocation);
        if (unit.isPossibleToMoveAt(targetLocation)) {
            path = unit.getPath(targetLocation).orElse(null);
        } else {
            path = null;
        }
    }

    public int requiredActionPoints() {
        return path.size();
    }

    public boolean isOneTurnMove() {
        if (path == null) {
            return false;
        } else {
            final int actionPoint = unit.getActionPoints();
            return path.size() <= actionPoint;
        }
    }

    public List<Location> getPath() {
        if (path == null) {
            return Collections.emptyList();
        } else {
            return path;
        }
    }

}
