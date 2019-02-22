package org.microcol.gui.gamepanel.buttonpanel;

import java.util.Optional;

import org.microcol.gui.buttonpanel.NextTurnEvent;
import org.microcol.gui.event.DeclareIndependenceEvent;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.Colony;
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

import javafx.application.Platform;

@Singleton
@Listener
public class ButtonsGamePanelController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ButtonsGamePanelController.class);

    private final GameModelController gameModelController;

    private final ButtonsGamePanel buttonGamePanel;

    @Inject
    ButtonsGamePanelController(final ButtonsGamePanel buttonGamePanel,
            final GameModelController gameModelController) {
        this.buttonGamePanel = Preconditions.checkNotNull(buttonGamePanel);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onNextTurn(final NextTurnEvent event) {
        LOGGER.debug("Next turn was triggered");
        buttonGamePanel.disableAllButtons();
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        LOGGER.debug("Turn started event {}", event);
        if (event.getPlayer().isHuman()) {
            buttonGamePanel.enableAllButtons();
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
            buttonGamePanel.disableAllButtons();
        }
    }

    @Subscribe
    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            buttonGamePanel.enableAllButtons();
        }
    }

    @Subscribe
    private void onSelectedUnitWasChanged(final SelectedUnitWasChangedEvent event) {
        LOGGER.debug("Selected unit was changed " + event.getSelectedUnit());
        if (event.getSelectedUnit().isPresent()) {
            final Unit unit = event.getSelectedUnit().get();
            evaluateAllButtonsForSelectedUnit(unit);
        } else {
            Platform.runLater(() -> {
                buttonGamePanel.setVisibleButtonMove(false);
                buttonGamePanel.setVisibleButtonBuildColony(false);
                buttonGamePanel.setVisibleButtonPlowField(false);
            });
        }
    }

    @Subscribe
    private void onIndependenceWasDeclared(
            @SuppressWarnings("unused") final DeclareIndependenceEvent event) {
        buttonGamePanel.setVisibleButtonDeclareIndependence(false);
    }

    private void evaluateAllButtonsForSelectedUnit(final Unit unit) {
        evaluateDeclareIndependenceButton();
        evaluateMoveButton(unit);
        evaluateBuildColony(unit);
        evaluatePlowField(unit);
    }

    private void evaluateMoveButton(final Unit unit) {
        buttonGamePanel.setVisibleButtonMove(unit.getActionPoints() > 0);
    }

    private void evaluateDeclareIndependenceButton() {
        buttonGamePanel.setVisibleButtonDeclareIndependence(
                !gameModelController.getCurrentPlayer().isDeclaredIndependence());
    }

    private void evaluatePlowField(final Unit unit) {
        if (unit.canPlowFiled()) {
            final Terrain terrain = gameModelController.getModel().getMap()
                    .getTerrainAt(unit.getLocation());
            buttonGamePanel.setVisibleButtonPlowField(!terrain.isHasField());
        } else {
            buttonGamePanel.setVisibleButtonPlowField(false);
        }
    }

    private void evaluateBuildColony(final Unit unit) {
        if (unit.getType().canBuildColony()) {
            final Optional<Colony> oColony = gameModelController.getModel()
                    .getColonyAt(unit.getLocation());
            if (oColony.isPresent()) {
                buttonGamePanel.setVisibleButtonBuildColony(false);
            } else {
                buttonGamePanel.setVisibleButtonBuildColony(unit.getActionPoints() > 0);
            }
        } else {
            buttonGamePanel.setVisibleButtonBuildColony(false);
        }
    }

}
