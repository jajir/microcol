package org.microcol.model;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Model {
	private final ListenerManager listenerManager;

	private final Calendar calendar;
	private final World world;
	private final ImmutableList<Player> players;

	private final ShipStorage shipStorage;
	private final GameManager gameManager;

	Model(final Calendar calendar, final World world, final List<Player> players, final List<Ship> ships) {
		listenerManager = new ListenerManager();

		this.calendar = Preconditions.checkNotNull(calendar);
		this.world = Preconditions.checkNotNull(world);

		this.players = ImmutableList.copyOf(players);
		this.players.forEach(player -> {
			player.setModel(this);
		});

		shipStorage = new ShipStorage(ships);
		shipStorage.getShips().forEach(ship -> {
			ship.setModel(this);
		});

		gameManager = new GameManager(this);
	}

	public void addListener(ModelListener listener) {
		listenerManager.addListener(listener);
	}

	public void removeListener(ModelListener listener) {
		listenerManager.removeListener(listener);
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public World getWorld() {
		return world;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getCurrentPlayer() {
		return gameManager.getCurrentPlayer();
	}

	ShipStorage getShipStorage() {
		return shipStorage;
	}

	public List<Ship> getShips() {
		return shipStorage.getShips();
	}

	public Map<Location, List<Ship>> getShipsAt() {
		return shipStorage.getShipsAt();
	}

	public List<Ship> getShipsAt(final Location location) {
		return shipStorage.getShipsAt(location);
	}

	public void startGame() {
		gameManager.startGame();
	}

	public void endTurn() {
		gameManager.endTurn();
	}

	void checkGameActive() {
		gameManager.checkGameActive();
	}

	void checkCurrentPlayer(final Player player) {
		gameManager.checkCurrentPlayer(player);
	}

	void fireGameStarted() {
		listenerManager.fireGameStarted(this);
	}

	void fireRoundStarted() {
		listenerManager.fireRoundStarted(this, calendar);
	}

	void fireTurnStarted(final Player player) {
		listenerManager.fireTurnStarted(this, player);
	}

	void fireShipMoved(final Ship ship, final Location start, final Path path) {
		listenerManager.fireShipMoved(this, ship, start, path);
	}

	void fireGameFinished() {
		listenerManager.fireGameFinished(this);
	}
}
