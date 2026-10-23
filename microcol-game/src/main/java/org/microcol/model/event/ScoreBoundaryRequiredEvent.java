package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;

/**
 * Spring 1800 has frozen the score of the human colonial nation (R-0193); play
 * waits until the player continues without scoring. A loaded game raises it
 * again.
 */
public final class ScoreBoundaryRequiredEvent extends AbstractModelEvent {
    private final Player player;

    public ScoreBoundaryRequiredEvent(final Model model, final Player player) {
        super(model);
        this.player = Preconditions.checkNotNull(player);
    }

    public Player getPlayer() {
        return player;
    }
}
