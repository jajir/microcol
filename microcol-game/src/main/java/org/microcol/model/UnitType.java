package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public enum UnitType {
	
	COLONIST(ImmutableList.of(Terrain.CONTINENT), 1, true, 0, true),
	FRIGATE(Terrain.UNIT_CAN_SAIL_AT, 4, true, 1, false),
	GALLEON(Terrain.UNIT_CAN_SAIL_AT, 6, false, 5, false);
	

	private final List<Terrain> moveableTerrains;
	private final int speed;
	private final boolean canAttack;
	private final int cargoCapacity;
	private final boolean storable;

	private UnitType(final List<Terrain> moveableTerrains, final int speed, final boolean canAttack, final int cargoCapacity, final boolean storable) {
		this.moveableTerrains = Preconditions.checkNotNull(moveableTerrains);
		this.speed = speed;
		this.canAttack = canAttack;
		this.cargoCapacity = cargoCapacity;
		this.storable = storable;
	}
	
	public static boolean isShip(final UnitType unitType) {
		return unitType == UnitType.FRIGATE || unitType == UnitType.GALLEON;
	}

	public List<Terrain> getMoveableTerrains() {
		return moveableTerrains;
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
		return moveableTerrains.contains(terrain);
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
