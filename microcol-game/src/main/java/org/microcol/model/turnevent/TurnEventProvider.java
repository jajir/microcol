package org.microcol.model.turnevent;

import org.microcol.model.Colony;
import org.microcol.model.Player;

/**
 * Class simplify creating of turn events.
 */
public class TurnEventProvider {

    private final static String KEY_SHIP_COME_TO_EUROPE = "turnEvent.shipComeToEuropePort";

    private final static String KEY_SHIP_COME_TO_HIGHSEAS = "turnEvent.shipComeToHighSeas";

    private final static String KEY_FAMINE_PLAGUE_COLONY = "turnEvent.faminePlagueColony";

    private final static String KEY_FAMINE_WILL_PLAGUE_COLONY = "turnEvent.famineWillPlagueColony";

    public static TurnEvent getShipComeEuropePort(final Player player) {
        return new SimpleTurnEvent(KEY_SHIP_COME_TO_EUROPE, new Object[0], player);
    }

    public static TurnEvent getShipComeHighSeas(final Player player) {
        return new SimpleTurnEvent(KEY_SHIP_COME_TO_HIGHSEAS, new Object[0], player);
    }

    public static TurnEvent getFaminePlagueColony(final Player player, final Colony colony) {
        return new SimpleTurnEvent(KEY_FAMINE_PLAGUE_COLONY, new Object[] { colony.getName() },
                player);
    }

    public static TurnEvent getFamineWillPlagueColony(final Player player, final Colony colony) {
        return new SimpleTurnEvent(KEY_FAMINE_WILL_PLAGUE_COLONY, new Object[] { colony.getName() },
                player);
    }

}
