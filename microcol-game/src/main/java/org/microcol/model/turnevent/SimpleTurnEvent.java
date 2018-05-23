package org.microcol.model.turnevent;

import org.microcol.model.Player;

/**
 * Simple turn event class.
 */
public class SimpleTurnEvent extends AbstractTurnEvent {

    SimpleTurnEvent(final String messageKey, final Object[] args, final Player player) {
        super(messageKey, args, player);
    }

}
