package org.microcol.gui.colony;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Holds rectangular areas (fields) in on-screen coordinates. It convert
 * on-screen coordinates to direction of field. Direction is {@link Location}.
 */
public class ClickableArea {

    private final Map<Rectangle, Location> areas;

    public ClickableArea() {
        areas = new HashMap<>();
        final Point square = Point.of(GamePanelView.TILE_WIDTH_IN_PX,
                GamePanelView.TILE_WIDTH_IN_PX);
        final Point shift = Point.of(GamePanelView.TILE_WIDTH_IN_PX,
                GamePanelView.TILE_WIDTH_IN_PX);
        final Location center = Location.of(0, 0);
        final List<Location> locs = Lists.newArrayList(center.getNeighbors());
        locs.forEach(loc -> {
            final Point p = Point.of(loc).add(shift);
            Rectangle rect = Rectangle.ofPointAndSize(p, square);
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
