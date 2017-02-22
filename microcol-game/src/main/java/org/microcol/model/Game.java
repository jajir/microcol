package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.GameListener;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

public class Game {
	// FIXME JKA Temporary hack.
	protected static Game instance;

	// TODO JKA Change protected.
	protected final GameListenersManager listenersManager;
	private final Map map;
	private final Calendar calendar;
	private final List<Player> players;
	private Player currentPlayer;
	private final List<Ship> ships;
	private boolean started;
	private boolean finished;

	protected Game(final Map map, final Calendar calendar, final List<Player> players, final List<Ship> ships) {
		// TODO JKA Add not null tests.
		this.listenersManager = new GameListenersManager();
		this.map = map;
		this.calendar = calendar;
		this.players = new ArrayList<>(players);
		this.currentPlayer = players.get(0);
		this.ships = new ArrayList<>(ships);

		instance = this;
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

	public boolean isStarted() {
		return started;
	}

	public void start() {
		// TODO JKA Předělat.
		started = true;
		listenersManager.fireRoundStarted(this);
		currentPlayer = players.get(0);
		ships.forEach(ship -> {
			if (ship.getOwner().equals(currentPlayer)) {
				ship.startTurn();
			}
		});
		listenersManager.fireTurnStarted(this, currentPlayer);
	}

	public boolean isFinished() {
		return calendar.isFinished() || finished;
	}

	public void endTurn() {
		// TODO JKA Předělat.
		final int index = players.indexOf(currentPlayer);
		// TODO JKA Předělat na modulo.
		if (index < players.size() - 1) {
			currentPlayer = players.get(index + 1);
		} else {
			calendar.endRound();
			currentPlayer = players.get(0);
			listenersManager.fireRoundStarted(this);
		}
		ships.forEach(ship -> {
			if (ship.getOwner().equals(currentPlayer)) {
				ship.startTurn();
			}
		});
		listenersManager.fireTurnStarted(this, currentPlayer);
	}

	// TODO JKA Move to unit tests.
	public static void main(String[] args) {
		GameBuilder builder = new GameBuilder();
		Game game = builder
			.setMap(100, 50)
			.setCalendar(1, 3)
			.addPlayer("Player1", true)
			.addShip("Player1", 50, 20, 5)
			.build();
		game.addListener(new GameListener() {
			@Override
			public void roundStarted(RoundStartedEvent event) {
				System.out.println("New round started: " + event + ", " + game.getCalendar());
			}

			@Override
			public void turnStarted(TurnStartedEvent event) {
				System.out.println("New turn started: " + event);
			}

			@Override
			public void shipMoved(ShipMovedEvent event) {
				System.out.println("Ship moved: " + event);
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
