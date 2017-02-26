package org.microcol.gui.model;

import java.util.List;

import org.microcol.gui.Localized;
import org.microcol.gui.MoveAutomatization;
import org.microcol.gui.event.NextTurnController;
import org.microcol.model.Game;
import org.microcol.model.GameBuilder;
import org.microcol.model.GameListener;
import org.microcol.model.Location;
import org.microcol.model.Ship;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Communicate between model and GUI.
 */
public class GameController implements Localized {

	private final NextTurnController nextTurnController;

	private final MoveAutomatization moveAutomatization;

	private Game game;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveAutomatization moveAutomatization) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveAutomatization = Preconditions.checkNotNull(moveAutomatization);
	}

	public void newGame() {
		GameBuilder builder = new GameBuilder();
		game = builder.setMap(50, 50).setCalendar(1, 3).addPlayer("Player1", true).addShip("Player1", 5, 10, 10)
				.addPlayer("Pocitac", false).addShip("Pocitac", 5, 10, 10).build();
		game.addListener(new GameListener() {
			
			@Override
			public void turnStarted(TurnStartedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void shipMoved(ShipMovedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void roundStarted(RoundStartedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void gameStarted(GameStartedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void gameFinished(GameFinishedEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public Game getWorld() {
		return game;
	}

	public void performMove(final Ship ship, final List<Location> path) {
		moveAutomatization.addMove(new MoveAutomatization.MovePlanner(ship, path));
	}

	public void nextTurn() {
		game.endTurn();
		moveAutomatization.perforMoves();
	}

}
