package org.microcol.model.turnevent;

import org.microcol.model.Founder;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/** A persisted one-time notice that a father joined Congress. */
public final class TurnEventFounderArrived extends AbstractTurnEvent {
    private final Founder founder;

    TurnEventFounderArrived(final String playerName, final Founder founder) {
        super(playerName);
        this.founder = Preconditions.checkNotNull(founder);
    }

    public Founder getFounder() {
        return founder;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setFounder(founder);
        return po;
    }

    public static TurnEventFounderArrived tryLoad(final TurnEventPo po) {
        if (!TurnEventFounderArrived.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(po.getFounder() != null,
                "Founder arrival is missing its founder");
        return new TurnEventFounderArrived(po.getPlayerName(), po.getFounder());
    }
}
