package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.ColonyWasCapturedController;
import org.microcol.gui.event.model.GameStoppedController;
import org.microcol.model.Location;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.GameStoppedEvent;

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
            final ColonyWasCapturedController colonyWasCapturedController,
            final GameStoppedController gameStoppedController) {
        this.tileWasSelectedController = Preconditions.checkNotNull(tileWasSelectedController);
        colonyWasCapturedController.addListener(this::onColonyWasCapturedController);
        gameStoppedController.addListener(this::onGameStopped);
        selectedTile = null;
    }

    @SuppressWarnings("unused")
    private void onGameStopped(final GameStoppedEvent event) {
        selectedTile = null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("selectedTile", selectedTile).toString();
    }

    private void onColonyWasCapturedController(final ColonyWasCapturedEvent event) {
        setForcelySelectedTile(event.getCapturedColony().getLocation(), ScrollToFocusedTile.skip);
    }

    public Optional<Location> getSelectedTile() {
        return Optional.ofNullable(selectedTile);
    }

    public void setSelectedTile(final Location newlySelectedTile,
            final ScrollToFocusedTile scrollToFocusedTile) {
        Preconditions.checkNotNull(newlySelectedTile);
        if (!newlySelectedTile.equals(selectedTile)) {
            setForcelySelectedTile(newlySelectedTile, scrollToFocusedTile);
        }
    }

    private void setForcelySelectedTile(final Location newlySelectedTile,
            final ScrollToFocusedTile scrollToFocusedTile) {
        Preconditions.checkNotNull(newlySelectedTile);
        tileWasSelectedController
                .fireEvent(new TileWasSelectedEvent(newlySelectedTile, scrollToFocusedTile));
        this.selectedTile = newlySelectedTile;
    }

}
