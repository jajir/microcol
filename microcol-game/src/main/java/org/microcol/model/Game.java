package org.microcol.model;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Game {
	private final Calendar calendar;
	private final World world;
	private final ImmutableList<Player> players;

	private final ListenerManager listenerManager;
	private final ShipStorage shipStorage; 

	private Player currentPlayer;
	private boolean started;

	protected Game(final Calendar calendar, final World world, final List<Player> players, final List<Ship> ships) {
		this.calendar = Preconditions.checkNotNull(calendar);
		this.world = Preconditions.checkNotNull(world);
		this.players = ImmutableList.copyOf(players);
		this.players.forEach(player -> {
			player.setGame(this);
		});

		listenerManager = new ListenerManager();
		shipStorage = new ShipStorage(ships);
		shipStorage.getShips().forEach(ship -> {
			ship.setGame(this);
		});
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
		Preconditions.checkState(isActive(), "Game must be active.");

		return currentPlayer;
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

	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return calendar.isFinished();
	}

	boolean isActive() {
		return started && !isFinished();
	}

	public void start() {
		Preconditions.checkState(!started, "Game was already started.");

		started = true;
		currentPlayer = players.get(0);
		listenerManager.fireGameStarted(this);
		listenerManager.fireRoundStarted(this, calendar);
		currentPlayer.startTurn();
		listenerManager.fireTurnStarted(this, currentPlayer);
	}

	public void endTurn() {
		Preconditions.checkState(isActive(), "Game must be active.");

		final int index = players.indexOf(currentPlayer);
		if (index < players.size() - 1) {
			currentPlayer = players.get(index + 1);
			currentPlayer.startTurn();
			listenerManager.fireTurnStarted(this, currentPlayer);
		} else {
			calendar.endRound();
			if (!calendar.isFinished()) {
				currentPlayer = players.get(0);
				listenerManager.fireRoundStarted(this, calendar);
				currentPlayer.startTurn();
				listenerManager.fireTurnStarted(this, currentPlayer);
			} else {
				listenerManager.fireGameFinished(this);
			}
		}
	}

	void fireShipMoved(final Game game, final Ship ship, final Location start, final Path path) {
		listenerManager.fireShipMoved(game, ship, start, path);
	}
}
