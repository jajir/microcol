package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.event.GameStoppedEvent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected tile.
 */
@Listener
public final class SelectedTileManager {

    private final TileWasSelectedController tileWasSelectedController;

    private Location selectedTile;

    @Inject
    public SelectedTileManager(final TileWasSelectedController tileWasSelectedController) {
        this.tileWasSelectedController = Preconditions.checkNotNull(tileWasSelectedController);
        selectedTile = null;
    }

    @Subscribe
    private void onGameStopped(@SuppressWarnings("unused") final GameStoppedEvent event) {
        selectedTile = null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("selectedTile", selectedTile).toString();
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
