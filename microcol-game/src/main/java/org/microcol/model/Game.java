package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Game {
	// FIXME JKA Temporary hack.
	private static Game instance;

	private final ModelListenersManager listenersManager;
	private final Map map;
	private final Calendar calendar;
	private final ImmutableList<Player> players;
	private final ImmutableList<Ship> ships;

	private Player currentPlayer;
	private boolean started;

	protected Game(final Map map, final Calendar calendar, final List<Player> players, final List<Ship> ships) {
		this.listenersManager = new ModelListenersManager();
		this.map = Preconditions.checkNotNull(map);
		this.calendar = Preconditions.checkNotNull(calendar);
		this.players = ImmutableList.copyOf(players);
		this.ships = ImmutableList.copyOf(ships);

		instance = this;
	}

	protected ModelListenersManager getListenersManager() {
		return listenersManager;
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
		// TODO JKA Check game state.

		return currentPlayer;
	}

	public List<Ship> getShips() {
		return ships;
	}

	// TODO JKA Předělat na immutable.
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
 
	public void start() {
		// TODO JKA Předělat.
		started = true;
		listenersManager.fireGameStarted(this);
		listenersManager.fireRoundStarted(this, calendar);
		currentPlayer = players.get(0);
		players.forEach(player -> {
			player.startGame(this);
		});
		ships.forEach(ship -> {
			if (ship.getOwner().equals(currentPlayer)) {
				ship.startTurn();
			}
		});
		listenersManager.fireTurnStarted(this, currentPlayer);
	}

	public boolean isFinished() {
		return calendar.isFinished();
	}

	public boolean isActive() {
		return started && !isFinished();
	}

	public void endTurn() {
		// TODO JKA Předělat.
		final int index = players.indexOf(currentPlayer);
		// TODO JKA Předělat na modulo.
		if (index < players.size() - 1) {
			currentPlayer = players.get(index + 1);
		} else {
			calendar.endRound();
			if (calendar.isFinished()) {
				listenersManager.fireGameFinished(this);
			} else {
				currentPlayer = players.get(0);
				listenersManager.fireRoundStarted(this, calendar);
			}
		}
		ships.forEach(ship -> {
			if (ship.getOwner().equals(currentPlayer)) {
				ship.startTurn();
			}
		});
		listenersManager.fireTurnStarted(this, currentPlayer);
	}

	protected static Game getInstance() {
		return instance;
	}
}
