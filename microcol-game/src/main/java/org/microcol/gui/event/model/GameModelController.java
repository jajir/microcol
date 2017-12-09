package org.microcol.gui.event.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.KingPlayer;
import org.microcol.ai.SimpleAiPlayer;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.gui.util.Localized;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
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
