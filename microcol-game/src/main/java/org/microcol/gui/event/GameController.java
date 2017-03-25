package org.microcol.gui.event;

import java.util.List;

import org.microcol.ai.SkyNet;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.Localized;
import org.microcol.gui.MusicController;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.ModelBuilder;
import org.microcol.model.Path;
import org.microcol.model.Ship;
import org.microcol.model.ShipType;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Exchange events between model and GUI.
 */
public class GameController implements Localized {

	private Logger logger = LoggerFactory.getLogger(GameController.class);

	private final NextTurnController nextTurnController;

	private final MoveUnitController moveUnitController;

	private final NewGameController newGameController;

	private final TurnStartedController turnStartedController;

	private final MusicController musicController;

	private final GamePreferences gamePreferences;

	private final DebugRequestController debugRequestController;

	private final GameFinishedController gameFinishedController;

	private Model game;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveUnitController moveUnitController,
			final NewGameController newGameController, final TurnStartedController turnStartedController,
			final MusicController musicController, final GamePreferences gamePreferences,
			final DebugRequestController debugRequestController, final GameFinishedController gameFinishedController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
		this.newGameController = Preconditions.checkNotNull(newGameController);
		this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
		this.musicController = Preconditions.checkNotNull(musicController);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.debugRequestController = Preconditions.checkNotNull(debugRequestController);
		this.gameFinishedController = Preconditions.checkNotNull(gameFinishedController);
	}

	/**
	 * Start new game and register listener.
	 */
	public void newGame() {
		ModelBuilder builder = new ModelBuilder();
		game = builder.setCalendar(1570, 1571)
				// .setMap(15, 10)
				.setMap("/maps/map-01.txt")
				.addPlayer("Player1", true)
					.addShip("Player1", ShipType.GALLEON, Location.of(4, 2))
					.addShip("Player1", ShipType.FRIGATE, Location.of(3, 3))
				.addPlayer("Player2", true)
					.addShip("Player2", ShipType.GALLEON, Location.of(7, 7))
					.addShip("Player2", ShipType.FRIGATE, Location.of(7, 9))
					.addShip("Player2", ShipType.FRIGATE, Location.of(14, 9))
//				.setMap("/maps/map-02.txt")
//				.addPlayer("Player1", true)
//					.addShip("Player1", ShipType.GALLEON, Location.of(1, 1))
//					.addShip("Player1", ShipType.FRIGATE, Location.of(3, 1))
//				.addPlayer("Player2", true)
//					.addShip("Player2", ShipType.GALLEON, Location.of(3, 3))
//					.addShip("Player2", ShipType.FRIGATE, Location.of(1, 3))
				.build();
		game.addListener(new ModelAdapter() {

			@Override
			public void turnStarted(final TurnStartedEvent event) {
				turnStartedController.fireEvent(event);
			}

			@Override
			public void shipMoved(final ShipMovedEvent event) {
				moveUnitController.fireUnitMovedEvent(event);
			}

			@Override
			public void roundStarted(final RoundStartedEvent event) {
				nextTurnController.fireEvent(event);
			}

			@Override
			public void gameStarted(final GameStartedEvent event) {
				game = event.getModel();
				newGameController.fireEvent(event);
			}

			@Override
			public void gameFinished(final GameFinishedEvent event) {
				logger.debug("Game finished " + event);
				gameFinishedController.fireEvent(event);
			}

			@Override
			public void debugRequested(final DebugRequestedEvent event) {
				debugRequestController.fireEvent(event);
			}
		});
		SkyNet skyNet = new SkyNet(game);
		skyNet.searchAndDestroy();
		new Thread(() -> game.startGame()).start();
		musicController.start(gamePreferences.getVolume());
	}

	public Model getModel() {
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
