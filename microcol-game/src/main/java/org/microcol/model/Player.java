package org.microcol.model;

import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Player {
	private Model model;

	private final String name;
	private final boolean computer;

	Player(final String name, final boolean computer) {
		this.name = Preconditions.checkNotNull(name);
		this.computer = computer;
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
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
		return model.getShips(this);
	}

	public Map<Location, List<Ship>> getShipsAt() {
		return model.getShipsAt(this);
	}

	public List<Ship> getShipsAt(final Location location) {
		return model.getShipsAt(this, location);
	}

	public List<Ship> getEnemyShips() {
		return model.getEnemyShips(this);
	}

	public Map<Location, List<Ship>> getEnemyShipsAt() {
		return model.getEnemyShipsAt(this);
	}

	public List<Ship> getEnemyShipsAt(final Location location) {
		return model.getEnemyShipsAt(this, location);
	}

	void startTurn() {
		getShips().forEach(ship -> {
			ship.startTurn();
		});
	}

	public void endTurn() {
		model.checkGameActive();
		model.checkCurrentPlayer(this);
		model.endTurn();
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
