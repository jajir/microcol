package org.microcol.model.event;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is send when movement is finished. Event contains movement description.
 */
public final class UnitMoveStartedEvent extends AbstractStoppableEvent {

    private final Unit unit;

    private final Path path;

    public UnitMoveStartedEvent(final Model model, final Unit unit, final Path path) {
        super(model);

        this.unit = Preconditions.checkNotNull(unit);
        this.path = Preconditions.checkNotNull(path);
    }

    public Unit getUnit() {
        return unit;
    }

    public Location getStartLocation() {
        return path.getStart();
    }

    public Location getTargetLocation() {
        return path.getTarget();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("unit", unit).add("stopped", isStopped())
                .toString();
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

}
