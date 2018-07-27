package org.microcol.gui.gamepanel;

import org.microcol.gui.util.Listener;
import org.microcol.model.event.UnitMoveFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Process unit moved event. It plan animations.
 */
@Listener
public final class UnitMoveFinishedListener {

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitManager selectedUnitManager;

    @Inject
    public UnitMoveFinishedListener(final SelectedTileManager selectedTileManager,
            final SelectedUnitManager selectedUnitManager) {
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
    }

    @Subscribe
    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            selectedTileManager.setSelectedTile(event.getTargetLocation(), ScrollToFocusedTile.no);
            if (event.getUnit().isAtPlaceLocation()) {
                selectedUnitManager.setSelectedUnit(event.getUnit());
            } else if (event.getUnit().isAtCargoSlot()) {
                selectedUnitManager
                        .setSelectedUnit(event.getUnit().getPlaceCargoSlot().getOwnerUnit());
            }
        }

    }

}
