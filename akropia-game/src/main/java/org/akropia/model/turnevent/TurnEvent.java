package org.akropia.model.turnevent;

import org.akropia.model.store.TurnEventPo;

/**
 * Define TurnEvent contract.
 */
public interface TurnEvent {

    /**
     * @return the player name
     */
    String getPlayerName();

    TurnEventPo save();

}
