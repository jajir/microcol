package org.microcol.gui.event;

import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.util.Listener;
import org.microcol.model.Unit;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.unit.UnitActionType;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Front end event.
 */
@Listener
public final class PlowFieldEventListener {

    private final SelectedUnitManager selectedUnitManager;

    private final EventBus eventBus;
    private final GameModelController gameModelController;

    @Inject
    PlowFieldEventListener(final SelectedUnitManager selectedUnitManager,
            final EventBus eventBus,
            final GameModelController gameModelController) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Subscribe
    private void onPlowField(
            @SuppressWarnings("unused") final PlowFieldEvent event) {
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        final Unit unit = selectedUnitManager.getSelectedUnit()
                .orElseThrow(() -> new IllegalStateException(
                        "Plow field event can't be invoked when no unit is selected."));
        Preconditions.checkArgument(unit.canPlowFiled(),
                "Unit can't plow field.");
        unit.setActionType(UnitActionType.plowField);
        eventBus.post(new RefreshRightPanelEvent());
    }

}
