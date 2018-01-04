package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.UnitMovedController;
import org.microcol.gui.mainmenu.SelectNextUnitController;
import org.microcol.gui.mainmenu.SelectNextUnitEvent;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected unit. It provide selected unit to move
 * if there is any.
 */
public class SelectedUnitManager {

	private final Logger logger = LoggerFactory.getLogger(SelectedUnitManager.class);

	private final GameModelController gameModelController;

	private final SelectedTileManager selectedTileManager;

	private Unit selectedUnit;

	/**
	 * Default constructor
	 */
	@Inject
	public SelectedUnitManager(final GameModelController gameModelController,
			final TileWasSelectedController tileWasSelectedController,
			final SelectNextUnitController selectNextUnitController,
			final SelectedTileManager selectedTileManager,
			final UnitMovedController unitMovedController) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
		tileWasSelectedController.addListener(event -> evaluateLocation(event.getLocation()));
		selectNextUnitController.addListener(this::onSelectNextUnit);
		unitMovedController.addRunLaterListener(event -> {
			if (event.getUnit().getOwner().isHuman()) {
				setSelectedUnit(event.getUnit());
			}
		});
	}

	private void setSelectedUnit(final Unit unit) {
		Preconditions.checkNotNull(unit);
		Preconditions.checkState(unit.isAtPlaceLocation());
		selectedUnit = unit;
		logger.debug("Selected unit is now: {}", selectedUnit);
	}

	private void evaluateLocation(final Location location) {
		if (selectedUnit == null) {
			selectedUnit = gameModelController.getModel().getFirstSelectableUnitAt(location).orElse(null);
		} else {
			if (!selectedUnit.isAtPlaceLocation()) {
				selectedUnit = gameModelController.getModel().getFirstSelectableUnitAt(location).orElse(null);
			}
			/*
			 * If selected tile is location where is selected unit than do
			 * nothing
			 */
			if (!selectedUnit.getLocation().equals(location)) {
				selectedUnit = gameModelController.getModel().getFirstSelectableUnitAt(location).orElse(null);
			}
		}
		logger.debug("Selected unit at {} is now: {}", location, selectedUnit);
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

	public boolean isSelectedUnitMoveable() {
		return selectedUnit != null && selectedUnit.getAvailableMoves() > 0;
	}

}