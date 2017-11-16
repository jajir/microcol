package org.microcol.gui.panelview;

import java.util.Optional;

import org.microcol.model.Location;

import com.google.inject.Inject;

/**
 * Control and preserve state of selected tile, mouse over tile and mode.
 */
public class ViewState {
	
	//TODO in class in no functionality verify that class in really used

	private boolean isMoveMode;

	private Optional<Location> selectedTile;

	/**
	 * Default constructor
	 * 
	 * @param mouseOverTileChangedController
	 *            required mouse over tile changed
	 */
	@Inject
	public ViewState() {
		selectedTile = Optional.empty();
		isMoveMode = false;
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
