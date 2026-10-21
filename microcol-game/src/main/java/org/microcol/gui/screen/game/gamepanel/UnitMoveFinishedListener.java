package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.event.RefreshRightPanelEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.model.Unit;
import org.microcol.model.event.UnitMoveFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Process unit moved event. It plan animations.
 */
@Listener
public final class UnitMoveFinishedListener {

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitManager selectedUnitManager;

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    @Inject
    public UnitMoveFinishedListener(
            final SelectedTileManager selectedTileManager,
            final SelectedUnitManager selectedUnitManager,
            final GameModelController gameModelController,
            final EventBus eventBus) {
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Subscribe
    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        final Unit unit = event.getUnit();
        if (unit.getOwner().isHuman()) {
            selectedTileManager.setSelectedTile(event.getTargetLocation(),
                    ScrollToFocusedTile.no);
            if (!gameModelController.getModel().isUnitInGame(unit)) {
                // A lost-city rumor can remove the explorer at the end of its
                // step; nothing may keep acting on it.
                selectedUnitManager.forgetRemovedUnit(unit);
                eventBus.post(new RefreshRightPanelEvent());
            } else if (unit.isAtPlaceLocation()) {
                selectedUnitManager.setSelectedUnit(unit);
            } else if (unit.isAtCargoSlot()) {
                selectedUnitManager.setSelectedUnit(
                        unit.getPlaceCargoSlot().getOwnerUnit());
            }
        }

    }

}
