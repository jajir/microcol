package org.akropia.gui.screen.game.gamepanel;

import java.util.Optional;

import org.akropia.gui.event.SelectNextUnitEvent;
import org.akropia.gui.event.SkipUnitTurnEvent;
import org.akropia.gui.event.RefreshRightPanelEvent;
import org.akropia.gui.buttonpanel.Buttons;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.i18n.I18n;
import org.akropia.model.Model;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.Listener;
import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.model.event.ColonyWasFoundEvent;
import org.akropia.model.event.GameStoppedEvent;
import org.akropia.model.event.NewUnitWasBornEvent;
import org.akropia.model.event.UnitEmbarkedEvent;
import org.akropia.model.event.UnitMovedStepStartedEvent;
import org.akropia.model.event.UnitMovedToColonyFieldEvent;
import org.akropia.model.event.UnitMovedToConstructionEvent;
import org.akropia.model.event.UnitMovedToHighSeasEvent;
import org.akropia.model.event.UnitMovedToLocationEvent;
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

    private final Logger logger = LoggerFactory
            .getLogger(SelectedUnitManager.class);

    private final GameModelController gameModelController;

    private final SelectedTileManager selectedTileManager;

    private final EventBus eventBus;

    private Unit selectedUnit;
    private final GamePanelController gamePanelController;

    private final TradeRoutePlanning tradeRoutePlanning;
    private final I18n i18n;

    @Inject
    public SelectedUnitManager(final GameModelController gameModelController,
            final SelectedTileManager selectedTileManager,
            final EventBus eventBus,
            final GamePanelController gamePanelController, final I18n i18n,
            final TradeRoutePlanning tradeRoutePlanning) {
        this.tradeRoutePlanning = Preconditions
                .checkNotNull(tradeRoutePlanning);
        this.gamePanelController = Preconditions
                .checkNotNull(gamePanelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Subscribe
    private void onTileWasSelected(TileWasSelectedEvent event) {
        evaluateLocation(event.getLocation());
    }

    @Subscribe
    private void onUnitMovedToLocation(final UnitMovedToLocationEvent event) {
        if (!getSelectedUnit().isPresent()
                && selectedTileManager.getSelectedTile().isPresent()
                && selectedTileManager.getSelectedTile().get()
                        .equals(event.getUnit().getLocation())) {
            evaluateLocation(event.getUnit().getLocation());
        }
    }

    @Subscribe
    private void onUnitMovedToConstruction(
            final UnitMovedToConstructionEvent event) {
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
                    evaluateLocation(
                            selectedTileManager.getSelectedTile().get());
                }
            }
        }
    }

    @Subscribe
    private void onGameStopped(
            @SuppressWarnings("unused") final GameStoppedEvent event) {
        selectedUnit = null;
    }

    @Subscribe
    private void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
        if (selectedTileManager.getSelectedTile().isPresent()) {
            if (event.getLocation()
                    .equals(selectedTileManager.getSelectedTile().get())) {
                if (selectedUnit == null) {
                    forcelySetSelectedUnit(event.getUnit());
                }
            }
        }
    }

    /** Select a live own map unit without issuing or clearing any order. */
    public void selectFromPanel(final Unit unit) {
        if (!gameModelController.isHumanTurnReady()
                || gamePanelController.isUnitMoving()
                || tradeRoutePlanning.isActive()
                || !gameModelController.getModel().getAllUnits().contains(unit)
                || !unit.getOwner().equals(gameModelController.getHumanPlayer())
                || !unit.isAtPlaceLocation()) {
            return;
        }
        setSelectedUnit(unit);
        selectedTileManager.setSelectedTile(unit.getLocation(),
                ScrollToFocusedTile.skip);
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
        eventBus.post(
                new SelectedUnitWasChangedEvent(previousUnit, selectedUnit));
        logger.debug("Selected unit is now: {}", selectedUnit);
    }

    private void unselectUnit() {
        forcelySetSelectedUnit(null);
    }

    /**
     * Drops the selection when its unit has just left the game, so no control
     * keeps acting on it.
     *
     * @param unit unit that was removed
     */
    public void forgetRemovedUnit(final Unit unit) {
        Preconditions.checkNotNull(unit);
        if (unit.equals(selectedUnit)) {
            unselectUnit();
        }
    }

    private void evaluateLocation(final Location location) {
        if (selectedUnit == null) {
            selectedUnit(
                    gameModelController.getModel()
                            .getFirstSelectableUnitAtOwnedBy(location,
                                    gameModelController.getHumanPlayer())
                            .orElse(null));
        } else {
            if (selectedUnit.isAtPlaceLocation()) {
                /*
                 * If selected tile is location where is selected unit than do
                 * nothing
                 */
                if (!selectedUnit.getLocation().equals(location)) {
                    selectedUnit(gameModelController.getModel()
                            .getFirstSelectableUnitAtOwnedBy(location,
                                    gameModelController.getHumanPlayer())
                            .orElse(null));
                }
            } else {
                selectedUnit(gameModelController.getModel()
                        .getFirstSelectableUnitAtOwnedBy(location,
                                gameModelController.getHumanPlayer())
                        .orElse(null));
            }
        }
        logger.debug("Selected unit at {} is now: {}", location, selectedUnit);
    }

    @Subscribe
    private void onSelectNextUnit(final SelectNextUnitEvent event) {
        Preconditions.checkNotNull(event);
        if (!gameModelController.isHumanTurnReady()
                || gamePanelController.isUnitMoving()) {
            return;
        }
        final Model model = gameModelController.getModel();
        getSelectedUnit();
        final Optional<Unit> next = selectedUnit == null
                || selectedUnit.getOwner() != model.getCurrentPlayer()
                        ? model.getFirstSelectableUnit()
                        : model.getNextUnitForCurrentPlayer(selectedUnit);
        selectAndFocus(next.orElse(null));
    }

    @Subscribe
    private void onSkipUnitTurn(final SkipUnitTurnEvent event) {
        Preconditions.checkNotNull(event);
        getSelectedUnit();
        if (!gameModelController.isHumanTurnReady()
                || gamePanelController.isUnitMoving() || selectedUnit == null) {
            return;
        }
        final Model model = gameModelController.getModel();
        final Unit unit = selectedUnit;
        try {
            // Preserve the position in the cycle before spending movement.
            final Optional<Unit> next = unit.getOwner() == model
                    .getCurrentPlayer()
                            ? model.getNextUnitForCurrentPlayer(unit)
                                    .filter(candidate -> candidate != unit)
                            : Optional.empty();
            model.skipUnitTurn(unit);
            selectAndFocus(next.filter(model::canSkipUnitTurn).orElseGet(
                    () -> model.getFirstSelectableUnit().orElse(null)));
            eventBus.post(new RefreshRightPanelEvent());
        } catch (IllegalStateException rejected) {
            eventBus.post(new SelectNextUnitEvent());
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Buttons.unitOrderUnavailable), Source.GAME));
        }
    }

    private void selectAndFocus(final Unit next) {
        selectedUnit(next);
        if (selectedUnit != null) {
            selectedTileManager.setSelectedTile(selectedUnit.getLocation(),
                    ScrollToFocusedTile.smoothScroll);
        }
    }

    public Optional<Unit> getSelectedUnit() {
        if (selectedUnit != null && gameModelController.isGameModelReady()
                && !gameModelController.getModel().getAllUnits()
                        .contains(selectedUnit)) {
            unselectUnit();
        }
        return Optional.ofNullable(selectedUnit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("selectedUnit", selectedUnit).toString();
    }

}
