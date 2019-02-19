package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

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
        return MoreObjects.toStringHelper(this).add("player", player).toString();
    }

}
