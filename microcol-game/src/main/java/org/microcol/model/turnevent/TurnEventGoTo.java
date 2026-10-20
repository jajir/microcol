package org.microcol.model.turnevent;

import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

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
        CANCELLED_DAMAGED
    }

    private final Kind kind;
    private final int unitId;
    private final UnitType unitType;
    private final String destinationName;

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
        return new TurnEventGoTo(po.getPlayerName(),
                Kind.valueOf(po.getGoToKind()), po.getGoToUnitId(),
                po.getGoToUnitType(), po.getGoToDestinationName());
    }
}
