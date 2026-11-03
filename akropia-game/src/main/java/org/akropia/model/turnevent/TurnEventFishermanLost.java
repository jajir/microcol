package org.akropia.model.turnevent;

import org.akropia.model.Colony;
import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.model.store.TurnEventPo;
import com.google.common.base.Preconditions;

/** One persisted explanation for a fisherman lost at sea. */
public final class TurnEventFishermanLost extends AbstractTurnEvent {
    public enum Reason {
        NO_CARGO_SPACE, NO_SAFE_LAND
    }

    private final int unitId;
    private final Location location;
    private final String colonyName;
    private final Reason reason;

    public TurnEventFishermanLost(final Unit unit, final Colony colony,
            final Reason reason) {
        this(unit.getOwner().getName(), unit.getId(), unit.getLocation(),
                colony.getName(), reason);
    }

    private TurnEventFishermanLost(final String owner, final Integer unitId,
            final Location location, final String colonyName,
            final Reason reason) {
        super(owner);
        Preconditions
                .checkArgument(
                        unitId != null && unitId >= 0 && location != null
                                && colonyName != null && !colonyName.isBlank()
                                && reason != null,
                        "Invalid fisherman loss notice");
        this.unitId = unitId;
        this.location = location;
        this.colonyName = colonyName;
        this.reason = reason;
    }

    public int getUnitId() {
        return unitId;
    }

    public Location getLocation() {
        return location;
    }

    public String getColonyName() {
        return colonyName;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setFishermanUnitId(unitId);
        out.setFishermanLossReason(reason.name());
        out.setClearedX(location.getX());
        out.setClearedY(location.getY());
        out.setColonyName(colonyName);
        return out;
    }

    public static TurnEventFishermanLost tryLoad(final TurnEventPo po) {
        if (!TurnEventFishermanLost.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getClearedX() != null && po.getClearedY() != null,
                "Missing fisherman loss location");
        return new TurnEventFishermanLost(po.getPlayerName(),
                po.getFishermanUnitId(),
                Location.of(po.getClearedX(), po.getClearedY()),
                po.getColonyName(),
                Reason.valueOf(po.getFishermanLossReason()));
    }
}
