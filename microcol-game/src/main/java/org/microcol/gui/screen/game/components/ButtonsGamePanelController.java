package org.microcol.gui.screen.game.components;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.buttonpanel.NextTurnEvent;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.i18n.I18n;
import com.google.common.eventbus.EventBus;
import org.microcol.gui.dialog.DisbandDialog;
import org.microcol.gui.dialog.GoToColonyDialog;
import org.microcol.gui.dialog.JettisonFlow;
import org.microcol.gui.event.DeclareIndependenceResolvedEvent;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.screen.game.gamepanel.GamePanelController;
import javafx.application.Platform;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.Model;
import org.microcol.gui.event.BuildRoadEventListener;
import org.microcol.gui.event.ClearForestEventListener;
import org.microcol.gui.event.PlowFieldEventListener;
import org.microcol.gui.event.RefreshRightPanelEvent;
import org.microcol.gui.event.StartGoToEvent;
import org.microcol.model.BuildRoadProblem;
import org.microcol.model.ClearForestProblem;
import org.microcol.model.Colony;
import org.microcol.model.DisbandOffer;
import org.microcol.model.DisbandProblem;
import org.microcol.model.JettisonProblem;
import org.microcol.model.PlowProblem;
import org.microcol.model.Unit;
import org.microcol.model.unit.Ship;
import org.microcol.model.event.ActionWasStartedEvent;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.ImmigrationChoiceRequiredEvent;
import org.microcol.model.event.TaxDemandRequiredEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.unit.UnitActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@Listener
public class ButtonsGamePanelController {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ButtonsGamePanelController.class);

    private final GameModelController gameModelController;

    private final SelectedUnitManager selectedUnitManager;

    private final ButtonsGamePanel view;
    private final GamePanelController gamePanelController;
    private final EventBus eventBus;
    private final I18n i18n;
    private final Provider<GoToColonyDialog> goToColonyDialog;
    private final Provider<DisbandDialog> disbandDialog;
    private final JettisonFlow jettisonFlow;

    @Inject
    ButtonsGamePanelController(final ButtonsGamePanel buttonGamePanel,
            final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager,
            final GamePanelController gamePanelController,
            final EventBus eventBus, final I18n i18n,
            final Provider<GoToColonyDialog> goToColonyDialog,
            final Provider<DisbandDialog> disbandDialog,
            final JettisonFlow jettisonFlow) {
        this.goToColonyDialog = Preconditions.checkNotNull(goToColonyDialog);
        this.disbandDialog = Preconditions.checkNotNull(disbandDialog);
        this.jettisonFlow = Preconditions.checkNotNull(jettisonFlow);
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.gamePanelController = Preconditions
                .checkNotNull(gamePanelController);
        this.view = Preconditions.checkNotNull(buttonGamePanel);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        view.setSentryAction(this::changeSentryOrder);
        view.setFortifyAction(this::changeFortifyOrder);
        view.setGoToAction(this::changeGoToOrder);
        view.setGoToColonyAction(this::chooseGoToColony);
        view.setCancelPlowFieldAction(this::cancelPlowField);
        view.setCancelClearForestAction(this::cancelClearForest);
        view.setCancelBuildRoadAction(this::cancelBuildRoad);
        view.setDisbandAction(this::disbandSelectedUnit);
        view.setJettisonAction(this::jettisonSelectedShip);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onNextTurn(final NextTurnEvent event) {
        LOGGER.debug("Next turn was triggered");
        view.disableTurnCommands();
    }

    @Subscribe
    private void onImmigrationChoiceRequired(
            final ImmigrationChoiceRequiredEvent event) {
        if (event.getPlayer().isHuman()) {
            Platform.runLater(() -> {
                if (gameModelController.getModel() == event.getModel()
                        && event.getModel().isAwaitingImmigrationChoice()) {
                    view.allowPendingImmigrationNavigation();
                }
            });
        }
    }

    @Subscribe
    private void onTaxDemandRequired(final TaxDemandRequiredEvent event) {
        if (event.getPlayer().isHuman()) {
            Platform.runLater(() -> {
                if (gameModelController.getModel() == event.getModel()
                        && event.getModel().isAwaitingTaxDecision()) {
                    view.allowPendingImmigrationNavigation();
                }
            });
        }
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        LOGGER.debug("Turn started event {}", event);
        if (event.getPlayer().isHuman()) {
            view.enableAllButtons();
            // Production changes support at the start of the national round.
            refreshDeclareIndependenceButton();
            if (selectedUnitManager.getSelectedUnit().isPresent()) {
                final Unit unit = selectedUnitManager.getSelectedUnit().get();
                evaluateAllButtonsForSelectedUnit(unit);
            } else {
                view.setVisibleButtonMove(false);
                view.setVisibleButtonBuildColony(false);
                view.setVisibleButtonPlowField(false);
                view.setVisibleButtonCancelPlowField(false);
                view.setVisibleButtonClearForest(false);
                view.setVisibleButtonCancelClearForest(false);
                view.setVisibleButtonBuildRoad(false);
                view.setVisibleButtonCancelBuildRoad(false);
            }
        }
        updateUnitOrders();
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
        updateUnitOrders();
    }

    @Subscribe
    private void onSelectedUnitWasChanged(
            final SelectedUnitWasChangedEvent event) {
        LOGGER.debug("Selected unit was changed " + event.getSelectedUnit());
        if (event.getSelectedUnit().isPresent()) {
            final Unit unit = event.getSelectedUnit().get();
            evaluateAllButtonsForSelectedUnit(unit);
        } else {
            view.setVisibleButtonMove(false);
            view.setVisibleButtonBuildColony(false);
            view.setVisibleButtonPlowField(false);
            view.setVisibleButtonCancelPlowField(false);
            view.setVisibleButtonClearForest(false);
            view.setVisibleButtonCancelClearForest(false);
            view.setVisibleButtonBuildRoad(false);
            view.setVisibleButtonCancelBuildRoad(false);
        }
        evaluateDeclareIndependenceButton();
        updateUnitOrders();
    }

    @Subscribe
    private void onActionWasStarted(
            @SuppressWarnings("unused") final ActionWasStartedEvent event) {
        selectedUnitManager.getSelectedUnit()
                .ifPresent(this::evaluateAllButtonsForSelectedUnit);
        updateUnitOrders();
    }

    /**
     * The button follows the model rule, not the click: a canceled or rejected
     * confirmation keeps it, a declaration hides it.
     */
    @Subscribe
    private void onIndependenceWasDeclared(
            @SuppressWarnings("unused") final IndependenceWasDeclaredEvent event) {
        refreshDeclareIndependenceButton();
    }

    @Subscribe
    private void onDeclareIndependenceResolved(
            @SuppressWarnings("unused") final DeclareIndependenceResolvedEvent event) {
        refreshDeclareIndependenceButton();
    }

    @Subscribe
    private void onGameStarted(
            @SuppressWarnings("unused") final GameStartedEvent event) {
        refreshDeclareIndependenceButton();
    }

    @Subscribe
    private void onColonyWasFound(
            @SuppressWarnings("unused") final ColonyWasFoundEvent event) {
        refreshDeclareIndependenceButton();
    }

    @Subscribe
    private void onColonyWasCaptured(
            @SuppressWarnings("unused") final ColonyWasCapturedEvent event) {
        refreshDeclareIndependenceButton();
    }

    @Subscribe
    private void onUnitAttacked(
            @SuppressWarnings("unused") final UnitAttackedEvent event) {
        refreshDeclareIndependenceButton();
    }

    private void refreshDeclareIndependenceButton() {
        Platform.runLater(this::evaluateDeclareIndependenceButton);
    }

    private void evaluateAllButtonsForSelectedUnit(final Unit unit) {
        updateUnitOrders();
        evaluateDeclareIndependenceButton();
        evaluateMoveButton(unit);
        evaluateBuildColony(unit);
        evaluatePlowField(unit);
        evaluateClearForest(unit);
        evaluateBuildRoad(unit);
    }

    private void evaluateMoveButton(final Unit unit) {
        view.setVisibleButtonMove(
                unit.getMovementThirds() > 0 && unit.getAction().isNoAction()
                        && getModel().getGoToDestination(unit).isEmpty());
    }

    private void evaluateDeclareIndependenceButton() {
        // A replaced or stopped game has no button state to maintain.
        if (!gameModelController.isGameModelReady()) {
            return;
        }
        view.setVisibleButtonDeclareIndependence(gameModelController
                .getHumanPlayer().isPossibleToDecalareIndependence());
    }

    private void evaluatePlowField(final Unit unit) {
        // An equipped idle pioneer keeps the control; a rejected start
        // explains the reason through the status bar.
        final Optional<PlowProblem> problem = getModel().getPlowProblem(unit);
        view.setVisibleButtonPlowField(
                problem.isEmpty() || PlowFieldEventListener
                        .isExplainedOnControl(problem.get(), unit));
        view.setVisibleButtonCancelPlowField(getModel().canCancelPlow(unit));
    }

    private void evaluateClearForest(final Unit unit) {
        // An equipped idle pioneer in a forest keeps the control; a rejected
        // start explains the reason through the status bar.
        final Optional<ClearForestProblem> problem = getModel()
                .getClearForestProblem(unit);
        view.setVisibleButtonClearForest(problem.isEmpty()
                || ClearForestEventListener.isExplainedOnControl(problem.get(),
                        unit, isInForest(unit)));
        view.setVisibleButtonCancelClearForest(
                getModel().canCancelClearForest(unit));
    }

    private void evaluateBuildRoad(final Unit unit) {
        // An equipped idle pioneer keeps the control; a rejected start
        // explains the reason through the status bar.
        final Optional<BuildRoadProblem> problem = getModel()
                .getBuildRoadProblem(unit);
        view.setVisibleButtonBuildRoad(
                problem.isEmpty() || BuildRoadEventListener
                        .isExplainedOnControl(problem.get(), unit));
        view.setVisibleButtonCancelBuildRoad(
                getModel().canCancelBuildRoad(unit));
    }

    private void cancelBuildRoad() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null) {
            return;
        }
        try {
            getModel().cancelBuildRoad(unit);
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Buttons.buildRoadProblemUnavailable),
                    StatusBarMessageEvent.Source.GAME));
        }
        updateUnitOrders();
        // The unit panel stops showing the road progress at once.
        eventBus.post(new RefreshRightPanelEvent());
    }

    private boolean isInForest(final Unit unit) {
        return unit.isAtPlaceLocation() && getModel().getMap()
                .getTerrainAt(unit.getLocation()).isHasTrees();
    }

    private void cancelClearForest() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null) {
            return;
        }
        try {
            getModel().cancelClearForest(unit);
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Buttons.clearForestProblemUnavailable),
                    StatusBarMessageEvent.Source.GAME));
        }
        updateUnitOrders();
    }

    private void cancelPlowField() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null) {
            return;
        }
        try {
            getModel().cancelPlow(unit);
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Buttons.plowProblemUnavailable),
                    StatusBarMessageEvent.Source.GAME));
        }
        updateUnitOrders();
    }

    /**
     * Shows the model's disbanding terms for the selected unit and removes it
     * only after the player confirmed exactly those terms. A refusal explains
     * the model's reason; canceling changes nothing.
     */
    private void disbandSelectedUnit() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null || !gameModelController.isHumanTurnReady()) {
            return;
        }
        final Model model = getModel();
        final Optional<DisbandProblem> problem = model.getDisbandProblem(unit);
        if (problem.isPresent()) {
            postDisbandStatus(disbandReason(problem.get()));
            return;
        }
        final Optional<DisbandOffer> offer = model.getDisbandOffer(unit);
        if (offer.isEmpty()) {
            postDisbandStatus(Buttons.disbandProblemUnavailable);
            return;
        }
        final Optional<Boolean> answer = disbandDialog.get().confirm(unit,
                offer.get());
        if (answer.isEmpty()) {
            updateUnitOrders();
            return;
        }
        try {
            model.disband(unit, offer.get(), answer.get());
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            // State changed between the offer and the confirmation.
            postDisbandStatus(Buttons.disbandChanged);
            updateUnitOrders();
            return;
        }
        selectedUnitManager.forgetRemovedUnit(unit);
        eventBus.post(new RefreshRightPanelEvent());
        updateUnitOrders();
    }

    /**
     * Offers goods of the selected ship at sea through the shared flow. The
     * model decides every limit; the right panel is refreshed for the remaining
     * cargo after a discard.
     */
    private void jettisonSelectedShip() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (!(unit instanceof Ship)
                || !gameModelController.isHumanTurnReady()) {
            return;
        }
        if (jettisonFlow.jettison(getModel(), (Ship) unit)) {
            eventBus.post(new RefreshRightPanelEvent());
        }
        updateUnitOrders();
    }

    /**
     * A ship at sea keeps the control also when the phase refuses it, because
     * the click explains the reason. Ships elsewhere, or foreign ones, hide it.
     */
    private boolean isJettisonShown(final Unit unit) {
        if (!(unit instanceof Ship) || !unit.getOwner().isHuman()) {
            return false;
        }
        final Optional<JettisonProblem> problem = getModel()
                .getJettisonProblem(unit);
        return problem.isEmpty() || problem.get() == JettisonProblem.WRONG_PHASE
                || problem.get() == JettisonProblem.NO_GOODS;
    }

    private void postDisbandStatus(final Buttons reason) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(reason),
                StatusBarMessageEvent.Source.GAME));
    }

    private static Buttons disbandReason(final DisbandProblem problem) {
        switch (problem) {
        case WRONG_PHASE:
            return Buttons.disbandProblemWrongPhase;
        case NOT_OWNER:
            return Buttons.disbandProblemNotOwner;
        case VOYAGE:
            return Buttons.disbandProblemVoyage;
        case IN_REPAIR:
            return Buttons.disbandProblemInRepair;
        case ABOARD_CARRIER:
            return Buttons.disbandProblemAboardCarrier;
        case LOADED_CARRIER:
            return Buttons.disbandProblemLoadedCarrier;
        case RECEIVING_COLONY_INVALID:
            return Buttons.disbandProblemReceivingColony;
        case LAST_COLONY_RESIDENT:
            return Buttons.disbandProblemLastResident;
        case MINIMUM_RESIDENTS:
            return Buttons.disbandProblemMinimumResidents;
        default:
            return Buttons.disbandProblemUnavailable;
        }
    }

    private void evaluateBuildColony(final Unit unit) {
        view.setVisibleButtonBuildColony(
                unit.getAction().getType() != UnitActionType.sentry
                        && gameModelController.getModel()
                                .canUnitBuildColony(unit));
    }

    private void updateUnitOrders() {
        Platform.runLater(() -> {
            final boolean ready = gameModelController.isHumanTurnReady()
                    && !gamePanelController.isUnitMoving();
            final Unit selected = selectedUnitManager.getSelectedUnit()
                    .orElse(null);
            final boolean sentry = selected != null
                    && selected.getAction().getType() == UnitActionType.sentry;
            view.setSentryOrder(sentry,
                    ready && selected != null
                            && (sentry ? getModel().canActivateSentry(selected)
                                    : getModel().canSetSentry(selected)));
            final boolean fortify = selected != null
                    && selected.getAction().getType() == UnitActionType.fortify;
            view.setFortifyOrder(fortify,
                    fortify && getModel().isFortified(selected),
                    ready && selected != null
                            && (fortify ? getModel().canCancelFortify(selected)
                                    : getModel().canFortify(selected)));
            final Optional<String> goTo = selected == null ? Optional.empty()
                    : getModel().getGoToDestinationName(selected);
            view.setGoToOrder(goTo.isPresent(),
                    selected != null && getModel().isGoToPaused(selected),
                    ready && selected != null
                            && (goTo.isPresent()
                                    ? getModel().canCancelGoTo(selected)
                                    : getModel().canStartGoTo(selected)),
                    ready && selected != null
                            && !getModel().getGoToColonies(selected).isEmpty(),
                    goTo.orElse(null));
            view.setDisbandOrder(
                    selected != null && selected.getOwner().isHuman(),
                    ready && selected != null);
            view.setJettisonOrder(selected != null && isJettisonShown(selected),
                    ready && selected != null);
            view.setUnitOrdersEnabled(
                    ready && getModel().getFirstSelectableUnit().isPresent(),
                    ready && selectedUnitManager.getSelectedUnit()
                            .map(getModel()::canSkipUnitTurn).orElse(false));
        });
    }

    private void changeSentryOrder() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null) {
            return;
        }
        try {
            if (unit.getAction().getType() == UnitActionType.sentry) {
                getModel().activateSentry(unit);
            } else {
                getModel().setSentry(unit);
            }
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Buttons.unitOrderUnavailable),
                    StatusBarMessageEvent.Source.GAME));
        }
        updateUnitOrders();
    }

    /**
     * Cancels the shown go-to order, or asks the map for a new destination.
     */
    private void changeGoToOrder() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null) {
            return;
        }
        if (getModel().getGoToDestination(unit).isPresent()) {
            try {
                getModel().cancelGoTo(unit);
            } catch (IllegalStateException rejected) {
                eventBus.post(new StatusBarMessageEvent(
                        i18n.get(Buttons.unitOrderUnavailable),
                        StatusBarMessageEvent.Source.GAME));
            }
            updateUnitOrders();
        } else {
            eventBus.post(new StartGoToEvent());
        }
    }

    /**
     * Shows the colonies the model currently offers and issues the order for
     * the chosen one. Closing or canceling the dialog changes nothing.
     */
    private void chooseGoToColony() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null || !gameModelController.isHumanTurnReady()) {
            return;
        }
        final List<Colony> offered = getModel().getGoToColonies(unit);
        if (offered.isEmpty()) {
            eventBus.post(
                    new StatusBarMessageEvent(i18n.get(Buttons.goToChanged),
                            StatusBarMessageEvent.Source.GAME));
            updateUnitOrders();
            return;
        }
        goToColonyDialog.get().choose(offered)
                .ifPresent(colony -> gameModelController.performGoTo(unit,
                        colony.getLocation(), colony));
    }

    private void changeFortifyOrder() {
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        if (unit == null) {
            return;
        }
        try {
            if (unit.getAction().getType() == UnitActionType.fortify) {
                getModel().cancelFortify(unit);
            } else {
                getModel().fortify(unit);
            }
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Buttons.unitOrderUnavailable),
                    StatusBarMessageEvent.Source.GAME));
        }
        updateUnitOrders();
    }

    private Model getModel() {
        return gameModelController.getModel();
    }

}
