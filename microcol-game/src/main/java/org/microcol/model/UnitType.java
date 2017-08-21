package org.microcol.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This is class defining all in game available units. Enumeration doesn't
 * support nice builders. Because of that this class is not enumerations.
 * 
 * <p>
 * It's not necessary to implements equals because of number of instances is
 * defined here in class. Outside of this class other instances can't be
 * created.
 * </p>
 */
public class UnitType {
	
	public final static UnitType COLONIST = UnitType.make()
			.setName("COLONIST")
			.setMoveableTerrains(ImmutableList.of(Terrain.CONTINENT))
			.setSpeed(1)
			.setCanAttack(true)
			.setCargoCapacity(0)
			.setStorable(true)
			.setEuropePrice(1400)
			.build();
	
	public final static UnitType FRIGATE = UnitType.make()
			.setName("FRIGATE")
			.setMoveableTerrains(Terrain.UNIT_CAN_SAIL_AT)
			.setSpeed(4)
			.setCanAttack(true)
			.setCargoCapacity(1)
			.setStorable(false)
			.setEuropePrice(4000)
			.build();
	
	public final static UnitType GALLEON = UnitType.make()
			.setName("GALLEON")
			.setMoveableTerrains(Terrain.UNIT_CAN_SAIL_AT)
			.setSpeed(6)
			.setCanAttack(false)
			.setCargoCapacity(5)
			.setStorable(false)
			.setEuropePrice(5000)
			.build();
	
	public final static List<UnitType> UNIT_TYPES = ImmutableList.of(COLONIST, FRIGATE, GALLEON);

	private final static Map<String, UnitType> UNIT_TYPES_BY_NAME = UNIT_TYPES.stream()
			.collect(ImmutableMap.toImmutableMap(UnitType::name, Function.identity()));
	
	private final String name;
	private final List<Terrain> moveableTerrains;
	private final int speed;
	private final boolean canAttack;
	private final int cargoCapacity;
	private final boolean storable;
	private final int europePrice;
	
	private static class UnitTypeBuilder{

		private String name;
		private List<Terrain> moveableTerrains;
		private int speed;
		private boolean canAttack;
		private int cargoCapacity;
		private boolean storable;
		private int europePrice;
		
		private UnitType build(){
			return new UnitType(name, moveableTerrains, speed, canAttack, cargoCapacity, storable, europePrice);
		}

		private UnitTypeBuilder setName(final String name) {
			this.name = name;
			return this;
		}

		private UnitTypeBuilder setMoveableTerrains(final List<Terrain> moveableTerrains) {
			this.moveableTerrains = moveableTerrains;
			return this;
		}

		private UnitTypeBuilder setSpeed(final int speed) {
			this.speed = speed;
			return this;
		}

		private UnitTypeBuilder setCanAttack(final boolean canAttack) {
			this.canAttack = canAttack;
			return this;
		}

		private UnitTypeBuilder setCargoCapacity(final int cargoCapacity) {
			this.cargoCapacity = cargoCapacity;
			return this;
		}

		private UnitTypeBuilder setStorable(final boolean storable) {
			this.storable = storable;
			return this;
		}

		private UnitTypeBuilder setEuropePrice(int europePrice) {
			this.europePrice = europePrice;
			return this;
		}
		
	}

	private UnitType(final String name, final List<Terrain> moveableTerrains, final int speed, final boolean canAttack, final int cargoCapacity, final boolean storable, final int europePrice) {
		this.name = Preconditions.checkNotNull(name);
		this.moveableTerrains = Preconditions.checkNotNull(moveableTerrains);
		this.speed = speed;
		this.canAttack = canAttack;
		this.cargoCapacity = cargoCapacity;
		this.storable = storable;
		this.europePrice = europePrice;
	}
	
	private static UnitTypeBuilder make(){
		return new UnitTypeBuilder();
	}
	
	public String name(){
		return name;
	}
	
	public static UnitType valueOf(final String unitTypeName){
		if (UNIT_TYPES_BY_NAME.get(unitTypeName) == null) {
			throw new IllegalArgumentException(String.format("There is no such UnitType (%s)", unitTypeName));
		} else {
			return UNIT_TYPES_BY_NAME.get(unitTypeName);
		}
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

	public int getEuropePrice() {
		return europePrice;
	}
}
