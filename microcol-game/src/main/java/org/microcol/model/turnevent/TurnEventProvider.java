package org.microcol.model.turnevent;

import org.microcol.model.Colony;
import org.microcol.model.Goods;
import org.microcol.model.Player;

/**
 * Class simplify creating of turn events.
 */
public final class TurnEventProvider {

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
        return new TurnEventShipComeToEuropePort(player.getName());
    }

    /**
     * Get new turn event when ship come from Europe to colonies.
     *
     * @param player
     *            required owner of ship
     * @return turn event object
     */
    public static TurnEvent getShipComeToHighSeas(final Player player) {
        return new TurnEventShipComeToHighSeas(player.getName());
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
        return new TurnEventFaminePlagueColony(player.getName(), colony.getName());
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
        return new TurnEventFamineWillPlagueColony(player.getName(), colony.getName());
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
        return new TurnEventColonyWasDestroyed(player.getName(), colony.getName());
    }

    /**
     * Get new turn event when new colony was lost. Some other player conquer
     * it.
     *
     * @param player
     *            required owner of lost colony
     * @param colony
     *            required destroyed colony name
     * @return turn event object
     */
    public static TurnEvent getColonyWasLost(final Player player, final Colony colony) {
        return new TurnEventColonyWasLost(player.getName(), colony.getName());
    }

    /**
     * Get new turn event when new unit appears at Europe.
     *
     * @param player
     *            required owner of new unit
     * @return turn event object
     */
    public static TurnEvent getNewUnitInEurope(final Player player) {
        return new TurnEventNewUnitInEurope(player.getName());
    }

    /**
     * Get new turn event when new unit appears in colony.
     *
     * @param player
     *            required owner of new unit
     * @return turn event object
     */
    public static TurnEvent getNewUnitInColony(final Player player, final Colony colony) {
        return new TurnEventNewUnitInColony(player.getName(), colony.getName());
    }

    /**
     * Get new turn event when some goods is throws away because of limited free
     * space warehouse.
     *
     * @param player
     *            required owner of new unit
     * @param goods
     *            define lost goods type and amount
     * @return turn event object
     */
    public static TurnEvent getGoodsWasThrowsAway(final Player player, final Goods goods,
            final Colony colony) {
        return new TurnEventGoodsWasThrownAway(player.getName(), colony.getName(), goods);
    }

}
