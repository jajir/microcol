package org.microcol.gui.event.model;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Events incoming from model are translated to GIU events. This class manage
 * GUI event controllers for model events.
 */
public class ModelEventManager {

    private final RoundStartedController roundStartedController;

    private final UnitMovedStepStartedController unitMovedStepStartedController;
    
    private final UnitMovedStepFinishedController unitMovedStepFinishedController;
    
    private final UnitMoveFinishedController unitMoveFinishedController;

    private final GameStartedController gameStartedController;

    private final TurnStartedController turnStartedController;

    private final DebugRequestController debugRequestController;

    private final GameFinishedController gameFinishedController;

    private final UnitAttackedEventController unitAttackedEventController;

    private final GoldWasChangedController goldWasChangedController;

    private final ColonyWasCapturedController colonyWasCapturedController;

    private final UnitEmbarkedController unitEmbarkedController;

    private final UnitMovedToHighSeasController unitMovedToHighSeasController;

    private final IndependenceWasDeclaredColntroller independenceWasDeclaredColntroller;

    @Inject
    public ModelEventManager(final RoundStartedController roundStartedController,
            final UnitMovedStepStartedController unitMovedStepStartedController,
            final UnitMovedStepFinishedController unitMovedStepFinishedController,
            final GameStartedController gameStartedController,
            final TurnStartedController turnStartedController,
            final DebugRequestController debugRequestController,
            final GameFinishedController gameFinishedController,
            final UnitAttackedEventController unitAttackedEventController,
            final GoldWasChangedController goldWasChangedController,
            final ColonyWasCapturedController colonyWasCapturedController,
            final UnitEmbarkedController unitEmbarkedController,
            final UnitMoveFinishedController unitMoveFinishedController,
            final UnitMovedToHighSeasController unitMovedToHighSeasController,
            final IndependenceWasDeclaredColntroller independenceWasDeclaredColntroller) {
        this.roundStartedController = Preconditions.checkNotNull(roundStartedController);
        this.unitMovedStepStartedController = Preconditions.checkNotNull(unitMovedStepStartedController);
        this.unitMovedStepFinishedController = Preconditions.checkNotNull(unitMovedStepFinishedController);
        this.gameStartedController = Preconditions.checkNotNull(gameStartedController);
        this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
        this.debugRequestController = Preconditions.checkNotNull(debugRequestController);
        this.gameFinishedController = Preconditions.checkNotNull(gameFinishedController);
        this.unitAttackedEventController = Preconditions.checkNotNull(unitAttackedEventController);
        this.goldWasChangedController = Preconditions.checkNotNull(goldWasChangedController);
        this.colonyWasCapturedController = Preconditions.checkNotNull(colonyWasCapturedController);
        this.unitEmbarkedController = Preconditions.checkNotNull(unitEmbarkedController);
        this.unitMoveFinishedController = Preconditions.checkNotNull(unitMoveFinishedController);
        this.unitMovedToHighSeasController = Preconditions
                .checkNotNull(unitMovedToHighSeasController);
        this.independenceWasDeclaredColntroller = Preconditions
                .checkNotNull(independenceWasDeclaredColntroller);
    }

    public RoundStartedController getRoundStartedController() {
        return roundStartedController;
    }

    public UnitMovedStepStartedController getUnitMovedStepStartedController() {
        return unitMovedStepStartedController;
    }

    public GameStartedController getGameStartedController() {
        return gameStartedController;
    }

    public TurnStartedController getTurnStartedController() {
        return turnStartedController;
    }

    public DebugRequestController getDebugRequestController() {
        return debugRequestController;
    }

    public GameFinishedController getGameFinishedController() {
        return gameFinishedController;
    }

    public UnitAttackedEventController getUnitAttackedEventController() {
        return unitAttackedEventController;
    }

    public GoldWasChangedController getGoldWasChangedController() {
        return goldWasChangedController;
    }

    /**
     * @return the colonyWasCapturedController
     */
    public ColonyWasCapturedController getColonyWasCapturedController() {
        return colonyWasCapturedController;
    }

    /**
     * @return the unitEmbarkController
     */
    public UnitEmbarkedController getUnitEmbarkController() {
        return unitEmbarkedController;
    }

    /**
     * @return the unitMoveFinishedController
     */
    public UnitMoveFinishedController getUnitMoveFinishedController() {
        return unitMoveFinishedController;
    }

    /**
     * @return the unitMovedToHighSeasController
     */
    public UnitMovedToHighSeasController getUnitMovedToHighSeasController() {
        return unitMovedToHighSeasController;
    }

    /**
     * @return the independenceWasDeclaredColntroller
     */
    public IndependenceWasDeclaredColntroller getIndependenceWasDeclaredColntroller() {
        return independenceWasDeclaredColntroller;
    }

    public UnitMovedStepFinishedController getUnitMovedStepFinishedController() {
        return unitMovedStepFinishedController;
    }

}
