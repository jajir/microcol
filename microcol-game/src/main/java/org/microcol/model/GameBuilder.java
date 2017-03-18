package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class GameBuilder {
	private final List<Player> players;
	private final List<Ship> ships;

	private Calendar calendar;
	private World world;

	public GameBuilder() {
		players = new ArrayList<>();
		ships = new ArrayList<>();
	}

	public GameBuilder setCalendar(final int startYear, final int endYear) {
		calendar = new Calendar(startYear, endYear);

		return this;
	}

	public GameBuilder setWorld(final int maxX, final int maxY) {
		world = new World(maxX, maxY);

		return this;
	}

	public GameBuilder setWorld(final String fileName) {
		world = new World(fileName);

		return this;
	}

	public GameBuilder addPlayer(final String name, final boolean computer) {
		Preconditions.checkArgument(getPlayer(name) == null, "Player name must be unique: %s", name);

		players.add(new Player(name, computer));

		return this;
	}

	private Player getPlayer(final String name) {
		// TODO JKA Use streams
		for (Player player : players) {
			if (player.getName().equals(name)) {
				return player;
			}
		}

		return null;
	}

	public GameBuilder addShip(final String ownerName, final int maxMoves, final int x, final int y) {
		// TODO JKA Check location - there is not any enemy ship
		ships.add(new Ship(getPlayer(ownerName), maxMoves, Location.of(x, y)));

		return this;
	}

	public Game build() {
		Preconditions.checkState(!players.isEmpty(), "Game must have at least one player.");

		return new Game(calendar, world, players, ships);
	}
}
