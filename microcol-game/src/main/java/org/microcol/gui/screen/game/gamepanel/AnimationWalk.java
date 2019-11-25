package org.microcol.gui.screen.game.gamepanel;

import java.util.List;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.util.PaintService;
import org.microcol.gui.util.PathPlanningService;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Draw walk animation based on predefined path. It animate just neighbors step.
 * 
 */
final class AnimationWalk implements Animation {

    private final PaintService paintService;

    /**
     * Moving unit.
     */
    private final Unit unit;

    private final Location locationFrom;

    /**
     * Contains locations for move between two tiles.
     */
    private final List<Point> partialPath;

    private final Direction unitOrientation;

    AnimationWalk(final PathPlanningService pathPlanningService, final Location locationFrom,
            final Location locationTo, final Unit unit, final PaintService paintService,
            final ExcludePainting excludePainting, final Direction unitOrientation) {
        this.locationFrom = Preconditions.checkNotNull(locationFrom);
        Preconditions.checkNotNull(locationTo);
        this.paintService = Preconditions.checkNotNull(paintService);
        Preconditions.checkNotNull(excludePainting);
        Preconditions.checkNotNull(locationFrom.getNeighbors().contains(locationTo),
                "Start locations '%s' is not neighbors od end location '%s'", locationFrom,
                locationTo);
        this.unit = Preconditions.checkNotNull(unit);
        this.unitOrientation = Preconditions.checkNotNull(unitOrientation);
        excludePainting.excludeUnit(unit);

        final Point from = Point.ZERO;
        final Location diff = locationTo.sub(locationFrom);
        final Point to = Tile.ofLocation(Location.of(diff.getX(), diff.getY()))
                .getBottomRightCorner();
        partialPath = pathPlanningService.getPathLimitSpeed(from, to);

    }

    @Override
    public void nextStep() {
        if (!partialPath.isEmpty()) {
            partialPath.remove(0);
        }
    }

    @Override
    public boolean hasNextStep() {
        return !partialPath.isEmpty();
    }

    @Override
    public void paint(final GraphicsContext graphics, final Area area) {
        boolean wasNotPainted = true;
        while (hasNextStep() && wasNotPainted) {
            final Point point = partialPath.get(0).add(area.convertToCanvasPoint(locationFrom));
            if (area.isVisibleCanvasPoint(point)) {
                paintService.paintUnit(graphics, point, unit, unitOrientation);
                wasNotPainted = false;
            }
            nextStep();
        }

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("partialPath", partialPath)
                .add("unit", unit).toString();
    }

    @Override
    public boolean canBePainted(final Area area) {
        if (hasNextStep()) {
            final Point point = partialPath.get(0).add(area.convertToCanvasPoint(locationFrom));
            return area.isVisibleCanvasPoint(point);
        }
        return false;
    }

}
