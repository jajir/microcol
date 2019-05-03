package org.microcol.gui.screen.colony;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Holds rectangular areas (fields) in on-screen coordinates. It convert
 * on-screen coordinates to direction of field. Direction is {@link Location}.
 */
public final class ClickableArea {

    private final Map<ColonyFieldTile, Location> areas;

    public ClickableArea() {
        areas = new HashMap<>();
        final List<Location> locs = Lists.newArrayList(Location.CENTER.getNeighbors());
        locs.forEach(this::addDirection);
    }

    private void addDirection(final Location loc) {
        final ColonyFieldTile colonyFieldTile = ColonyFieldTile.ofLocation(loc);
        areas.put(colonyFieldTile, loc);
    }

    public Optional<Location> getDirection(final Point point) {
        return areas.entrySet().stream().filter(entry -> entry.getKey().isIn(point))
                .map(entry -> entry.getValue()).findAny();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ClickableArea.class).add("areas", areas).toString();
    }

}
