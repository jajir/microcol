package org.akropia.gui.screen.game.gamepanel;

import java.util.Optional;

import org.akropia.gui.util.Listener;
import org.akropia.model.Location;
import org.akropia.model.event.GameStoppedEvent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected tile.
 */
@Listener
public final class SelectedTileManager {

    private final EventBus eventBus;

    private Location selectedTile;

    @Inject
    public SelectedTileManager(final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        selectedTile = null;
    }

    @Subscribe
    private void onGameStopped(
            @SuppressWarnings("unused") final GameStoppedEvent event) {
        selectedTile = null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("selectedTile", selectedTile).toString();
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
        eventBus.post(new TileWasSelectedEvent(newlySelectedTile,
                scrollToFocusedTile));
        this.selectedTile = newlySelectedTile;
    }

}
