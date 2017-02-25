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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name)
			.add("human", human)
			.toString();
	}
}
