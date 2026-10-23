package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved one-time notice that a paid mercenary group was placed at the colony
 * port (R-0169). It is recorded with the placement, so a group that waited for
 * an open route is announced when it actually arrives.
 */
public final class TurnEventMercenaryArrival extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;

    TurnEventMercenaryArrival(final String playerName, final int colonyId,
            final String colonyName) {
        super(playerName);
        Preconditions.checkArgument(colonyId >= 0, "Invalid colony id");
        this.colonyId = colonyId;
        this.colonyName = Preconditions.checkNotNull(colonyName);
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setColonyId(colonyId);
        out.setColonyName(colonyName);
        return out;
    }

    public static TurnEventMercenaryArrival tryLoad(final TurnEventPo po) {
        if (TurnEventMercenaryArrival.class.getSimpleName()
                .equals(po.getType())) {
            return new TurnEventMercenaryArrival(po.getPlayerName(),
                    Preconditions.checkNotNull(po.getColonyId(),
                            "Missing colony id"),
                    Preconditions.checkNotNull(po.getColonyName(),
                            "Missing colony name"));
        }
        return null;
    }
}
