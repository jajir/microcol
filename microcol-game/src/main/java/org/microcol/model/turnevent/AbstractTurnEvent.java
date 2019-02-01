package org.microcol.model.turnevent;

import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract class hold all object needed for correct turn event type.
 */
public abstract class AbstractTurnEvent implements TurnEvent {

    private final TurnEvents messageKey;

    private final Object[] args;

    private final Player player;

    private String localizedMessage;

    private boolean solved;

    /**
     * Constructor setting basic fields.
     *
     * @param messageKey
     *            required message localized store key
     * @param args
     *            required array of arguments to localized message
     * @param player
     *            required player for whom was event generated
     */
    AbstractTurnEvent(final TurnEvents messageKey, final Object[] args, final Player player) {
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
    public void setSolved(final boolean solved) {
        this.solved = solved;
    }

    @Override
    public TurnEvents getMessageKey() {
        return messageKey;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    @Override
    public void setLocalizedMessage(final String localizedMessage) {
        this.localizedMessage = Preconditions.checkNotNull(localizedMessage);
    }

}
