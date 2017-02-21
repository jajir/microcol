package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class GameBuilder {
	private Map map;
	private final List<Player> players;
	private final List<Ship> ships;

	public GameBuilder() {
		players = new ArrayList<>();
		ships = new ArrayList<>();
	}

	public GameBuilder setMap(final int maxX, final int maxY) {
		map = new Map(maxX, maxY);

		return this;
	}

	public GameBuilder addPlayer(final String name, final boolean human) {
		// TODO JKA Name must be unique.
		players.add(new Player(name, human));

		return this;
	}

	private Player getPlayer(final String name) {
		// TODO JKA Use streams.
		for (Player player : players) {
			if (player.getName().equals(name)) {
				return player;
			}
		}

		return null;
	}

	public GameBuilder addShip(final String ownerName, final int x, final int y) {
		final Player owner = getPlayer(ownerName);
		ships.add(new Ship(owner, new Location(x, y)));

		return this;
	}

	public Game build() {
		// TODO JKA Implements tests.
		return new Game(map, players, ships);
	}
}
