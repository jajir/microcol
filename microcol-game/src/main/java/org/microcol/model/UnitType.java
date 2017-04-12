package org.microcol.model;

import com.google.common.base.MoreObjects;

public enum UnitType {
	COLONIST(Terrain.CONTINENT, 1, true),
	FRIGATE(Terrain.OCEAN, 4, true),
	GALLEON(Terrain.OCEAN, 6, false);

	private final Terrain moveableTerrain;
	private final int speed;
	private final boolean canAttack;

	private UnitType(final Terrain moveableTerrain, final int speed, final boolean canAttack) {
		this.moveableTerrain = moveableTerrain;
		this.speed = speed;
		this.canAttack = canAttack;
	}

	public Terrain getMoveableTerrain() {
		return moveableTerrain;
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
