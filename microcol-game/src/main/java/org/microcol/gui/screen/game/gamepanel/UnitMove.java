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

    /**
     * The model decides affordability, so terrain costs and the full-turn
     * exception are the same here as when the unit moves.
     *
     * @return {@code true} when the whole route can be walked in this turn
     */
    public boolean isOneTurnMove() {
        return path != null && unit.canAffordPath(path);
    }

    public List<Location> getPath() {
        if (path == null) {
            return Collections.emptyList();
        } else {
            return path;
        }
    }

}
