package org.microcol.model;

import com.google.common.base.MoreObjects;

public enum ShipType {
	GALLEON(6, false),
	FRIGATE(4, true);

	private final int speed;
	private final boolean canAttack;

	private ShipType(final int speed, final boolean canAttack) {
		this.speed = speed;
		this.canAttack = canAttack;
	}

	public int getSpeed() {
		return speed;
	}

	public boolean canAttack() {
		return canAttack;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name())
			.add("speed", speed)
			.add("canAttack", canAttack)
			.toString();
	}
}
