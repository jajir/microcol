package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventNewUnitInColony extends AbstractTurnEvent {

    private final String colonyName;

    TurnEventNewUnitInColony(final String playerName, final String colonyName) {
        super(playerName);
        this.colonyName = Preconditions.checkNotNull(colonyName);
    }

    public String getColonyName() {
        return colonyName;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setColonyName(getColonyName());
        return out;
    }

    public static TurnEventNewUnitInColony tryLoad(final TurnEventPo po) {
        if (TurnEventNewUnitInColony.class.getSimpleName().equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventNewUnitInColony(playerName, colonyName);
        }
        return null;
    }

}
