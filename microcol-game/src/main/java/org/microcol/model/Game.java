package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.GameListener;

public class Game {
	private final GameListenersManager listenersManager;
	private final Map map;
	private final List<Player> players;
	private Player currentPlayer;
	private final List<Ship> ships;
	private boolean started;
	private boolean finished;

	public Game(final Map map, final List<Player> players, final List<Ship> ships) {
		// TODO JKA Add not null tests.
		this.listenersManager = new GameListenersManager();
		this.map = map;
		this.players = new ArrayList<>(players);
		this.currentPlayer = players.get(0);
		this.ships = new ArrayList<>(ships);
	}

	public void addListener(GameListener listener) {
		listenersManager.addListener(listener);
	}

	public void removeListener(GameListener listener) {
		listenersManager.removeListener(listener);
	}

	public Map getMap() {
		return map;
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

	public boolean isStarted() {
		return started;
	}

	public void start() {
		// FIXME JKA Implement.
	}

	public boolean isFinished() {
		return finished;
	}
}
