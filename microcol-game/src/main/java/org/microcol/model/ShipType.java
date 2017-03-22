package org.microcol.model;

import com.google.common.base.MoreObjects;

public enum ShipType {
	GALLEON(6),
	FRIGATE(4);

	private final int speed;

	private ShipType(final int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("speed", speed)
			.toString();
	}
}
