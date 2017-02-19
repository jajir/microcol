package org.microcol.model;

import org.microcol.gui.Localized;
import org.microcol.gui.MoveUnitController;
import org.microcol.gui.NextTurnController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GameController implements Localized {

	private final NextTurnController nextTurnController;

	private final MoveUnitController moveUnitController;

	private World world;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveUnitController moveUnitController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
	}

	public void newGame() {
		world = new World(nextTurnController, moveUnitController, getText());
		nextTurnController.fireNextTurnEvent(world);
	}

	public World getWorld() {
		return world;
	}

}
