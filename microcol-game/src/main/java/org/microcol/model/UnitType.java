package org.microcol.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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
	
	/**
	 * Default production of all goods in default case.
	 */
	private final static float DEFAULT_PRODUCTION_MODIFIER = 1F;
	
	/**
	 * There is no unit that could be attacked.
	 */
	private final static Predicate<UnitType> UNIT_TYPE_CANT_ATTACK = unitType -> false;
	
	private final String name;
	private final List<TerrainType> moveableTerrains;
	private final int speed;
	private final int cargoCapacity;
	private final boolean storable;
	private final int europePrice;
	
	/**
	 * Here is good type in which production is unit exceptional. When it's
	 * <code>null</code> than unit is not expert on producing any type of goods.
	 */
	private final GoodType expertInProducing;
	
	/**
	 * Production multiplier on basic good production.
	 */
	private final float expertProductionModifier;
	
	/**
	 * Predicate allows to find unit types that could be attacked by this unit
	 * type.
	 */
	private final Predicate<UnitType> attackableUnitTypeFilter;
	
	public final static UnitType COLONIST = UnitType.make()
			.setName("COLONIST")
			.setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
			.setSpeed(1)
			.setAttackableUnitTypeFilter(unitType -> !unitType.isShip())
			.setCargoCapacity(0)
			.setStorable(true)
			.setEuropePrice(1400)
			.build();
	
	public final static UnitType EXPERT_ORE_MINER = UnitType.make()
			.setName("EXPERT_ORE_MINER")
			.setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
			.setSpeed(1)
			.setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
			.setCargoCapacity(0)
			.setStorable(true)
			.setEuropePrice(600)
			.setExpertise(GoodType.ORE, 2.0F)
			.build();
	
	public final static UnitType MASTER_BLACKSMITH = UnitType.make()
			.setName("MASTER_BLACKSMITH")
			.setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
			.setSpeed(1)
			.setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
			.setCargoCapacity(0)
			.setStorable(true)
			.setEuropePrice(1100)
			.setExpertise(GoodType.TOOLS, 2.0F)
			.build();
	
	public final static UnitType FRIGATE = UnitType.make()
			.setName("FRIGATE")
			.setMoveableTerrains(TerrainType.UNIT_CAN_SAIL_AT)
			.setSpeed(4)
			.setAttackableUnitTypeFilter(unitType -> unitType.isShip())
			.setCargoCapacity(1)
			.setStorable(false)
			.setEuropePrice(4000)
			.build();
	
	public final static UnitType GALLEON = UnitType.make()
			.setName("GALLEON")
			.setMoveableTerrains(TerrainType.UNIT_CAN_SAIL_AT)
			.setSpeed(6)
			.setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
			.setCargoCapacity(5)
			.setStorable(false)
			.setEuropePrice(5000)
			.build();
	
	public final static List<UnitType> UNIT_TYPES = ImmutableList.of(COLONIST, FRIGATE, GALLEON);

	private final static Map<String, UnitType> UNIT_TYPES_BY_NAME = UNIT_TYPES.stream()
			.collect(ImmutableMap.toImmutableMap(UnitType::name, Function.identity()));
	
	private static class UnitTypeBuilder{

		private String name;
		private List<TerrainType> moveableTerrains;
		private int speed;
		private int cargoCapacity;
		private Predicate<UnitType> attackableUnitTypeFilter;
		private boolean storable;
		private int europePrice;
		private GoodType expertInProducing;
		private float expertProductionModifier;
		
		UnitType build(){
			return new UnitType(name, moveableTerrains, speed, attackableUnitTypeFilter, cargoCapacity, storable,
					europePrice, expertInProducing, expertProductionModifier);
		}

		UnitTypeBuilder setName(final String name) {
			this.name = name;
			return this;
		}

		UnitTypeBuilder setMoveableTerrains(final List<TerrainType> moveableTerrains) {
			this.moveableTerrains = moveableTerrains;
			return this;
		}

		UnitTypeBuilder setSpeed(final int speed) {
			this.speed = speed;
			return this;
		}

		UnitTypeBuilder setAttackableUnitTypeFilter(final Predicate<UnitType> attackableUnitTypeFilter) {
			this.attackableUnitTypeFilter = attackableUnitTypeFilter;
			return this;
		}

		UnitTypeBuilder setCargoCapacity(final int cargoCapacity) {
			this.cargoCapacity = cargoCapacity;
			return this;
		}

		UnitTypeBuilder setStorable(final boolean storable) {
			this.storable = storable;
			return this;
		}

		UnitTypeBuilder setEuropePrice(final int europePrice) {
			this.europePrice = europePrice;
			return this;
		}

		UnitTypeBuilder setExpertise(final GoodType expertInProducing, final float expertProductionModifier) {
			this.expertInProducing = expertInProducing;
			this.expertProductionModifier = expertProductionModifier;
			return this;
		}
		
	}

	UnitType(final String name, final List<TerrainType> moveableTerrains, final int speed,
			final Predicate<UnitType> attackableUnitTypeFilter, final int cargoCapacity, final boolean storable, final int europePrice,
			final GoodType expertInProducing, final float expertProductionModifier) {
		this.name = Preconditions.checkNotNull(name);
		this.moveableTerrains = Preconditions.checkNotNull(moveableTerrains);
		this.speed = speed;
		this.attackableUnitTypeFilter = Preconditions.checkNotNull(attackableUnitTypeFilter);
		this.cargoCapacity = cargoCapacity;
		this.storable = storable;
		this.europePrice = europePrice;
		this.expertInProducing = expertInProducing;
		this.expertProductionModifier = expertProductionModifier;
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

	public boolean isShip() {
		return this == UnitType.FRIGATE || this == UnitType.GALLEON;
	}
	
	public List<TerrainType> getMoveableTerrains() {
		return moveableTerrains;
	}

	public int getSpeed() {
		return speed;
	}

	public boolean canAttack() {
		return !getAttackableUnitType().isEmpty();
	}
	
	public List<UnitType> getAttackableUnitType() {
		return UNIT_TYPES.stream().filter(attackableUnitTypeFilter).collect(ImmutableList.toImmutableList());
	}

	public int getCargoCapacity() {
		return cargoCapacity;
	}

	public boolean isStorable() {
		return storable;
	}
	
	public boolean canMoveAtTerrain(final TerrainType terrain) {
		Preconditions.checkNotNull(terrain);
		return moveableTerrains.contains(terrain);
	}
	
	public boolean canHoldCargo() {
		return cargoCapacity > 0;
	}

	/**
	 * Can unit type build new colony.
	 *
	 * @return Return <code>true</code> when unit type could build colony. When
	 *         return <code>false</code> unit type can't build colony.
	 */
	public boolean canBuildColony() {
		return !isShip() && !canHoldCargo();
	}
	
	/**
	 * How much more of given goods type could unit produce.
	 *
	 * @param goodType
	 *            required good type
	 * @return goods type related production modifier
	 */
	public float getProductionModifier(final GoodType goodType){
		if(goodType.equals(expertInProducing)){
			return expertProductionModifier;
		}else{
			return DEFAULT_PRODUCTION_MODIFIER;
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof UnitType) {
			final UnitType other = (UnitType) obj;
			return name.equals(other.name);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name())
			.add("speed", speed)
			.add("canAttack", canAttack())
			.add("cargoCapacity", cargoCapacity)
			.add("storable", storable)
			.toString();
	}

	public int getEuropePrice() {
		return europePrice;
	}
}
