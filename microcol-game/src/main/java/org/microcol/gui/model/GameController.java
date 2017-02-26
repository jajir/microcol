package org.microcol.gui.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.gui.Localized;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.NextTurnController;
import org.microcol.model.Game;
import org.microcol.model.GameBuilder;
import org.microcol.model.Location;
import org.microcol.model.ModelListener;
import org.microcol.model.Path;
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

	private Logger logger = Logger.getLogger(GameController.class);

	private final NextTurnController nextTurnController;

	private final MoveUnitController moveUnitController;

	private Game game;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveUnitController moveUnitController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
	}

	public void newGame() {
		GameBuilder builder = new GameBuilder();
		game = builder.setMap(50, 50).setCalendar(1570, 1800).addPlayer("Player1", true).addShip("Player1", 5, 5, 5)
				.addPlayer("Pocitac", false).addShip("Pocitac", 5, 10, 10).build();
		game.addListener(new ModelListener() {

			@Override
			public void turnStarted(final TurnStartedEvent event) {
				// TODO Auto-generated method stub
				logger.debug("Turn started for player '" + event.getPlayer().getName() + "'.");
			}

			@Override
			public void shipMoved(final ShipMovedEvent event) {
				logger.debug("Ship moved " + event);
				// TODO JJ enable move animation by following code
				moveUnitController.fireMoveUnitEvent(event);
			}

			@Override
			public void roundStarted(final RoundStartedEvent event) {
				// TODO JJ pass calendar
				nextTurnController.fireNextTurnEvent(event.getGame());
			}

			@Override
			public void gameStarted(final GameStartedEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void gameFinished(final GameFinishedEvent event) {
				// TODO Auto-generated method stub

			}
		});
		game.start();
	}

	public Game getGame() {
		return game;
	}

	public void performMove(final Ship ship, final List<Location> path) {
		logger.debug("Start move ship: " + ship);
		ship.moveTo(new Path(path));
		// moveAutomatization.addMove(new MoveAutomatization.MovePlanner(ship,
		// path));
	}

	public void nextTurn() {
		logger.debug("Next Year event was triggered.");
		game.endTurn();
		// moveAutomatization.perforMoves();
	}

}
