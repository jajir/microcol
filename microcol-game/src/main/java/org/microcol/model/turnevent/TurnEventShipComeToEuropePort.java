package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

public class TurnEventShipComeToEuropePort extends AbstractTurnEvent {

    TurnEventShipComeToEuropePort(final String playerName) {
        super(playerName);
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        return out;
    }

    public static TurnEventShipComeToEuropePort tryLoad(final TurnEventPo po) {
        if (TurnEventShipComeToEuropePort.class.getSimpleName().equals(po.getType())) {
            final String playerName = po.getPlayerName();
            return new TurnEventShipComeToEuropePort(playerName);
        }
        return null;
    }

}
