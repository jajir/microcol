package org.microcol.gui.event;

import java.util.List;
import java.util.Optional;

import org.microcol.ai.AIModelBuilder;
import org.microcol.ai.Engine;
import org.microcol.gui.Localized;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelBuilder;
import org.microcol.model.Path;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Exchange events between model and GUI.
 */
public class GameController implements Localized {

	private Logger logger = LoggerFactory.getLogger(GameController.class);

	private final ModelEventManager modelEventManager;

	private Optional<ModelListenerImpl> modelListener;

	private Optional<Model> model;

	private Optional<Engine> aiEngine;

	@Inject
	public GameController(final ModelEventManager modelEventManager) {
		this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
		model = Optional.empty();
		aiEngine = Optional.empty();
		modelListener = Optional.empty();
	}

	/**
	 * Start new game and register listener.
	 */
	public void newGame() {
		if (model.isPresent()) {
			stopGame();
		}

		Preconditions.checkArgument(!model.isPresent());
		Preconditions.checkArgument(!aiEngine.isPresent());
		Preconditions.checkArgument(!modelListener.isPresent());

		if ("true".equals(System.getProperty("development")) && "JKA".equals(System.getProperty("developer"))) {
			setAndStartModel(AIModelBuilder.build());
		} else {
			ModelBuilder builder = new ModelBuilder();
			builder.setCalendar(1570, 1800).setMap("/maps/test-map-ocean-1000x1000.txt").addPlayer("Player1", false)
					.addUnit("Player1", UnitType.GALLEON, Location.of(4, 2))
					.addUnit("Player1", UnitType.FRIGATE, Location.of(9, 7)).addPlayer("Player2", true)
					.addUnit("Player2", UnitType.GALLEON, Location.of(7, 7))
					.addUnit("Player2", UnitType.FRIGATE, Location.of(7, 9));
			setAndStartModel(builder.build());
		}
	}

	private void setAndStartModel(final Model newModel) {
		model = Optional.of(newModel);
		modelListener = Optional.of(new ModelListenerImpl(modelEventManager));
		model.get().addListener(modelListener.get());
		aiEngine = Optional.of(new Engine(model.get()));
		aiEngine.get().start();
		new Thread(() -> model.get().startGame()).start();
	}

	public void startNewGame() {
		newGame();
	}

	public void setModel(final Model newModel) {
		Preconditions.checkNotNull(newModel);
		stopGame();
		setAndStartModel(newModel);
	}

	public Model getModel() {
		return model.get();
	}

	public Optional<Model> getOptionalModel() {
		return model;
	}

	private void stopGame() {
		Preconditions.checkArgument(model.isPresent());
		Preconditions.checkArgument(aiEngine.isPresent());
		Preconditions.checkArgument(modelListener.isPresent());
		model.get().removeListener(modelListener.get());
		model = Optional.empty();
		aiEngine = Optional.empty();
		modelListener = Optional.empty();
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
		new Thread(() -> model.get().endTurn()).start();
	}

	public Engine getAiEngine() {
		return aiEngine.get();
	}

}
