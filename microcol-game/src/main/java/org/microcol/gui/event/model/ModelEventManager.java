package org.microcol.gui.event.model;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Events incoming from model are translated to GIU events. This class manage
 * GUI event controllers for model events.
 */
public class ModelEventManager {
	
	private final RoundStartedController roundStartedController;

	private final UnitMovedController unitMovedController;

	private final UnitMoveFinishedController unitMoveFinishedController;

	private final GameStartedController gameStartedController;

	private final TurnStartedController turnStartedController;

	private final DebugRequestController debugRequestController;

	private final GameFinishedController gameFinishedController;

	private final UnitAttackedEventController unitAttackedEventController;
	
	private final GoldWasChangedController goldWasChangedController;
	
	private final ColonyWasCapturedController colonyWasCapturedController;
	
	private final UnitEmbarkedController unitEmbarkedController; 

	@Inject
	public ModelEventManager(final RoundStartedController roundStartedController, final UnitMovedController unitMovedController,
			final GameStartedController gameStartedController, final TurnStartedController turnStartedController,
			final DebugRequestController debugRequestController, final GameFinishedController gameFinishedController,
			final UnitAttackedEventController unitAttackedEventController,
			final GoldWasChangedController goldWasChangedController,
			final ColonyWasCapturedController colonyWasCapturedController,
			final UnitEmbarkedController unitEmbarkedController,
			final UnitMoveFinishedController unitMoveFinishedController) {
		this.roundStartedController = Preconditions.checkNotNull(roundStartedController);
		this.unitMovedController = Preconditions.checkNotNull(unitMovedController);
		this.gameStartedController = Preconditions.checkNotNull(gameStartedController);
		this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
		this.debugRequestController = Preconditions.checkNotNull(debugRequestController);
		this.gameFinishedController = Preconditions.checkNotNull(gameFinishedController);
		this.unitAttackedEventController = Preconditions.checkNotNull(unitAttackedEventController);
		this.goldWasChangedController = Preconditions.checkNotNull(goldWasChangedController);
		this.colonyWasCapturedController = Preconditions.checkNotNull(colonyWasCapturedController);
		this.unitEmbarkedController = Preconditions.checkNotNull(unitEmbarkedController);
		this.unitMoveFinishedController = Preconditions.checkNotNull(unitMoveFinishedController);
	}

	public RoundStartedController getRoundStartedController() {
		return roundStartedController;
	}

	public UnitMovedController getUnitMovedController() {
		return unitMovedController;
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

	/**
	 * @return the unitMoveFinishedController
	 */
	public UnitMoveFinishedController getUnitMoveFinishedController() {
		return unitMoveFinishedController;
	}
	
}
