package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved one-time notice that the earned foreign intervention arrived at a
 * coastal colony (R-0190).
 */
public final class TurnEventInterventionArrival extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;

    TurnEventInterventionArrival(final String playerName, final int colonyId,
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

    public static TurnEventInterventionArrival tryLoad(final TurnEventPo po) {
        if (TurnEventInterventionArrival.class.getSimpleName()
                .equals(po.getType())) {
            return new TurnEventInterventionArrival(po.getPlayerName(),
                    Preconditions.checkNotNull(po.getColonyId(),
                            "Missing colony id"),
                    Preconditions.checkNotNull(po.getColonyName(),
                            "Missing colony name"));
        }
        return null;
    }
}
