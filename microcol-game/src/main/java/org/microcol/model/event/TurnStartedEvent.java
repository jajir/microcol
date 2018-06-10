package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class TurnStartedEvent extends AbstractModelEvent {

    private final Player player;

    private final boolean isFreshStart;

    public TurnStartedEvent(final Model model, final Player player, final boolean isFreshStart) {
        super(model);

        this.player = Preconditions.checkNotNull(player);
        this.isFreshStart = isFreshStart;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("player", player)
                .add("isFreshStart", isFreshStart).toString();
    }

    /**
     * Provide information if it's fresh new turn of game or game was loaded and
     * this turn was previously already started.
     *
     * @return When return <code>true</code> than it's new turn. When it's
     *         <code>false</code> than turn was already running.
     */
    public boolean isFreshStart() {
        return isFreshStart;
    }
}
