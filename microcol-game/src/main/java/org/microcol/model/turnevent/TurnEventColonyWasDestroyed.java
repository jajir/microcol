package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventColonyWasDestroyed extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;
    private final int lostShips;

    /**
     * Record the destroyed colony and its name at the time of destruction.
     *
     * @param playerName name of the player receiving the report
     * @param colonyId   nonnegative identifier of the destroyed colony
     * @param colonyName name of the colony at the time of destruction
     */
    TurnEventColonyWasDestroyed(final String playerName, final int colonyId,
            final String colonyName) {
        this(playerName, colonyId, colonyName, 0);
    }

    TurnEventColonyWasDestroyed(final String playerName, final int colonyId,
            final String colonyName, final int lostShips) {
        super(playerName);
        Preconditions.checkArgument(colonyId >= 0, "Invalid colony id");
        this.colonyId = colonyId;
        this.colonyName = Preconditions.checkNotNull(colonyName);
        Preconditions.checkArgument(lostShips >= 0, "Invalid ship loss count");
        this.lostShips = lostShips;
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    public int getLostShips() {
        return lostShips;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setColonyId(colonyId);
        out.setColonyName(getColonyName());
        out.setLostShips(lostShips);
        return out;
    }

    public static TurnEventColonyWasDestroyed tryLoad(final TurnEventPo po) {
        if (TurnEventColonyWasDestroyed.class.getSimpleName()
                .equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventColonyWasDestroyed(playerName,
                    Preconditions.checkNotNull(po.getColonyId(),
                            "Missing colony id"),
                    colonyName,
                    po.getLostShips() == null ? 0 : po.getLostShips());
        }
        return null;
    }

}
