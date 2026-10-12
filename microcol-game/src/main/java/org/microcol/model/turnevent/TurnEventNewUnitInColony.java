package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventNewUnitInColony extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;

    TurnEventNewUnitInColony(final String playerName, final int colonyId,
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
        out.setColonyName(getColonyName());
        return out;
    }

    public static TurnEventNewUnitInColony tryLoad(final TurnEventPo po) {
        if (TurnEventNewUnitInColony.class.getSimpleName()
                .equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventNewUnitInColony(playerName, Preconditions
                    .checkNotNull(po.getColonyId(), "Missing colony id"),
                    colonyName);
        }
        return null;
    }

}
