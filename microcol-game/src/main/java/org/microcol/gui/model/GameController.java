package org.microcol.gui.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.ai.SkyNet;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.Localized;
import org.microcol.gui.MusicController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.NewGameController;
import org.microcol.gui.event.NextTurnController;
import org.microcol.gui.event.TurnStartedController;
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
 * Exchange events between model and GUI.
 */
public class GameController implements Localized {

	private Logger logger = Logger.getLogger(GameController.class);

	private final NextTurnController nextTurnController;

	private final MoveUnitController moveUnitController;

	private final NewGameController newGameController;

	private final TurnStartedController turnStartedController;

	private final MusicController musicController;

	private final GamePreferences gamePreferences;

	private Game game;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveUnitController moveUnitController,
			final NewGameController newGameController, final TurnStartedController turnStartedController,
			final MusicController musicController, final GamePreferences gamePreferences) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
		this.newGameController = Preconditions.checkNotNull(newGameController);
		this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
		this.musicController = Preconditions.checkNotNull(musicController);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
	}

	/**
	 * Start new game and register listener.
	 */
	public void newGame() {
		GameBuilder builder = new GameBuilder();
		game = builder.setMap(500, 500).setCalendar(1570, 1800)
				.addPlayer("Player1", false).addShip("Player1", 5, 5, 5).addShip("Player1", 5, 4, 4)
				.addPlayer("Pocitac", true).addShip("Pocitac", 5, 6, 6).addShip("Pocitac", 5, 7, 7).addShip("Pocitac", 5, 8, 8)
				.build();
		game.addListener(new ModelListener() {

			@Override
			public void turnStarted(final TurnStartedEvent event) {
				logger.debug("Turn started for player '" + event.getPlayer().getName() + "'.");
				turnStartedController.fireTurnStartedEvent(event);
			}

			@Override
			public void shipMoved(final ShipMovedEvent event) {
				logger.debug("Ship moved " + event);
				moveUnitController.fireUnitMovedEvent(event);
			}

			@Override
			public void roundStarted(final RoundStartedEvent event) {
				logger.debug("Turn started for year '" + event.getCalendar().getCurrentYear() + "'.");
				nextTurnController.fireNextTurnEvent(event);
			}

			@Override
			public void gameStarted(final GameStartedEvent event) {
				logger.debug("Game started " + event);
				game = event.getGame();
				newGameController.fireNewGameStartedEvent(event);
			}

			@Override
			public void gameFinished(final GameFinishedEvent event) {
				logger.debug("Game finished " + event);
			}
		});
		SkyNet skyNet = new SkyNet(game);
		skyNet.searchAndDestroy();
		new Thread(() -> game.start()).start();
		musicController.start(gamePreferences.getVolume());
	}

	public Game getGame() {
		return game;
	}

	public void performMove(final Ship ship, final List<Location> path) {
		logger.debug("Start move ship: " + ship);
		new Thread(() -> ship.moveTo(new Path(path))).start();
		// moveAutomatization.addMove(new MoveAutomatization.MovePlanner(ship,
		// path));
	}

	public void nextTurn() {
		logger.debug("Next Year event was triggered.");
		// TODO JJ it always starts new thread, is it correct?
		new Thread(() -> game.endTurn()).start();
		// moveAutomatization.perforMoves();
	}

}
