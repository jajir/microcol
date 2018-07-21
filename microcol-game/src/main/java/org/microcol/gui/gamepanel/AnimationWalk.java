package org.microcol.gui.gamepanel;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
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
public final class AnimationWalk implements Animation {

    private final PaintService paintService;

    /**
     * Moving unit.
     */
    private final Unit unit;

    private final Location locationFrom;

    /**
     * Contains locations for move between two tiles.
     */
    private final List<Point> partialPath = new ArrayList<>();

    private final Direction unitOrientation;

    AnimationWalk(final PathPlanning pathPlanning, final Location locationFrom,
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

        final Point from = Point.of(0, 0);
        final Point to = Point.of(Location.of(locationTo.getX() - locationFrom.getX(),
                locationTo.getY() - locationFrom.getY()));
        pathPlanning.paintPath(from, to, point -> partialPath.add(point));

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
            final Point point = partialPath.get(0).add(area.convertToPoint(locationFrom));
            if (area.isVisibleScreenPoint(point)) {
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
            final Point point = partialPath.get(0).add(area.convertToPoint(locationFrom));
            return area.isVisibleScreenPoint(point);
        }
        return false;
    }

}
