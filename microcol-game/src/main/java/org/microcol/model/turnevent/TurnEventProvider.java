package org.microcol.model.turnevent;

import org.microcol.model.Colony;
import org.microcol.model.Player;

/**
 * Class simplify creating of turn events.
 */
public final class TurnEventProvider {

    private static final String KEY_SHIP_COME_TO_EUROPE = "turnEvent.shipComeToEuropePort";

    private static final String KEY_SHIP_COME_TO_HIGHSEAS = "turnEvent.shipComeToHighSeas";

    private static final String KEY_FAMINE_PLAGUE_COLONY = "turnEvent.faminePlagueColony";

    private static final String KEY_FAMINE_WILL_PLAGUE_COLONY = "turnEvent.famineWillPlagueColony";

    private static final String KEY_COLONY_WAS_DESTORYED = "turnEvent.colonyWasDestroyed";

    private static final String KEY_NEW_UNIT_RELIGION = "turnEvent.newUnitIsInEurope";

    /**
     * Hidden default constructor.
     */
    private TurnEventProvider() {
    }

    /**
     * Get new turn event when ship come to Europe port.
     *
     * @param player
     *            required owner of ship
     * @return turn event object
     */
    public static TurnEvent getShipComeEuropePort(final Player player) {
        return new SimpleTurnEvent(KEY_SHIP_COME_TO_EUROPE, new Object[0], player);
    }

    /**
     * Get new turn event when ship come from Europe to colonies.
     *
     * @param player
     *            required owner of ship
     * @return turn event object
     */
    public static TurnEvent getShipComeHighSeas(final Player player) {
        return new SimpleTurnEvent(KEY_SHIP_COME_TO_HIGHSEAS, new Object[0], player);
    }

    /**
     * Get new turn event when famine killed some colonist in colony.
     *
     * @param player
     *            required owner colony plagued by famine
     * @param colony
     *            required plagued colony
     * @return turn event object
     */
    public static TurnEvent getFaminePlagueColony(final Player player, final Colony colony) {
        return new SimpleTurnEvent(KEY_FAMINE_PLAGUE_COLONY, new Object[] {colony.getName() },
                player);
    }

    /**
     * Get new turn event when famine is about to kill some colonist in colony.
     *
     * @param player
     *            required owner colony plagued by famine
     * @param colony
     *            required plagued colony
     * @return turn event object
     */
    public static TurnEvent getFamineWillPlagueColony(final Player player, final Colony colony) {
        return new SimpleTurnEvent(KEY_FAMINE_WILL_PLAGUE_COLONY, new Object[] {colony.getName() },
                player);
    }

    /**
     * Get new turn event when new colony was destroyed.
     *
     * @param player
     *            required owner of destroyed colony
     * @param colony
     *            required destroyed colony name
     * @return turn event object
     */
    public static TurnEvent getColonyWasDestroyed(final Player player, final Colony colony) {
        return new SimpleTurnEvent(KEY_COLONY_WAS_DESTORYED, new Object[] {colony.getName() },
                player);
    }

    /**
     * Get new turn event when new unit appears at Europe.
     *
     * @param player
     *            required owner of new unit
     * @return turn event object
     */
    public static TurnEvent getNewUnitInEurope(final Player player) {
        return new SimpleTurnEvent(KEY_NEW_UNIT_RELIGION, new Object[0], player);
    }

}
