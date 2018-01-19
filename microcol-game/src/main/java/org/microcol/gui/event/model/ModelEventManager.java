package org.microcol.gui.event.model;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Events incoming from model are translated to GIU events. This class manage
 * GUI event controllers for model events.
 */
public class ModelEventManager {
	
	private final NextTurnController nextTurnController;

	private final UnitMovedController unitMovedController;

	private final NewGameController newGameController;

	private final TurnStartedController turnStartedController;

	private final DebugRequestController debugRequestController;

	private final GameFinishedController gameFinishedController;

	private final UnitAttackedEventController unitAttackedEventController;
	
	private final GoldWasChangedController goldWasChangedController;
	
	private final ColonyWasCapturedController colonyWasCapturedController;
	
	private final UnitEmbarkedController unitEmbarkedController; 

	@Inject
	public ModelEventManager(final NextTurnController nextTurnController, final UnitMovedController unitMovedController,
			final NewGameController newGameController, final TurnStartedController turnStartedController,
			final DebugRequestController debugRequestController, final GameFinishedController gameFinishedController,
			final UnitAttackedEventController unitAttackedEventController, final GoldWasChangedController goldWasChangedController,
			final ColonyWasCapturedController colonyWasCapturedController,
			final UnitEmbarkedController unitEmbarkedController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.unitMovedController = Preconditions.checkNotNull(unitMovedController);
		this.newGameController = Preconditions.checkNotNull(newGameController);
		this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
		this.debugRequestController = Preconditions.checkNotNull(debugRequestController);
		this.gameFinishedController = Preconditions.checkNotNull(gameFinishedController);
		this.unitAttackedEventController = Preconditions.checkNotNull(unitAttackedEventController);
		this.goldWasChangedController = Preconditions.checkNotNull(goldWasChangedController);
		this.colonyWasCapturedController = Preconditions.checkNotNull(colonyWasCapturedController);
		this.unitEmbarkedController = Preconditions.checkNotNull(unitEmbarkedController);
	}

	public NextTurnController getNextTurnController() {
		return nextTurnController;
	}

	public UnitMovedController getUnitMovedController() {
		return unitMovedController;
	}

	public NewGameController getNewGameController() {
		return newGameController;
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
	
	public GoldWasChangedController getGoldWasChangedController(){
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
	
}
