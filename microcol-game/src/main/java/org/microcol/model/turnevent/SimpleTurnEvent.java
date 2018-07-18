package org.microcol.model.turnevent;

import org.microcol.model.Player;

/**
 * Simple turn event class. Generic turn event encapsulate important data.
 */
public final class SimpleTurnEvent extends AbstractTurnEvent {

    /**
     * Default constructor. Just set properties.
     *
     * @param messageKey
     *            required message key
     * @param args
     *            optional arguments for message
     * @param player
     *            required player
     */
    SimpleTurnEvent(final String messageKey, final Object[] args, final Player player) {
        super(messageKey, args, player);
    }

}
