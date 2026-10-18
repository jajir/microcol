package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;

/**
 * A deliberate saved boundary: the Crown's tax demand waits for the human
 * nation's answer before its colonies are updated.
 */
public final class TaxDemandRequiredEvent extends AbstractModelEvent {
    private final Player player;

    public TaxDemandRequiredEvent(final Model model, final Player player) {
        super(model);
        this.player = Preconditions.checkNotNull(player);
    }

    public Player getPlayer() {
        return player;
    }
}
