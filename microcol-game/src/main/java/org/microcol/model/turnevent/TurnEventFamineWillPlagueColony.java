package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventFamineWillPlagueColony extends AbstractTurnEvent {

    private final String colonyName;

    TurnEventFamineWillPlagueColony(final String playerName, final String colonyName) {
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

    public static TurnEventFamineWillPlagueColony tryLoad(final TurnEventPo po) {
        if (TurnEventFamineWillPlagueColony.class.getSimpleName().equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventFamineWillPlagueColony(playerName, colonyName);
        }
        return null;
    }

}
