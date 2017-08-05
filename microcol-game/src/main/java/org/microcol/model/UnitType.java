package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public enum UnitType {
	COLONIST(Terrain.CONTINENT, 1, true, 0, true),
	FRIGATE(Terrain.OCEAN, 4, true, 1, false),
	GALLEON(Terrain.OCEAN, 6, false, 5, false);

	private final Terrain moveableTerrain;
	private final int speed;
	private final boolean canAttack;
	private final int cargoCapacity;
	private final boolean storable;

	private UnitType(final Terrain moveableTerrain, final int speed, final boolean canAttack, final int cargoCapacity, final boolean storable) {
		this.moveableTerrain = moveableTerrain;
		this.speed = speed;
		this.canAttack = canAttack;
		this.cargoCapacity = cargoCapacity;
		this.storable = storable;
	}
	
	public static boolean isShip(final UnitType unitType) {
		return unitType == UnitType.FRIGATE || unitType == UnitType.GALLEON;
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

	public int getCargoCapacity() {
		return cargoCapacity;
	}

	public boolean isStorable() {
		return storable;
	}
	
	public boolean canMoveAtTerrain(final Terrain terrain) {
		Preconditions.checkNotNull(terrain);
		return moveableTerrain == terrain;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name())
			.add("speed", speed)
			.add("canAttack", canAttack)
			.add("cargoCapacity", cargoCapacity)
			.add("storable", storable)
			.toString();
	}
}
