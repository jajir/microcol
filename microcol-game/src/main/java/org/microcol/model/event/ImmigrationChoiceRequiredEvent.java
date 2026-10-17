package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;

/** A deliberate saved choice boundary before colony turn preparation. */
public final class ImmigrationChoiceRequiredEvent extends AbstractModelEvent {
    private final Player player;

    public ImmigrationChoiceRequiredEvent(final Model model,
            final Player player) {
        super(model);
        this.player = Preconditions.checkNotNull(player);
    }

    public Player getPlayer() {
        return player;
    }
}
