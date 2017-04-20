package org.microcol.gui.event.model;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Events incoming from model are translated to GIU events. This class manage
 * GUI event controllers for model events.
 */
public class ModelEventManager {
	
	private final NextTurnController nextTurnController;

	private final MoveUnitController moveUnitController;

	private final NewGameController newGameController;

	private final TurnStartedController turnStartedController;

	private final DebugRequestController debugRequestController;

	private final GameFinishedController gameFinishedController;

	private final UnitAttackedEventController unitAttackedEventController;

	@Inject
	public ModelEventManager(final NextTurnController nextTurnController, final MoveUnitController moveUnitController,
			final NewGameController newGameController, final TurnStartedController turnStartedController,
			final DebugRequestController debugRequestController, final GameFinishedController gameFinishedController,
			final UnitAttackedEventController unitAttackedEventController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
		this.newGameController = Preconditions.checkNotNull(newGameController);
		this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
		this.debugRequestController = Preconditions.checkNotNull(debugRequestController);
		this.gameFinishedController = Preconditions.checkNotNull(gameFinishedController);
		this.unitAttackedEventController = Preconditions.checkNotNull(unitAttackedEventController);
	}

	public NextTurnController getNextTurnController() {
		return nextTurnController;
	}

	public MoveUnitController getMoveUnitController() {
		return moveUnitController;
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
}
