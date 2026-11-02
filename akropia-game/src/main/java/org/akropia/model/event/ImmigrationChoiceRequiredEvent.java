package org.akropia.model.event;

import org.akropia.model.Model;
import org.akropia.model.Player;

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
