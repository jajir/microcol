package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.ColonyWasCapturedController;
import org.microcol.gui.event.model.UnitMoveFinishedController;
import org.microcol.model.Location;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected tile.
 */
public final class SelectedTileManager {

    private final TileWasSelectedController tileWasSelectedController;

    private Location selectedTile;

    @Inject
    public SelectedTileManager(final TileWasSelectedController tileWasSelectedController,
            final UnitMoveFinishedController unitMoveFinishedController,
            final ColonyWasCapturedController colonyWasCapturedController) {
        this.tileWasSelectedController = Preconditions.checkNotNull(tileWasSelectedController);
        unitMoveFinishedController.addListener(this::onUnitMoveFinished);
        colonyWasCapturedController.addListener(this::onColonyWasCapturedController);
        selectedTile = null;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("selectedTile", selectedTile).toString();
    }

    private void onColonyWasCapturedController(final ColonyWasCapturedEvent event) {
        setForcelySelectedTile(event.getCapturedColony().getLocation());
    }

    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            setSelectedTile(event.getTargetLocation());
        }
    }

    public Optional<Location> getSelectedTile() {
        return Optional.ofNullable(selectedTile);
    }

    public void setSelectedTile(final Location newlySelectedTile) {
        Preconditions.checkNotNull(newlySelectedTile);
        if (!newlySelectedTile.equals(selectedTile)) {
            setForcelySelectedTile(newlySelectedTile);
        }
    }

    private void setForcelySelectedTile(final Location newlySelectedTile) {
        Preconditions.checkNotNull(newlySelectedTile);
        tileWasSelectedController.fireEvent(new TileWasSelectedEvent(newlySelectedTile));
        this.selectedTile = newlySelectedTile;
    }

}
