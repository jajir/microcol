package org.microcol.gui.model;

import java.util.List;

import org.microcol.gui.Localized;
import org.microcol.gui.MoveAutomatization;
import org.microcol.gui.event.NextTurnController;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GameController implements Localized {

	private final NextTurnController nextTurnController;

	private final MoveAutomatization moveAutomatization;

	private World world;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveAutomatization moveAutomatization) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveAutomatization = Preconditions.checkNotNull(moveAutomatization);
	}

	public void newGame() {
		world = new World(nextTurnController, getText());
		nextTurnController.fireNextTurnEvent(world);
	}

	public World getWorld() {
		return world;
	}

	public void performMove(final Ship ship, final List<Location> path) {
		moveAutomatization.addMove(new MoveAutomatization.MovePlanner(ship, path));
	}

	public void nextTurn() {
		world.nextTurn();
		moveAutomatization.perforMoves();
	}

}
