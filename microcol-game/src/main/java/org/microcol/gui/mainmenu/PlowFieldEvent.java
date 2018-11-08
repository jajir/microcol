package org.microcol.gui.mainmenu;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Front end event.
 */
public final class PlowFieldEvent {

    private final Player player;
    private final Unit unit;

    public static PlowFieldEvent make(final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager) {
        final Player player = gameModelController.getCurrentPlayer();
        final Unit unit = selectedUnitManager.getSelectedUnit()
                .orElseThrow(() -> new IllegalStateException(
                        "Plow field event can't be invoked when no unit is selected."));
        Preconditions.checkArgument(unit.canPlowFiled(), "Unit can't plow field.");
        return new PlowFieldEvent(player, unit);
    }

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
