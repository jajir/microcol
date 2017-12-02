package org.microcol.gui.event.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.KingPlayer;
import org.microcol.ai.SimpleAiPlayer;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.gui.util.Localized;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelBuilder;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Exchange events between model and GUI.
 * <p>
 * There is one class instance in runtime.
 * </p>
 */
public class GameModelController implements Localized {

	private final static Logger logger = LoggerFactory.getLogger(GameModelController.class);

	private final ModelEventManager modelEventManager;
	
	private final AnimationManager animationManager;

	private ModelListenerImpl modelListener;

	private Model model;

	private List<AbstractRobotPlayer> players;

	@Inject
	public GameModelController(final ModelEventManager modelEventManager, final AnimationManager animationManager) {
		this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
		this.animationManager = Preconditions.checkNotNull(animationManager);
		model = null;
		modelListener = null;
	}

	/**
	 * Start new game and register listener. Model will be removed.
	 */
	public void startNewDefaultGame() {
		tryToStopGame();
		setAndStartModel(buildComplexModel());
	}

	private Model buildComplexModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.setMap("/maps/test2.json").setCalendar(1570, 1800)

				/**
				 * Human player
				 */
				.addPlayer("Dutch").setComputerPlayer(false).setGold(1108).addColony("brunswick")
				.setLocation(Location.of(5, 4)).setDefaultConstructions()
				.setWorker(ConstructionType.RUM_DISTILLERS_HOUSE, 0, UnitType.COLONIST)
				.setWorker(ConstructionType.CARPENTERS_SHOP, 0, UnitType.COLONIST)
				.setWorker(ConstructionType.TOWN_HALL, 2, UnitType.COLONIST)
				.setWorker(Location.DIRECTION_NORTH_EAST, UnitType.COLONIST, GoodType.CORN)
				.setWorker(Location.DIRECTION_SOUTH_WEST, UnitType.COLONIST, GoodType.CORN).setGood(GoodType.CIGARS, 33)
				.setGood(GoodType.COAT, 100).setGood(GoodType.CORN, 75).build().build()
				.addUnit(UnitType.GALLEON, "Dutch", Location.of(5, 4))
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
				.addPlayer("Player2").setComputerPlayer(true).setGold(100).build()
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
				.addShipToPort(builder.makeUnitBuilder().setType(UnitType.GALLEON).setUnitToEuropePortPier()
						.setPlayerName("Dutch").addCargoGood(GoodType.COTTON, 100).addCargoUnit(UnitType.COLONIST)
						.build())
				.addShipToPort(builder.makeUnitBuilder().setType(UnitType.FRIGATE).setUnitToEuropePortPier()
						.setPlayerName("Dutch").addCargoGood(GoodType.CIGARS, 100).addCargoGood(GoodType.RUM, 100)
						.addCargoGood(GoodType.SILVER, 100).build())
				.build();

		return builder.build();
	}

	/**
	 * It start new game from model and prepare King AI and players AI.
	 * 
	 * @param newModel
	 *            required game model
	 */
	public void setAndStartModel(final Model newModel) {
		tryToStopGame();
		model = Preconditions.checkNotNull(newModel);
		players = new ArrayList<>();
		model.getPlayers().stream().filter(player -> player.isKing()).forEach(player -> {
			players.add(new KingPlayer(model, player, animationManager));
		});
		model.getPlayers().stream().filter(player -> !player.isKing() && player.isComputer()).forEach(player -> {
			players.add(new SimpleAiPlayer(model, player, animationManager));
		});
		modelListener = new ModelListenerImpl(modelEventManager);
		model.addListener(modelListener);
		model.startGame();
		// XXX start game is quite complex, but starting in separate thread
		// leads to error
		// if (!model.get().isGameStarted()) {
		// new Thread(() -> model.get().startGame()).start();
		// }
	}

	public Model getModel() {
		Preconditions.checkState(model != null, "Model is not ready");
		return model;
	}

	public boolean isModelReady() {
		return model != null;
	}

	public Player getCurrentPlayer() {
		return getModel().getCurrentPlayer();
	}

	private void tryToStopGame() {
		if(model != null){
			Preconditions.checkArgument(model != null);
			Preconditions.checkArgument(modelListener != null);
			model.removeListener(modelListener);
			model = null;
			modelListener = null;
			players.forEach(player -> player.stop());
			players = null;
		}
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
		new Thread(() -> model.endTurn()).start();
	}

	public void suspendAi() {
		players.forEach(player -> player.suspend());
	}

	public void resumeAi() {
		players.forEach(player -> player.resume());
	}

}
