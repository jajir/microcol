package org.microcol.gui.panelview;

import java.util.Optional;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Control and preserve state of selected tile, mouse over tile and mode.
 */
public class ViewState {

	private final MouseOverTileChangedController mouseOverTileChangedController;

	private boolean isMoveMode;

	private Optional<Location> selectedTile;

	private Optional<Location> mouseOverTile;

	/**
	 * Default constructor
	 * 
	 * @param mouseOverTileChangedController
	 *            required mouse over tile changed
	 */
	public ViewState(final MouseOverTileChangedController mouseOverTileChangedController) {
		this.mouseOverTileChangedController = Preconditions.checkNotNull(mouseOverTileChangedController);
		mouseOverTile = Optional.empty();
		selectedTile = Optional.empty();
		isMoveMode = false;
	}

	public Optional<Location> getMouseOverTile() {
		return mouseOverTile;
	}

	public void setMouseOverTile(final Optional<Location> newMouseOverTile) {
		Preconditions.checkNotNull(newMouseOverTile);
		if (mouseOverTile.isPresent()) {
			mouseOverTile.filter(mot -> !mot.equals(newMouseOverTile.get())).ifPresent(mot -> {
				this.mouseOverTile = newMouseOverTile;
				mouseOverTileChangedController.fireEvent(new MouseOverTileChangedEvent(mouseOverTile.get()));
			});
		} else {
			mouseOverTile = newMouseOverTile;
			mouseOverTileChangedController.fireEvent(new MouseOverTileChangedEvent(mouseOverTile.get()));
		}
	}

	public Optional<Location> getSelectedTile() {
		return selectedTile;
	}

	public void setSelectedTile(Optional<Location> selectedTile) {
		this.selectedTile = selectedTile;
	}

	public boolean isMoveMode() {
		return isMoveMode;
	}

	public void setMoveMode(boolean isMoveMode) {
		this.isMoveMode = isMoveMode;
	}

}
