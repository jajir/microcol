package org.microcol.gui.screen.game.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.SelectNextUnitEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStoppedEvent;
import org.microcol.model.event.NewUnitWasBornEvent;
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
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Control and preserve state of selected unit. It provide selected unit to move
 * if there is any.
 */
@Listener
public final class SelectedUnitManager {

    private final Logger logger = LoggerFactory.getLogger(SelectedUnitManager.class);

    private final GameModelController gameModelController;

    private final SelectedTileManager selectedTileManager;

    private final EventBus eventBus;

    private Unit selectedUnit;

    @Inject
    public SelectedUnitManager(final GameModelController gameModelController,
            final SelectedTileManager selectedTileManager, final EventBus eventBus) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Subscribe
    private void onTileWasSelected(TileWasSelectedEvent event) {
        evaluateLocation(event.getLocation());
    }

    @Subscribe
    private void onUnitMovedToLocation(final UnitMovedToLocationEvent event) {
        if (!getSelectedUnit().isPresent() && selectedTileManager.getSelectedTile().isPresent()
                && selectedTileManager.getSelectedTile().get()
                        .equals(event.getUnit().getLocation())) {
            evaluateLocation(event.getUnit().getLocation());
        }
    }

    @Subscribe
    private void onUnitMovedToConstruction(final UnitMovedToConstructionEvent event) {
        if (event.getUnit().equals(selectedUnit)) {
            unselectUnit();
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    @Subscribe
    private void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            if (event.getUnit().isAtPlaceLocation()) {
                selectedUnit(event.getUnit());
            }
        }
    }

    @Subscribe
    private void onUnitMovedToField(final UnitMovedToColonyFieldEvent event) {
        if (event.getUnit().equals(selectedUnit)) {
            unselectUnit();
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    @Subscribe
    private void onUnitMovedToHighSeas(
            @SuppressWarnings("unused") final UnitMovedToHighSeasEvent event) {
        if (selectedTileManager.getSelectedTile().isPresent()) {
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    @Subscribe
    private void onUnitEmbarked(final UnitEmbarkedEvent event) {
        if (event.getUnit().equals(selectedUnit)) {
            unselectUnit();
            evaluateLocation(selectedTileManager.getSelectedTile().get());
        }
    }

    @Subscribe
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

    @Subscribe
    private void onGameStopped(@SuppressWarnings("unused") final GameStoppedEvent event) {
        selectedUnit = null;
    }

    @Subscribe
    private void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
        if (selectedTileManager.getSelectedTile().isPresent()) {
            if (event.getLocation().equals(selectedTileManager.getSelectedTile().get())) {
                if (selectedUnit == null) {
                    forcelySetSelectedUnit(event.getUnit());
                }
            }
        }
    }

    public void setSelectedUnit(final Unit unit) {
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(unit.isAtPlaceLocation());
        if (unit.equals(selectedUnit)) {
            /**
             * If unit is already selected than skip method.
             */
            return;
        }
        forcelySetSelectedUnit(unit);
    }

    private void selectedUnit(final Unit unit) {
        if (unit != null) {
            Preconditions.checkState(unit.isAtPlaceLocation());
        }
        forcelySetSelectedUnit(unit);
    }

    private void forcelySetSelectedUnit(final Unit unit) {
        final Unit previousUnit = selectedUnit;
        selectedUnit = unit;
        eventBus.post(new SelectedUnitWasChangedEvent(previousUnit, selectedUnit));
        logger.debug("Selected unit is now: {}", selectedUnit);
    }

    private void unselectUnit() {
        forcelySetSelectedUnit(null);
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

    @Subscribe
    private void onSelectNextUnit(final SelectNextUnitEvent selectNextUnitEvent) {
        Preconditions.checkNotNull(selectNextUnitEvent);
        if (selectedUnit == null) {
            selectedUnit(gameModelController.getModel().getFirstSelectableUnit().orElse(null));
        } else {
            if (gameModelController.getModel().getNextUnitForCurrentPlayer(selectedUnit)
                    .isPresent()) {
                selectedUnit(gameModelController.getModel()
                        .getNextUnitForCurrentPlayer(selectedUnit).orElse(null));
            }
        }
        if (selectedUnit != null) {
            selectedTileManager.setSelectedTile(selectedUnit.getLocation(),
                    ScrollToFocusedTile.smoothScroll);
        }
    }

    public Optional<Unit> getSelectedUnit() {
        return Optional.ofNullable(selectedUnit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("selectedUnit", selectedUnit).toString();
    }

}
