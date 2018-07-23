package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.ColonyWasFoundController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.GameStoppedController;
import org.microcol.gui.event.model.UnitEmbarkedController;
import org.microcol.gui.event.model.UnitMovedStepStartedController;
import org.microcol.gui.event.model.UnitMovedToColonyFieldController;
import org.microcol.gui.event.model.UnitMovedToConstructionController;
import org.microcol.gui.event.model.UnitMovedToHighSeasController;
import org.microcol.gui.event.model.UnitMovedToLocationController;
import org.microcol.gui.mainmenu.SelectNextUnitController;
import org.microcol.gui.mainmenu.SelectNextUnitEvent;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStoppedEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.microcol.model.event.UnitMovedToColonyFieldEvent;
import org.microcol.model.event.UnitMovedToConstructionEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;
import org.microcol.model.event.UnitMovedToLocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected unit. It provide selected unit to move
 * if there is any.
 */
public final class SelectedUnitManager {

    private final Logger logger = LoggerFactory.getLogger(SelectedUnitManager.class);

    private final GameModelController gameModelController;

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitWasChangedController selectedUnitWasChangedController;

    private Unit selectedUnit;

    @Inject
    public SelectedUnitManager(final GameModelController gameModelController,
            final TileWasSelectedController tileWasSelectedController,
            final SelectNextUnitController selectNextUnitController,
            final SelectedTileManager selectedTileManager,
            final UnitMovedStepStartedController unitMovedStepStartedController,
            final UnitMovedToHighSeasController unitMovedToHighSeasController,
            final UnitMovedToColonyFieldController unitMovedToFieldController,
            final SelectedUnitWasChangedController selectedUnitWasChangedController,
            final UnitEmbarkedController unitEmbarkedController,
            final UnitMovedToConstructionController unitMovedToConstructionController,
            final UnitMovedToLocationController unitMovedToLocationController,
            final ColonyWasFoundController colonyWasFoundController,
            final GameStoppedController gameStoppedController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.selectedUnitWasChangedController = Preconditions
                .checkNotNull(selectedUnitWasChangedController);
        tileWasSelectedController.addListener(event -> evaluateLocation(event.getLocation()));
        selectNextUnitController.addListener(this::onSelectNextUnit);
        unitMovedStepStartedController.addRunLaterListener(this::onUnitMovedStepStarted);
        unitMovedToHighSeasController.addListener(this::onUnitMovedToHighSeas);
        unitMovedToFieldController.addListener(this::onUnitMovedToField);
        unitEmbarkedController.addListener(this::onUnitEmbarked);
        unitMovedToConstructionController.addListener(this::onUnitMovedToConstruction);
        colonyWasFoundController.addListener(this::onColonyWasFound);
        unitMovedToLocationController.addListener(this::onUnitMovedToLocation);
        gameStoppedController.addListener(this::onGameStopped);
    }

    private void onUnitMovedToLocation(final UnitMovedToLocationEvent event) {
        if (!getSelectedUnit().isPresent() && selectedTileManager.getSelectedTile().isPresent()
                && selectedTileManager.getSelectedTile().get()
                        .equals(event.getUnit().getLocation())) {
            evaluateLocation(event.getUnit().getLocation());
        }
    }

    private void onUnitMovedToConstruction(final UnitMovedToConstructionEvent event) {
        if (event.getUnit().equals(selectedUnit)) {
            unselectUnit();
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    private void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            if (event.getUnit().isAtPlaceLocation()) {
                selectedUnit(event.getUnit());
            }
        }
    }

    private void onUnitEmbarked(final UnitEmbarkedEvent event) {
        if (event.getUnit().equals(selectedUnit)) {
            unselectUnit();
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    @SuppressWarnings("unused")
    private void onGameStopped(final GameStoppedEvent event) {
        selectedUnit = null;
    }

    private void onColonyWasFound(final ColonyWasFoundEvent event) {
        if (selectedTileManager.getSelectedTile().isPresent()) {
            if (event.getColony().getLocation()
                    .equals(selectedTileManager.getSelectedTile().get())) {
                // founded colony is at focused location.
                if (selectedUnit != null && !selectedUnit.isAtPlaceLocation()) {
                    // verify that selected unit found colony and it's not at
                    // map
                    evaluateLocation(selectedTileManager.getSelectedTile().get());
                }
            }
        }
    }

    private void onUnitMovedToField(final UnitMovedToColonyFieldEvent event) {
        if (event.getUnit().equals(selectedUnit)) {
            unselectUnit();
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    public void setSelectedUnit(final Unit unit) {
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(unit.isAtPlaceLocation());

        final Unit previousUnit = selectedUnit;
        selectedUnit = unit;
        selectedUnitWasChangedController
                .fireEvent(new SelectedUnitWasChangedEvent(previousUnit, selectedUnit));
        logger.debug("Selected unit is now: {}", selectedUnit);
    }

    public void unselectUnit() {
        selectedUnit(null);
    }

    @SuppressWarnings("unused")
    private void onUnitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
        if (selectedTileManager.getSelectedTile().isPresent()) {
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    private void selectedUnit(final Unit unit) {
        if (unit != null) {
            Preconditions.checkState(unit.isAtPlaceLocation());
        }
        final Unit previousUnit = selectedUnit;
        selectedUnit = unit;
        selectedUnitWasChangedController
                .fireEvent(new SelectedUnitWasChangedEvent(previousUnit, selectedUnit));
        logger.debug("Selected unit is now: {}", selectedUnit);
    }

    private void evaluateLocation(final Location location) {
        if (selectedUnit == null) {
            selectedUnit(
                    gameModelController.getModel().getFirstSelectableUnitAt(location).orElse(null));
        } else {
            if (selectedUnit.isAtPlaceLocation()) {
                /*
                 * If selected tile is location where is selected unit than do
                 * nothing
                 */
                if (!selectedUnit.getLocation().equals(location)) {
                    selectedUnit(gameModelController.getModel().getFirstSelectableUnitAt(location)
                            .orElse(null));
                }
            } else {
                selectedUnit(gameModelController.getModel().getFirstSelectableUnitAt(location)
                        .orElse(null));
            }
        }
        logger.debug("Selected unit at {} is now: {}", location, selectedUnit);
    }

    private void onSelectNextUnit(final SelectNextUnitEvent selectNextUnitEvent) {
        Preconditions.checkNotNull(selectNextUnitEvent);
        if (selectedUnit == null) {
            selectedUnit(gameModelController.getModel().getFirstSelectableUnit().orElse(null));
        } else {
            selectedUnit(gameModelController.getModel().getNextUnitForCurrentPlayer(selectedUnit)
                    .orElse(null));
        }
        if (selectedUnit != null) {
            selectedTileManager.setSelectedTile(selectedUnit.getLocation(),
                    ScrollToFocusedTile.smoothScroll);
        }
    }

    public Optional<Unit> getSelectedUnit() {
        return Optional.ofNullable(selectedUnit);
    }

    public boolean isSelectedUnitMoveable() {
        return selectedUnit != null && selectedUnit.getActionPoints() > 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("selectedUnit", selectedUnit).toString();
    }

}
