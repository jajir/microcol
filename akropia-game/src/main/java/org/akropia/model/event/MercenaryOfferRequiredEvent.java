package org.akropia.model.event;

import org.akropia.model.Model;
import org.akropia.model.Player;

import com.google.common.base.Preconditions;

/**
 * The human owner completed an update with a mercenary offer to answer
 * (R-0169). The offer itself is read from the model; the event only announces
 * that the decision is due.
 */
public final class MercenaryOfferRequiredEvent extends AbstractModelEvent {
    private final Player player;

    public MercenaryOfferRequiredEvent(final Model model, final Player player) {
        super(model);
        this.player = Preconditions.checkNotNull(player);
    }

    public Player getPlayer() {
        return player;
    }
}
