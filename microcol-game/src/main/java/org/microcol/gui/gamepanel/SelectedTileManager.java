package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected tile.
 */
public class SelectedTileManager {

	private final TileWasSelectedController tileWasSelectedController;
	
	private final GameModelController gameModelController;

	private Location selectedTile;

	/**
	 * Default constructor
	 */
	@Inject
	public SelectedTileManager(final TileWasSelectedController tileWasSelectedController,
			final GameModelController gameModelController) {
		this.tileWasSelectedController = Preconditions.checkNotNull(tileWasSelectedController);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		selectedTile = null;
	}

	public Optional<Location> getSelectedTile() {
		return Optional.ofNullable(selectedTile);
	}

	public void setSelectedTile(final Location newlySelectedTile) {
		Preconditions.checkNotNull(newlySelectedTile);
		if (selectedTile == null) {
			tileWasSelectedController.fireEvent(new TileWasSelectedEvent(gameModelController, newlySelectedTile));
			this.selectedTile = newlySelectedTile;
		} else {
			if (!selectedTile.equals(newlySelectedTile)) {
				tileWasSelectedController.fireEvent(new TileWasSelectedEvent(gameModelController, newlySelectedTile));
				this.selectedTile = newlySelectedTile;
			}
		}
	}

}
