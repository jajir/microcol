package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

public class TurnEventShipComeToHighSeas extends AbstractTurnEvent {

    TurnEventShipComeToHighSeas(final String playerName) {
        super(playerName);
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setPlayerName(getPlayerName());
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        return out;
    }

    public static TurnEventShipComeToHighSeas tryLoad(final TurnEventPo po) {
        if (TurnEventShipComeToHighSeas.class.getSimpleName().equals(po.getType())) {
            final String playerName = po.getPlayerName();
            return new TurnEventShipComeToHighSeas(playerName);
        }
        return null;
    }

}
