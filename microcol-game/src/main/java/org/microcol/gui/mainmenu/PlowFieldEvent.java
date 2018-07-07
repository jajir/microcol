package org.microcol.gui.mainmenu;

import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Front end event.
 */
public class PlowFieldEvent {

    private final Player player;
    private final Unit unit;

    PlowFieldEvent(final Player player, final Unit unit) {
        this.player = Preconditions.checkNotNull(player);
        this.unit = Preconditions.checkNotNull(unit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlowFieldEvent.class).add("player", player)
                .add("unit", unit).toString();
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the unit
     */
    public Unit getUnit() {
        return unit;
    }

}