package org.microcol.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Game {
	private final Map map;
	private final Calendar calendar;
	private final ImmutableList<Player> players;

	private final ModelListenersManager listenersManager;
	private final ShipsStorage shipsStorage; 

	private Player currentPlayer;
	private boolean started;

	protected Game(final Map map, final Calendar calendar, final List<Player> players, final List<Ship> ships) {
		this.map = Preconditions.checkNotNull(map);
		this.calendar = Preconditions.checkNotNull(calendar);
		this.players = ImmutableList.copyOf(players);
		this.players.forEach(player -> {
			player.setGame(this);
		});

		listenersManager = new ModelListenersManager();
		shipsStorage = new ShipsStorage(ships);
		shipsStorage.getShips().forEach(ship -> {
			ship.setGame(this);
		});
	}

	public void addListener(ModelListener listener) {
		listenersManager.addListener(listener);
	}

	public void removeListener(ModelListener listener) {
		listenersManager.removeListener(listener);
	}

	public Map getMap() {
		return map;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getCurrentPlayer() {
		Preconditions.checkState(isActive(), "Game must be active.");

		return currentPlayer;
	}

	protected ShipsStorage getShipsStorage() {
		return shipsStorage;
	}

	public List<Ship> getShips() {
		return shipsStorage.getShips();
	}

	public List<Ship> getShipsAt(final Location location) {
		return shipsStorage.getShipsAt(location);
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return calendar.isFinished();
	}

	protected boolean isActive() {
		return started && !isFinished();
	}

	public void start() {
		Preconditions.checkState(!started, "Game was already started.");

		started = true;
		currentPlayer = players.get(0);
		listenersManager.fireGameStarted(this);
		listenersManager.fireRoundStarted(this, calendar);
		currentPlayer.startTurn();
		listenersManager.fireTurnStarted(this, currentPlayer);
	}

	public void endTurn() {
		Preconditions.checkState(isActive(), "Game must be active.");

		final int index = players.indexOf(currentPlayer);
		if (index < players.size() - 1) {
			currentPlayer = players.get(index + 1);
			currentPlayer.startTurn();
			listenersManager.fireTurnStarted(this, currentPlayer);
		} else {
			calendar.endRound();
			if (!calendar.isFinished()) {
				currentPlayer = players.get(0);
				listenersManager.fireRoundStarted(this, calendar);
				currentPlayer.startTurn();
				listenersManager.fireTurnStarted(this, currentPlayer);
			} else {
				listenersManager.fireGameFinished(this);
			}
		}
	}

	protected void fireShipMoved(final Game game, final Ship ship, final Location startLocation, final Path path) {
		listenersManager.fireShipMoved(game, ship, startLocation, path);
	}
}
