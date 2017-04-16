package org.microcol.gui.event;

import java.util.List;

import org.microcol.ai.AIModelBuilder;
import org.microcol.ai.Engine;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.Localized;
import org.microcol.gui.MusicController;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.ModelBuilder;
import org.microcol.model.Path;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedEvent;
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

	private final UnitAttackedEventController unitAttackedEventController;

	private Model game;

	private Engine aiEngine;

	@Inject
	public GameController(final NextTurnController nextTurnController, final MoveUnitController moveUnitController,
			final NewGameController newGameController, final TurnStartedController turnStartedController,
			final MusicController musicController, final GamePreferences gamePreferences,
			final DebugRequestController debugRequestController, final GameFinishedController gameFinishedController,
			final UnitAttackedEventController unitAttackedEventController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
		this.newGameController = Preconditions.checkNotNull(newGameController);
		this.turnStartedController = Preconditions.checkNotNull(turnStartedController);
		this.musicController = Preconditions.checkNotNull(musicController);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.debugRequestController = Preconditions.checkNotNull(debugRequestController);
		this.gameFinishedController = Preconditions.checkNotNull(gameFinishedController);
		this.unitAttackedEventController = Preconditions.checkNotNull(unitAttackedEventController);
	}

	/**
	 * Start new game and register listener.
	 */
	public void newGame() {
		if (game != null) {
			// TODO JJ already running game should be stopped
		}
		if ("true".equals(System.getProperty("development")) && "JKA".equals(System.getProperty("developer"))) {
			game = AIModelBuilder.build();
		} else {
			ModelBuilder builder = new ModelBuilder();
			builder.setCalendar(1570, 1800).setMap("/maps/test-map-ocean-1000x1000.txt").addPlayer("Player1", false)
					.addUnit("Player1", UnitType.GALLEON, Location.of(4, 2))
					.addUnit("Player1", UnitType.FRIGATE, Location.of(9, 7)).addPlayer("Player2", true)
					.addUnit("Player2", UnitType.GALLEON, Location.of(7, 7))
					.addUnit("Player2", UnitType.FRIGATE, Location.of(7, 9));
			game = builder.build();
		}
		game.addListener(new ModelAdapter() {

			@Override
			public void turnStarted(final TurnStartedEvent event) {
				turnStartedController.fireEvent(event);
			}

			@Override
			public void unitMoved(final UnitMovedEvent event) {
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

			@Override
			public void unitAttacked(final UnitAttackedEvent event) {
				unitAttackedEventController.fireEvent(event);
			}
		});
		aiEngine = new Engine(game);
		aiEngine.start();
		new Thread(() -> game.startGame()).start();
		musicController.start(gamePreferences.getVolume());
	}

	public Model getModel() {
		return game;
	}

	public void performMove(final Unit ship, final List<Location> path) {
		logger.debug("Start move ship: " + ship);
		new Thread(() -> ship.moveTo(Path.of(path))).start();
	}

	public void performFight(final Unit attacker, final Unit defender) {
		logger.debug("Start move ship: " + attacker);
		new Thread(() -> {
			/**
			 * If it's necessary than move.
			 */
			if (attacker.getPath(defender.getLocation(), true).isPresent()) {
				final List<Location> locations = attacker.getPath(defender.getLocation(), true).get();
				attacker.moveTo(Path.of(locations));
				attacker.attack(defender.getLocation());
			} else {
				attacker.attack(defender.getLocation());
			}
		}).start();
	}

	public void nextTurn() {
		logger.debug("Next Year event was triggered.");
		new Thread(() -> game.endTurn()).start();
	}

	public Engine getAiEngine() {
		return aiEngine;
	}

}
