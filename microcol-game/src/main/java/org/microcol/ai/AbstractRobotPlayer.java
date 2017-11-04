package org.microcol.ai;

import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Base class for robot players AI or king.
 */
public abstract class AbstractRobotPlayer {

	private final Logger logger = LoggerFactory.getLogger(AbstractRobotPlayer.class);

	private final Model model;
	
	private final ModelAdapter modelAdapter;
	
	/**
	 * Controlled player.
	 */
	private final Player player;

	private boolean running;

	public AbstractRobotPlayer(final Model model, final Player player) {
		this.model = Preconditions.checkNotNull(model);
		this.player = Preconditions.checkNotNull(player);
		modelAdapter = new ModelAdapter() {
			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().isComputer()) {
					turn(event.getPlayer());
				}
			}
		};
		model.addListener(modelAdapter);
		running = true;
		logger.info("Robot player started.");
	}
	
	public void stop() {
		model.removeListener(modelAdapter);
	}

	public void suspend() {
		running = false;
	}

	public void resume() {
		running = true;
		if (model.isGameRunning() && model.getCurrentPlayer().isComputer()) {
			turn(model.getCurrentPlayer());
		}
	}

	private void turn(final Player player) {
		if (!running) {
			return;
		}
		player.getUnits().stream().filter(unit -> unit.isAtPlaceLocation()).forEach(unit -> move(unit));
		player.endTurn();
	}

	private void move(final Unit unit) {
		if (!running) {
			return;
		}
		moveUnit(unit);
	}

	abstract void moveUnit(final Unit unit);

	/**
	 * @return the model
	 */
	protected Model getModel() {
		return model;
	}

	/**
	 * @return the player
	 */
	protected Player getPlayer() {
		return player;
	}

}
