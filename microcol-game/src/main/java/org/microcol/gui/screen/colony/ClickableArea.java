package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_SIZE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Holds rectangular areas (fields) in on-screen coordinates. It convert
 * on-screen coordinates to direction of field. Direction is {@link Location}.
 */
public final class ClickableArea {

    private final Map<Rectangle, Location> areas;

    public ClickableArea() {
        areas = new HashMap<>();
        final List<Location> locs = Lists.newArrayList(Location.CENTER.getNeighbors());
        locs.forEach(loc -> {
            final Point p = Point.of(loc).add(TILE_SIZE);
            Rectangle rect = Rectangle.ofPointAndSize(p, TILE_SIZE);
            areas.put(rect, loc);
        });
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
