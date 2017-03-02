package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
	// FIXME JKA Temporary hack.
	private static Game instance;

	private final ModelListenersManager listenersManager;
	private final Map map;
	private final Calendar calendar;
	private final List<Player> players;
	private final List<Ship> ships;

	private Player currentPlayer;
	private boolean started;

	protected Game(final Map map, final Calendar calendar, final List<Player> players, final List<Ship> ships) {
		// TODO JKA Add not null tests.
		this.listenersManager = new ModelListenersManager();
		this.map = map;
		this.calendar = calendar;
		this.players = new ArrayList<>(players);
		this.ships = new ArrayList<>(ships);

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

	// TODO JKA Předělat na immutable.
	public List<Player> getPlayers() {
		return players;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	// TODO JKA Předělat na immutable.
	public List<Ship> getShips() {
		return ships;
	}

	// TODO JKA Předělat na immutable.
	public List<Ship> getShipsAt(final Location location) {
		List<Ship> xxx = new ArrayList<>();
		ships.forEach(ship -> {
			if (ship.getLocation().equals(location)) {
				xxx.add(ship);
			}
		});
		return xxx;
	}

	// TODO JKA Předělat na immutable.
	public List<Ship> getCurrentPlayerShipsAt(final Location location) {
		List<Ship> xxx = new ArrayList<>();
		getShipsAt(location).forEach(ship -> {
			if (ship.getOwner().equals(getCurrentPlayer())) {
				xxx.add(ship);
			}
		});
		return xxx;
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
			}
			currentPlayer = players.get(0);
			listenersManager.fireRoundStarted(this, calendar);
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
