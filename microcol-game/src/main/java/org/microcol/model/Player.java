package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Player {
	private final String name;
	private final boolean computer;

	private Game game;
	private ImmutableList<Ship> ships;

	protected Player(final String name, final boolean computer) {
		this.name = Preconditions.checkNotNull(name);
		this.computer = computer;
	}

	public String getName() {
		return name;
	}

	public boolean isComputer() {
		return computer;
	}

	public boolean isHuman() {
		return !computer;
	}

	protected void startGame(final Game game) {
		this.game = game;

		// TODO JKA Use streams
		List<Ship> list = new ArrayList<>();
		game.getShips().forEach(ship -> {
			if (ship.getOwner().equals(this)) {
				list.add(ship);
			}
		});
		ships = ImmutableList.copyOf(list);

		ships.forEach(ship -> {
			ship.startGame(game);
		});
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

	protected void startTurn() {
		ships.forEach(ship -> {
			ship.startTurn();
		});
	}

	public void endTurn() {
		Preconditions.checkState(game.isActive(), "Game must be active.");
		Preconditions.checkState(game.getCurrentPlayer().equals(this), "This player (%s) is not current player (%s).", this, game.getCurrentPlayer());

		game.endTurn();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (!(object instanceof Player)) {
			return false;
		}

		Player player = (Player) object;

		return name.equals(player.name);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name)
			.add("computer", computer)
			.toString();
	}
}
