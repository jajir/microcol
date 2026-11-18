package org.akropia.model.turnevent;

import org.akropia.model.UnitType;
import org.akropia.model.Location;
import org.akropia.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Persisted notice that a go-to order was paused or canceled, with its reason.
 */
public final class TurnEventGoTo extends AbstractTurnEvent {
    public enum Kind {
        /** No route to the destination exists now; the order is retained. */
        PAUSED,
        /** The named colony no longer exists; the order is canceled. */
        CANCELLED_REMOVED,
        /** The named colony is no longer friendly; the order is canceled. */
        CANCELLED_NOT_FRIENDLY,
        /** The ship was damaged and cannot sail; the order is canceled. */
        CANCELLED_DAMAGED, ARRIVED, ARRIVED_EUROPE, ARRIVED_MAP, RETURN_BLOCKED,
        CANCELLED_BLOCKED, ROUTE_NO_ROUTE, ROUTE_STOP_UNREACHABLE,
        ROUTE_STOP_LOST, ROUTE_EUROPE_CLOSED, ROUTE_CARRIER_DAMAGED
    }

    private final Kind kind;
    private final int unitId;
    private final UnitType unitType;
    private final String destinationName;
    private Integer colonyId;
    private boolean europe;
    private Location location;

    public TurnEventGoTo(final String playerName, final Kind kind,
            final int unitId, final UnitType unitType,
            final String destinationName) {
        super(playerName);
        this.kind = Preconditions.checkNotNull(kind);
        Preconditions.checkArgument(unitId >= 0 && unitType != null
                && destinationName != null && !destinationName.isBlank(),
                "Invalid go-to event");
        this.unitId = unitId;
        this.unitType = unitType;
        this.destinationName = destinationName;
    }

    public TurnEventGoTo destination(final Integer colonyId,
            final boolean europe) {
        Preconditions.checkArgument(
                colonyId == null || colonyId >= 0 && !europe,
                "Invalid notice destination");
        this.colonyId = colonyId;
        this.europe = europe;
        return this;
    }

    public TurnEventGoTo atLocation(final Location at) {
        location = at;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getColonyId() {
        return colonyId;
    }

    public boolean isEurope() {
        return europe;
    }

    public Kind getKind() {
        return kind;
    }

    public int getUnitId() {
        return unitId;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setGoToKind(kind.name());
        po.setGoToUnitId(unitId);
        po.setGoToUnitType(unitType);
        po.setGoToDestinationName(destinationName);
        po.setColonyId(colonyId);
        po.setGoToEurope(europe);
        if (location != null) {
            po.setGoToX(location.getX());
            po.setGoToY(location.getY());
        }
        return po;
    }

    public static TurnEventGoTo tryLoad(final TurnEventPo po) {
        if (!TurnEventGoTo.class.getSimpleName().equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getGoToKind() != null && po.getGoToUnitId() != null
                        && po.getGoToUnitType() != null
                        && po.getGoToDestinationName() != null,
                "Incomplete go-to event");
        Preconditions.checkArgument(
                (po.getGoToX() == null) == (po.getGoToY() == null),
                "Incomplete go-to location");
        return new TurnEventGoTo(po.getPlayerName(),
                Kind.valueOf(po.getGoToKind()), po.getGoToUnitId(),
                po.getGoToUnitType(), po.getGoToDestinationName())
                .destination(po.getColonyId(), po.isGoToEurope())
                .atLocation(po.getGoToX() == null ? null
                        : Location.of(po.getGoToX(), po.getGoToY()));
    }
}
