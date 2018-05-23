package org.microcol.model.turnevent;

import org.microcol.model.Player;

/**
 * Define TurnEvent contract.
 */
public interface TurnEvent {

    Object[] getArgs();

    /**
     * @return the player
     */
    Player getPlayer();

    /**
     * @return the solved
     */
    boolean isSolved();

    /**
     * @param solved
     *            the solved to set
     */
    void setSolved(boolean solved);

    String getMessageKey();

    /**
     * Return localized message.
     * <p>
     * It should not be stored into persistent object. It's counted when turn
     * events are requested.
     * </p>
     */
    String getLocalizedMessage();

    /**
     * This allows to set correct localized message. It should not be called
     * manually.
     */
    void setLocalizedMessage(String localizedMessage);

}