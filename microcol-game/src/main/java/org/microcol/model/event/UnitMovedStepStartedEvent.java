package org.microcol.model.event;

import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is send when unit move start starts.
 */
public final class UnitMovedStepStartedEvent extends AbstractModelEvent {

    private final Unit unit;
    private final Location start;
    private final Location end;
    /**
     * Orientation have to be passed as parameter because move event doen't know
     * game context which determine orientation of unit. For example when ship come
     * from Europe than always face to the west.
     */
    private final Direction orientation;

    /**
     * Default constructor.
     *
     * @param model
     *            required model
     * @param unit
     *            required moved unit
     * @param start
     *            required start location of the move
     * @param end
     *            required end location of the move
     * @param orientation
     *            orientation in which in move performed
     */
    public UnitMovedStepStartedEvent(final Model model, final Unit unit, final Location start,
            final Location end, final Direction orientation) {
        super(model);

        this.unit = Preconditions.checkNotNull(unit);
        this.start = Preconditions.checkNotNull(start);
        this.end = Preconditions.checkNotNull(end);
        this.orientation = Preconditions.checkNotNull(orientation);
    }

    /**
     * Get moved unit.
     *
     * @return moved unit object
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Get location when move started.
     *
     * @return start location of the move
     */
    public Location getStart() {
        return start;
    }

    /**
     * Get location when move ended.
     *
     * @return end location of the move
     */
    public Location getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("unit", unit).add("start", start)
                .add("end", end).add("orientation", orientation).toString();
    }

    /**
     * Return <code>true</code> when player can see move. Player can see move when
     * start location or end location is in explored part of world.
     *
     * @param player
     *            required player
     * @return return <code>true</code> when user can see move otherwise return
     *         <code>false</code>
     */
    public boolean canPlayerSeeMove(final Player player) {
        Preconditions.checkNotNull(player);
        return player.isVisible(start) || player.isVisible(end);
    }

    /**
     * Get orientation in which in unit moving.
     *
     * @return direction in which in unit moving
     */
    public Direction getOrientation() {
        return orientation;
    }
}
