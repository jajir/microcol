package org.microcol.model.turnevent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract class hold all object needed for correct turn event type.
 */
public abstract class AbstractTurnEvent implements TurnEvent {

    private final String playerName;

    /**
     * Constructor setting basic fields.
     *
     * @param playerName
     *            required player name for whom was event generated
     */
    AbstractTurnEvent(final String playerName) {
        this.playerName = Preconditions.checkNotNull(playerName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("playerName", playerName).toString();
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

}
