package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.SelectNextUnitController;
import org.microcol.gui.mainmenu.SelectNextUnitEvent;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected tile, mouse over tile and mode.
 */
public class SelectedUnitManager {

	private final GameModelController gameModelController;
	
	private final SelectedTileManager selectedTileManager;

	private Unit selectedUnit;

	/**
	 * Default constructor
	 */
	@Inject
	public SelectedUnitManager(final GameModelController gameModelController,
			final TileWasSelectedController tileWasSelectedController,
			final SelectNextUnitController selectNextUnitController, final SelectedTileManager selectedTileManager) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
		tileWasSelectedController.addListener(this::onTileWasChanged);
		selectNextUnitController.addListener(this::onSelectNextUnit);
	}

	private void onTileWasChanged(final TileWasSelectedEvent tileWasSelectedEvent) {
		if(selectedUnit == null){
			selectedUnit = gameModelController.getModel().getFirstSelectableUnitAt(tileWasSelectedEvent.getLocation()).orElse(null);
		}else{
			//If selected tile is location where is selected unit than do nothing 
			if (!selectedUnit.getLocation().equals(tileWasSelectedEvent.getLocation())){
				selectedUnit = gameModelController.getModel().getFirstSelectableUnitAt(tileWasSelectedEvent.getLocation()).orElse(null);				
			}
		}
	}

	private void onSelectNextUnit(final SelectNextUnitEvent selectNextUnitEvent) {
		Preconditions.checkNotNull(selectNextUnitEvent);
		if (selectedUnit == null) {
			selectedUnit = gameModelController.getModel().getFirstSelectableUnitAt().orElse(null);
		} else {
			selectedUnit = gameModelController.getModel().getNextUnitForCurrentUser(selectedUnit).orElse(null);
		}
		if (selectedUnit != null) {
			selectedTileManager.setSelectedTile(selectedUnit.getLocation());
		}
	}

	public Optional<Unit> getSelectedUnit() {
		return Optional.ofNullable(selectedUnit);
	}

}
