package org.microcol.model;

public class Player {
	private final String name;
	private final boolean human;

	public Player(final String name, final boolean human) {
		// TODO JKA Add not null test.
		this.name = name;
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
		// TODO JKA Predelat
		StringBuilder builder = new StringBuilder();

		builder.append("Player [name = ");
		builder.append(name);
		builder.append(", human = ");
		builder.append(human);
		builder.append("]");

		return builder.toString();
	}
}
