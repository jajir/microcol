package org.microcol.gui.gamepanel;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;

import javafx.scene.canvas.GraphicsContext;

/**
 * Walk animation consist of many locations. Class is responsible for animating
 * walk from one location to another one.
 *
 */
public class AnimationWalkParticle {

    private final Location locationFrom;

    /**
     * Contains locations for move between two tiles.
     */
    private final List<Point> partialPath = new ArrayList<>();

    private final PaintService paintService;

    AnimationWalkParticle(final PaintService paintService, final Location locationFrom,
            final Location locationTo, final PathPlanning pathPlanning) {
        this.paintService = paintService;
        this.locationFrom = locationFrom;
        final Point from = Point.of(0, 0);
        final Point to = Point.of(Location.of(locationTo.getX() - locationFrom.getX(),
                locationTo.getY() - locationFrom.getY()));
        pathPlanning.paintPath(from, to, point -> partialPath.add(point));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("locationFrom", locationFrom)
                .add("", partialPath).toString();
    }

    /**
     * Provide information if animation should continue.
     * 
     * @return return <code>true</code> when not all animation was drawn, it
     *         return <code>false</code> when all animation is done
     */
    boolean hasNextStep() {
        return !partialPath.isEmpty();
    }

    void paint(final GraphicsContext graphics, final Area area, final Unit unit) {
        final Point point = partialPath.remove(0).add(area.convertToPoint(locationFrom));
        if (area.isVisibleScreenPoint(point)) {
            paintService.paintUnit(graphics, point, unit, ImageProvider.IMG_TILE_MODE_MOVE);
        }
    }

}
