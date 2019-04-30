package org.microcol.gui.screen.game.components;

import java.util.Optional;

import org.microcol.gui.buttonpanel.NextTurnEvent;
import org.microcol.gui.event.DeclareIndependenceEvent;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.Colony;
import org.microcol.model.Model;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Listener
public class ButtonsGamePanelController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ButtonsGamePanelController.class);

    private final GameModelController gameModelController;

    private final SelectedUnitManager selectedUnitManager;

    private final ButtonsGamePanel view;

    @Inject
    ButtonsGamePanelController(final ButtonsGamePanel buttonGamePanel,
            final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager) {
        this.view = Preconditions.checkNotNull(buttonGamePanel);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onNextTurn(final NextTurnEvent event) {
        LOGGER.debug("Next turn was triggered");
        view.disableAllButtons();
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        LOGGER.debug("Turn started event {}", event);
        if (event.getPlayer().isHuman()) {
            view.enableAllButtons();
            if (selectedUnitManager.getSelectedUnit().isPresent()) {
                final Unit unit = selectedUnitManager.getSelectedUnit().get();
                evaluateAllButtonsForSelectedUnit(unit);
            } else {
                view.setVisibleButtonMove(false);
                view.setVisibleButtonBuildColony(false);
                view.setVisibleButtonPlowField(false);
            }
        }
    }

    @Subscribe
    private void onEndMove(final EndMoveEvent event) {
        LOGGER.debug("Unit move finished");
        evaluateAllButtonsForSelectedUnit(event.getMovedUnit());
    }

    @Subscribe
    private void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            view.disableAllButtons();
        }
    }

    @Subscribe
    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            view.enableAllButtons();
        }
    }

    @Subscribe
    private void onSelectedUnitWasChanged(final SelectedUnitWasChangedEvent event) {
        LOGGER.debug("Selected unit was changed " + event.getSelectedUnit());
        if (event.getSelectedUnit().isPresent()) {
            final Unit unit = event.getSelectedUnit().get();
            evaluateAllButtonsForSelectedUnit(unit);
        } else {
            view.setVisibleButtonMove(false);
            view.setVisibleButtonBuildColony(false);
            view.setVisibleButtonPlowField(false);
        }
        evaluateDeclareIndependenceButton();
    }

    @Subscribe
    private void onIndependenceWasDeclared(
            @SuppressWarnings("unused") final DeclareIndependenceEvent event) {
        view.setVisibleButtonDeclareIndependence(false);
    }

    private void evaluateAllButtonsForSelectedUnit(final Unit unit) {
        evaluateDeclareIndependenceButton();
        evaluateMoveButton(unit);
        evaluateBuildColony(unit);
        evaluatePlowField(unit);
    }

    private void evaluateMoveButton(final Unit unit) {
        view.setVisibleButtonMove(unit.getActionPoints() > 0);
    }

    private void evaluateDeclareIndependenceButton() {
        view.setVisibleButtonDeclareIndependence(
                gameModelController.getCurrentPlayer().isPossibleToDecalareIndependence());
    }

    private void evaluatePlowField(final Unit unit) {
        if (getModel().getColonyAt(unit.getLocation()).isPresent()) {
            view.setVisibleButtonPlowField(false);
            return;
        }
        if (!unit.canPlowFiled()) {
            view.setVisibleButtonPlowField(false);
            return;
        }
        final Terrain terrain = getModel().getMap().getTerrainAt(unit.getLocation());
        view.setVisibleButtonPlowField(!terrain.isHasField());
    }

    private void evaluateBuildColony(final Unit unit) {
        if (!unit.getType().canBuildColony()) {
            view.setVisibleButtonBuildColony(false);
            return;
        }
        final Optional<Colony> oColony = getModel().getColonyAt(unit.getLocation());
        if (oColony.isPresent()) {
            view.setVisibleButtonBuildColony(false);
        } else {
            view.setVisibleButtonBuildColony(unit.getActionPoints() > 0);
        }
    }

    private Model getModel() {
        return gameModelController.getModel();
    }

}
