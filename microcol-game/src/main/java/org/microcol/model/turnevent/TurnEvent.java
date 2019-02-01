package org.microcol.model.turnevent;

import org.microcol.model.Player;

/**
 * Define TurnEvent contract.
 */
public interface TurnEvent {

    /**
     * Get parameters to final message. Like colony name or number of gold.
     *
     * @return field of message parameters
     */
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

    /**
     * Get message key in resource bundle.
     *
     * @return message key in resource bundle
     */
    TurnEvents getMessageKey();

    /**
     * Return localized message.
     * <p>
     * It should not be stored into persistent object. It's counted when turn
     * events are requested.
     * </p>
     *
     * @return localized message
     */
    String getLocalizedMessage();

    /**
     * This allows to set correct localized message. It should not be called
     * manually.
     *
     * @param localizedMessage
     *            required localized message that will be shown in turn event
     *            report.
     */
    void setLocalizedMessage(String localizedMessage);

}
