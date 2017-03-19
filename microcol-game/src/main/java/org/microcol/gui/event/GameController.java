package org.microcol.gui.event;

import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.ai.SkyNet;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.Localized;
import org.microcol.gui.MusicController;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelBuilder;
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

	private Model game;

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
		ModelBuilder builder = new ModelBuilder();
		game = builder.setCalendar(1570, 1800)
			//.setWorld(15, 10)
			.setWorld("/maps/map-01.txt")
			.addPlayer("Player1", true).addShip("Player1", 5, Location.of(4, 2)).addShip("Player1", 5, Location.of(3, 3))
			.addPlayer("Player2", true).addShip("Player2", 5, Location.of(7, 7)).addShip("Player2", 5, Location.of(7, 9)).addShip("Player2", 5, Location.of(14, 9))
			//.addPlayer("Player3", true)
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
				game = event.getModel();
				newGameController.fireNewGameStartedEvent(event);
			}

			@Override
			public void gameFinished(final GameFinishedEvent event) {
				logger.debug("Game finished " + event);
			}
		});
		SkyNet skyNet = new SkyNet(game);
		skyNet.searchAndDestroy();
		new Thread(() -> game.startGame()).start();
		musicController.start(gamePreferences.getVolume());
	}

	public Model getGame() {
		return game;
	}

	public void performMove(final Ship ship, final List<Location> path) {
		logger.debug("Start move ship: " + ship);
		new Thread(() -> ship.moveTo(Path.of(path))).start();
		// moveAutomatization.addMove(new MoveAutomatization.MovePlanner(ship,
		// path));
	}

	public void nextTurn() {
		logger.debug("Next Year event was triggered.");
		new Thread(() -> game.endTurn()).start();
		// moveAutomatization.perforMoves();
	}

}
