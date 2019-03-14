package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventFaminePlagueColony extends AbstractTurnEvent {

    private final String colonyName;

    TurnEventFaminePlagueColony(final String playerName, final String colonyName) {
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

    public static TurnEventFaminePlagueColony tryLoad(final TurnEventPo po) {
        if (TurnEventFaminePlagueColony.class.getSimpleName().equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventFaminePlagueColony(playerName, colonyName);
        }
        return null;
    }

}
