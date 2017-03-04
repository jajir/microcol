package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Player {
	private Game game;

	private final String name;
	private final boolean computer;

	protected Player(final String name, final boolean computer) {
		this.name = Preconditions.checkNotNull(name);
		this.computer = computer;
	}

	protected void setGame(final Game game) {
		this.game = Preconditions.checkNotNull(game);
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

	public List<Ship> getShips() {
		return game.getShipsStorage().getShips(this);
	}

	public List<Ship> getShipsAt(final Location location) {
		return game.getShipsStorage().getShipsAt(this, location);
	}

	public List<Ship> getEnemyShips() {
		return game.getShipsStorage().getEnemyShips(this);
	}

	protected void startTurn() {
		getShips().forEach(ship -> {
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
