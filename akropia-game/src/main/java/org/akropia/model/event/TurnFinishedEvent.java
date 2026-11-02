package org.akropia.model.event;

import org.akropia.model.Model;
import org.akropia.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Notify that some player finished turn.
 */
public final class TurnFinishedEvent extends AbstractModelEvent {

    private final Player player;

    public TurnFinishedEvent(final Model model, final Player player) {
        super(model);
        this.player = Preconditions.checkNotNull(player);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("player", player)
                .toString();
    }

}
