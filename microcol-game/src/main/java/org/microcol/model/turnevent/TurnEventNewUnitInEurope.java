package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

public class TurnEventNewUnitInEurope extends AbstractTurnEvent {

    TurnEventNewUnitInEurope(final String playerName) {
        super(playerName);
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        return out;
    }

    public static TurnEventNewUnitInEurope tryLoad(final TurnEventPo po) {
        if (TurnEventNewUnitInEurope.class.getSimpleName().equals(po.getType())) {
            final String playerName = po.getPlayerName();
            return new TurnEventNewUnitInEurope(playerName);
        }
        return null;
    }

}
