package org.microcol.model.turnevent;

import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * A persisted notice that one of the owner's pioneers finished plowing and the
 * square became a field (R-0393). It is created once, in the same update as the
 * field, and is routine: it never opens the report by itself when the turn
 * summary is off.
 */
public final class TurnEventFieldPlowed extends AbstractTurnEvent {

    private final int unitId;

    private final UnitType unitType;

    private final Location location;

    TurnEventFieldPlowed(final String playerName, final int unitId,
            final UnitType unitType, final Location location) {
        super(playerName);
        Preconditions.checkArgument(unitId >= 0 && unitType != null
                && UnitType.UNIT_TYPES.contains(unitType) && location != null,
                "Invalid field plowed event");
        this.unitId = unitId;
        this.unitType = unitType;
        this.location = location;
    }

    /** @return stable ID of the pioneer that finished the work */
    public int getUnitId() {
        return unitId;
    }

    /** @return type the pioneer had when the field was created */
    public UnitType getUnitType() {
        return unitType;
    }

    /** @return the square that became a field */
    public Location getLocation() {
        return location;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setFieldPlowedUnitId(unitId);
        po.setFieldPlowedUnitType(unitType);
        po.setFieldPlowedX(location.getX());
        po.setFieldPlowedY(location.getY());
        return po;
    }

    public static TurnEventFieldPlowed tryLoad(final TurnEventPo po) {
        if (!TurnEventFieldPlowed.class.getSimpleName().equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getPlayerName() != null && po.getFieldPlowedUnitId() != null
                        && po.getFieldPlowedUnitType() != null
                        && po.getFieldPlowedX() != null
                        && po.getFieldPlowedY() != null,
                "Incomplete field plowed event");
        return new TurnEventFieldPlowed(po.getPlayerName(),
                po.getFieldPlowedUnitId(), po.getFieldPlowedUnitType(),
                Location.of(po.getFieldPlowedX(), po.getFieldPlowedY()));
    }

}
