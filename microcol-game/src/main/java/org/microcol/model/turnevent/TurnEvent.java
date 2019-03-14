package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

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
