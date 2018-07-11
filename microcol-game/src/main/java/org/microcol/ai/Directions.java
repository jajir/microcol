package org.microcol.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Helps to navigate unit's in a random way oner map. For each unit suggest
 * direction to move.
 */
public final class Directions {

    private final Random random;
    private final Map<Unit, Location> lastDirections;

    public Directions() {
        this.random = new Random();
        this.lastDirections = new HashMap<>();
    }

    public Location getLastDirection(final Unit unit) {
        if (lastDirections.get(unit) == null) {
            lastDirections.put(unit,
                    Location.DIRECTIONS.get(random.nextInt(Location.DIRECTIONS.size())));
        }
        return lastDirections.get(unit);
    }

    public void resetDirection(final Unit unit) {
        final Location lastDirection = getLastDirection(unit);
        final List<Location> directions = new ArrayList<>(Location.DIRECTIONS);
        directions.remove(lastDirection);
        Preconditions.checkState(directions.size() == 7,
                "Directions size should be 7. Invalid location (%s)", lastDirection);
        lastDirections.put(unit, directions.get(random.nextInt(directions.size())));
    }

}
