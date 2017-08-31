package org.microcol.gui.event.model;

import java.util.List;
import java.util.Optional;

import org.microcol.ai.AIModelBuilder;
import org.microcol.ai.Engine;
import org.microcol.gui.util.Localized;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodType;
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
	public void startNewGame() {
		if (model.isPresent()) {
			stopGame();
		}

		Preconditions.checkArgument(!model.isPresent());
		Preconditions.checkArgument(!aiEngine.isPresent());
		Preconditions.checkArgument(!modelListener.isPresent());

		if ("true".equals(System.getProperty("development")) && "JKA".equals(System.getProperty("developer"))) {
			setAndStartModel(AIModelBuilder.build());
		} else {
			setAndStartModel(buidModel());
		}
	}

	private Model buidModel() {
		return buildComplexModel();
	}

	private Model buildComplexModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.setCalendar(1570, 1800)
			.setMap("/maps/test-map-simple-test.txt")

			/**
			 * Human player
			 */
			.addPlayer("Dutch")
				.setComputerPlayer(false)
				.setGold(1108)
				.addTown("brunswick")
					.setLocation(Location.of(5, 4))
					.setDefaultConstructions(true)
					.setWorker(ConstructionType.RUM_DISTILLERS_HOUSE, 0, UnitType.COLONIST)
					.setWorker(ConstructionType.CARPENTERS_SHOP, 0, UnitType.COLONIST)
					.setWorker(ConstructionType.TOWN_HALL, 2, UnitType.COLONIST)
					.setWorker(Location.DIRECTION_NORTH_EAST, UnitType.COLONIST)
					.setWorker(Location.DIRECTION_SOUTH_WEST, UnitType.COLONIST)
					.make()
				.make()
			.addUnit(UnitType.GALLEON, "Dutch", Location.of(5, 3))
			.addUnit(UnitType.FRIGATE, "Dutch", Location.of(4, 4))
			.addUnit(UnitType.COLONIST, "Dutch", Location.of(9, 4))
			.addUnit(UnitType.COLONIST, "Dutch", Location.of(6, 3))
			.addUnit(UnitType.COLONIST, "Dutch", Location.of(5, 4))

			.addUnit(builder.makeUnitBuilder().setType(UnitType.FRIGATE).setPlayerName("Dutch")
					.setShipIncomingToEurope(4).build())
			.addUnit(builder.makeUnitBuilder().setType(UnitType.GALLEON).setPlayerName("Dutch")
					.setShipIncomingToColonies(2).build())

			/**
			 * Opponent player2
			 */
			.addPlayer("Player2")
				.setComputerPlayer(true)
				.setGold(100)
				.make()
			.addUnit(UnitType.GALLEON, "Player2", Location.of(8, 8))
			.addUnit(UnitType.FRIGATE, "Player2", Location.of(8, 10))
			.addUnit(UnitType.FRIGATE, "Player2", Location.of(15, 10))
			.addUnit(UnitType.COLONIST, "Player2", Location.of(8, 4))

			/**
			 * Europe port
			 */
			.addUnit(builder.makeUnitBuilder().setType(UnitType.COLONIST).setPlayerName("Dutch")
					.setUnitToEuropePortPier().build())
			.addUnit(builder.makeUnitBuilder().setType(UnitType.COLONIST).setPlayerName("Dutch")
					.setUnitToEuropePortPier().build())
			.getEuropeBuilder()
			.addShipToPort(builder.makeUnitBuilder().setType(UnitType.GALLEON).setLocation(Location.of(2, 2))
					.setPlayerName("Dutch").addCargoGood(GoodType.COTTON, 100)
					.addCargoUnit(UnitType.COLONIST, true, false, false).build())
			.addShipToPort(builder.makeUnitBuilder().setType(UnitType.FRIGATE).setLocation(Location.of(2, 2))
					.setPlayerName("Dutch").addCargoGood(GoodType.CIGARS, 100).addCargoGood(GoodType.RUM, 100)
					.addCargoGood(GoodType.SILVER, 100).build())
			.build();

		return builder.build();
	}

	private void setAndStartModel(final Model newModel) {
		model = Optional.of(newModel);
		modelListener = Optional.of(new ModelListenerImpl(modelEventManager));
		model.get().addListener(modelListener.get());
		aiEngine = Optional.of(new Engine(model.get()));
		aiEngine.get().start();
		if (!model.get().isGameStarted()) {
			new Thread(() -> model.get().startGame()).start();
		}
	}

	public void setModel(final Model newModel) {
		Preconditions.checkNotNull(newModel);
		stopGame();
		setAndStartModel(newModel);
	}

	public Model getModel() {
		return model.orElseThrow(() -> new IllegalStateException("Model is not ready"));
	}

	public boolean isModelReady() {
		return model.isPresent();
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
			final Optional<List<Location>> locations = attacker.getPath(defender.getLocation(), true);
			if (locations.isPresent() && !locations.get().isEmpty()) {
				attacker.moveTo(Path.of(locations.get()));
			}
			attacker.attack(defender.getLocation());
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
