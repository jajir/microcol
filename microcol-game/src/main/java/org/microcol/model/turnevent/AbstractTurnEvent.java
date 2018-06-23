package org.microcol.model.turnevent;

import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract class hold all object needed for correct turn event type.
 */
public abstract class AbstractTurnEvent implements TurnEvent {

    private final String messageKey;

    private final Object[] args;

    private final Player player;

    private String localizedMessage;

    private boolean solved;

    AbstractTurnEvent(final String messageKey, final Object[] args, final Player player) {
        this.messageKey = Preconditions.checkNotNull(messageKey);
        this.args = Preconditions.checkNotNull(args);
        this.player = Preconditions.checkNotNull(player);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("messageKey", messageKey)
                .add("player", player.getName()).add("solved", solved).toString();
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    @Override
    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = Preconditions.checkNotNull(localizedMessage);
    }

}