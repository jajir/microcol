package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventColonyWasDestroyed extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;

    /**
     * Default constructor. Just set properties.
     *
     * @param messageKey required message key
     * @param args       optional arguments for message
     * @param player     required player
     */
    TurnEventColonyWasDestroyed(final String playerName, final int colonyId,
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

    public static TurnEventColonyWasDestroyed tryLoad(final TurnEventPo po) {
        if (TurnEventColonyWasDestroyed.class.getSimpleName()
                .equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventColonyWasDestroyed(playerName, Preconditions
                    .checkNotNull(po.getColonyId(), "Missing colony id"),
                    colonyName);
        }
        return null;
    }

}
