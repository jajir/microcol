package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Game {
	private final Map map;
	private final Calendar calendar;
	private final ImmutableList<Player> players;
	private final ImmutableList<Ship> ships;

	private final ModelListenersManager listenersManager;

	private Player currentPlayer;
	private boolean started;

	protected Game(final Map map, final Calendar calendar, final List<Player> players, final List<Ship> ships) {
		this.map = Preconditions.checkNotNull(map);
		this.calendar = Preconditions.checkNotNull(calendar);
		this.players = ImmutableList.copyOf(players);
		this.ships = ImmutableList.copyOf(ships);

		this.listenersManager = new ModelListenersManager();
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

	public List<Ship> getShips() {
		return ships;
	}

	public List<Ship> getShipsAt(final Location location) {
		List<Ship> shipsAt = new ArrayList<>();
		ships.forEach(ship -> {
			if (ship.getLocation().equals(location)) {
				shipsAt.add(ship);
			}
		});

		// TODO JKA Optimalizovat
		return ImmutableList.copyOf(shipsAt);
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
		players.forEach(player -> {
			player.startGame(this);
		});
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
