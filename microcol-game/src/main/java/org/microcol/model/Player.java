package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Player {
	private final String name;
	private final boolean human;

	protected Player(final String name, final boolean human) {
		this.name = Preconditions.checkNotNull(name);
		this.human = human;
	}

	// TODO JKA Předělat
	public Game getGame() {
		return Game.getInstance();
	}

	public String getName() {
		return name;
	}

	public boolean isHuman() {
		return human;
	}

	public boolean isComputer() {
		return !human;
	}

	// TODO JKA getShips()

	// TODO JKA endTurn()

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
			.add("human", human)
			.toString();
	}
}
