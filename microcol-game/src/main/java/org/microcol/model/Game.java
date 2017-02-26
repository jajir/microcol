package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

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

	// TODO JKA Move to unit tests.
	public static void main(String[] args) {
		GameBuilder builder = new GameBuilder();
		Game game = builder
			.setMap(100, 50)
			.setCalendar(1, 3)
			.addPlayer("Player1", true)
			.addShip("Player1", 5, 50, 20)
			.build();
		game.addListener(new ModelListener() {
			@Override
			public void gameStarted(GameStartedEvent event) {
				System.out.println("Game started: " + event);
			}

			@Override
			public void roundStarted(RoundStartedEvent event) {
				System.out.println("New round started: " + event);
			}

			@Override
			public void turnStarted(TurnStartedEvent event) {
				System.out.println("New turn started: " + event);
			}

			@Override
			public void shipMoved(ShipMovedEvent event) {
				System.out.println("Ship moved: " + event);
			}

			@Override
			public void gameFinished(GameFinishedEvent event) {
				System.out.println("Game finished: " + event);
			}
		});
		game.start();
		Ship ship = game.getShips().get(0);
		List<Location> locations = new ArrayList<>();
		locations.add(new Location(51, 21));
		locations.add(new Location(52, 22));
		locations.add(new Location(53, 23));
		Path path = new Path(locations);
		ship.moveTo(path);
		game.endTurn();
		System.out.println("Game finished: " + game.isFinished());
		game.endTurn();
		System.out.println("Game finished: " + game.isFinished());
	}
}
