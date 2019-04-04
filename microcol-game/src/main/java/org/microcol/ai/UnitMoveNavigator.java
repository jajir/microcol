package org.microcol.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Helps unit to keep one chosen direction of move.
 */
public final class UnitMoveNavigator {

    private final Random random;

    private final Map<Unit, Direction> lastDirections;

    public UnitMoveNavigator() {
        this.random = new Random();
        this.lastDirections = new HashMap<>();
    }

    /**
     * For given unit get last used direction and if it's not there choose new
     * random one.
     *
     * @param unit
     *            required unit
     * @return direction of move
     */
    public Location getLastVector(final Unit unit) {
        return getLastDirection(unit).getVector();
    }

    private Direction getLastDirection(final Unit unit) {
        Direction lastDirection = lastDirections.get(unit);
        if (lastDirection == null) {
            lastDirection = getNextRandomDirection(Direction.getAll());
            lastDirections.put(unit, lastDirection);
        }
        return lastDirection;
    }

    /**
     * Choose another random direction different from previous one.
     *
     * @param unit
     *            required unit
     */
    public void resetDirection(final Unit unit) {
        if (lastDirections.containsKey(unit)) {
            final Direction lastDirection = lastDirections.get(unit);
            final List<Direction> directions = new ArrayList<>(Direction.getAll());
            directions.remove(lastDirection);
            Preconditions.checkState(directions.size() == 7,
                    "Directions size should be 7. Invalid last direction (%s)", lastDirection);
            lastDirections.put(unit, getNextRandomDirection(directions));
        } else {
            lastDirections.put(unit, getNextRandomDirection(Direction.getAll()));
        }
    }

    private Direction getNextRandomDirection(final List<Direction> directions) {
        return directions.get(random.nextInt(directions.size()));
    }

}
