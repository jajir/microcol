package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.UnitMoveFinishedController;
import org.microcol.model.Location;
import org.microcol.model.event.UnitMoveFinishedEvent;

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
			final GameModelController gameModelController,
			final UnitMoveFinishedController unitMoveFinishedController) {
		this.tileWasSelectedController = Preconditions.checkNotNull(tileWasSelectedController);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		unitMoveFinishedController.addListener(this::onUnitMoveFinished);
		selectedTile = null;
	}
	
	private void onUnitMoveFinished(final UnitMoveFinishedEvent event){
		if (event.getUnit().getOwner().isHuman()) {
			setSelectedTile(event.getTargetLocation());
		}
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
