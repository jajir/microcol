package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.util.Listener;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionType;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Front end event.
 */
@Listener
public final class PlowFieldEventListener {

    final GameModelController gameModelController;
    final SelectedUnitManager selectedUnitManager;

    @Inject
    PlowFieldEventListener(final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
    }

    @Subscribe
    private void onPlowField(@SuppressWarnings("unused") final PlowFieldEvent event) {
        final Unit unit = selectedUnitManager.getSelectedUnit()
                .orElseThrow(() -> new IllegalStateException(
                        "Plow field event can't be invoked when no unit is selected."));
        Preconditions.checkArgument(unit.canPlowFiled(), "Unit can't plow field.");
        unit.setAction(UnitActionType.plowField);
    }

}
