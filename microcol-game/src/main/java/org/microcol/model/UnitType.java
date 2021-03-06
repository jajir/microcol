package org.microcol.model;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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
public final class UnitType {
    
    /**
     * Default production of all goods in default case.
     */
    private static final float DEFAULT_PRODUCTION_MODIFIER = 1F;

    /**
     * Default frigate price in Europe port.
     */
    private static final int DEFAULT_FRIGATE_EUROPE_PRICE = 4_000;

    /**
     * Default frigate speed.
     */
    private static final int DEFAULT_FRIGATE_SPEED = 4;

    /**
     * Default frigate price in Europe port.
     */
    private static final int DEFAULT_GALLEON_EUROPE_PRICE = 5_000;

    /**
     * Default galleon cargo capacity.
     */
    private static final int DEFAULT_GALLEON_CARGO_CAPACITY = 5;

    /**
     * Default galleon speed.
     */
    private static final int DEFAULT_GALLEON_SPEED = 6;

    /**
     * Default master blacksmith price in Europe port.
     */
    private static final int DEFAULT_FREE_COLONIST_EUROPE_PRICE = 1_400;

    /**
     * Default ore miner price in Europe port.
     */
    private static final int DEFAULT_ORE_MINER_EUROPE_PRICE = 600;

    /**
     * Default master blacksmith price in Europe port.
     */
    private static final int DEFAULT_MASTER_BLACKSMITH_EUROPE_PRICE = 2_200;

    /**
     * There is no unit that could be attacked.
     */
    private static final Predicate<UnitType> UNIT_TYPE_CANT_ATTACK = unitType -> false;
    
    /**
     * When unit is in colony that this define number of food ate per turn.
     */
    private final static int ATE_FOOD_PER_TURN = 2;
  
    private final String name;
    private final List<TerrainType> moveableTerrains;
    private final int speed;
    private final int cargoCapacity;
    private final boolean storable;
    private final int europePrice;
    private final BiFunction<Model, Colony, Boolean> canByBuildInColony;

    /**
     * Here is good type in which production is unit exceptional. When it's
     * <code>null</code> than unit is not expert on producing any type of goods.
     */
    private final GoodsType expertInProducing;

    /**
     * Production multiplier on basic good production.
     */
    private final float expertProductionModifier;

    /**
     * Predicate allows to find unit types that could be attacked by this unit
     * type.
     */
    private final Predicate<UnitType> attackableUnitTypeFilter;

    /**
     * it's <code>true</code> when unit could plow field.
     */
    private final boolean canPlowField;
    
    /**
     * it's <code>true</code> when unit could build road.
     */
    private final boolean canBuildRoad;
    
    /**
     * it's <code>true</code> when unit could cut down trees.
     */
    private final boolean canCutTrees;
    
    /**
     * How many tools is required to build unit.
     */
    private final Integer requiredTools;
    
    /**
     * How many hammers is required to build unit.
     */
    private final Integer requiredHammers;

    public static final UnitType COLONIST = UnitType.make()
            .setName("COLONIST")
            .setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
            .setSpeed(1)
            .setAttackableUnitTypeFilter(unitType -> !unitType.isShip())
            .setCargoCapacity(0)
            .setStorable(true)
            .setEuropePrice(DEFAULT_FREE_COLONIST_EUROPE_PRICE)
            .setCanBuildRoad(true)
            .setCanCutTrees(true)
            .setCanPlowField(true)
            .build();

    public static final UnitType EXPERT_ORE_MINER = UnitType.make()
            .setName("EXPERT_ORE_MINER")
            .setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
            .setSpeed(1)
            .setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
            .setCargoCapacity(0)
            .setStorable(true)
            .setEuropePrice(DEFAULT_ORE_MINER_EUROPE_PRICE)
            .setExpertise(GoodsType.ORE, 2.0F)
            .setCanBuildRoad(true)
            .setCanCutTrees(true)
            .setCanPlowField(true)
            .build();

    public static final UnitType WAGON = UnitType.make()
            .setName("WAGON")
            .setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
            .setSpeed(1)
            .setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
            .setCargoCapacity(2)
            .setStorable(false)
            .setEuropePrice(0)
            .setCanBuildRoad(false)
            .setCanCutTrees(false)
            .setCanPlowField(false)
            .setRequiredTools(40)
            .setRequiredHammers(0)
            .setCanByBuildInColony((model, colony) -> {
                return colony.wasConstructionBuilded(ConstructionType.LUMBER_MILL);
            })
            .build();

    public static final UnitType MASTER_BLACKSMITH = UnitType.make()
            .setName("MASTER_BLACKSMITH")
            .setMoveableTerrains(TerrainType.UNIT_CAN_WALK_AT)
            .setSpeed(1)
            .setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
            .setCargoCapacity(0)
            .setStorable(true)
            .setEuropePrice(DEFAULT_MASTER_BLACKSMITH_EUROPE_PRICE)
            .setExpertise(GoodsType.TOOLS, 2.0F)
            .setCanBuildRoad(true)
            .setCanCutTrees(true)
            .setCanPlowField(true)
            .build();

    public static final UnitType FRIGATE = UnitType.make()
            .setName("FRIGATE")
            .setMoveableTerrains(TerrainType.UNIT_CAN_SAIL_AT)
            .setSpeed(DEFAULT_FRIGATE_SPEED)
            .setAttackableUnitTypeFilter(unitType -> unitType.isShip())
            .setCargoCapacity(1)
            .setStorable(false)
            .setEuropePrice(DEFAULT_FRIGATE_EUROPE_PRICE)
            .setRequiredTools(100)
            .setRequiredHammers(100)
            .setCanByBuildInColony((model, colony) -> {
                return colony.wasConstructionBuilded(ConstructionType.DRYDOCK);
            })
            .build();

    public static final UnitType GALLEON = UnitType.make()
            .setName("GALLEON")
            .setMoveableTerrains(TerrainType.UNIT_CAN_SAIL_AT)
            .setSpeed(DEFAULT_GALLEON_SPEED)
            .setAttackableUnitTypeFilter(UNIT_TYPE_CANT_ATTACK)
            .setCargoCapacity(DEFAULT_GALLEON_CARGO_CAPACITY)
            .setStorable(false)
            .setEuropePrice(DEFAULT_GALLEON_EUROPE_PRICE)
            .setRequiredTools(100)
            .setRequiredHammers(100)
            .setCanByBuildInColony((model, colony) -> {
                return colony.wasConstructionBuilded(ConstructionType.DRYDOCK);
            })
            .build();

    /**
     * List of all available unit types.
     */
    public static final List<UnitType> UNIT_TYPES = ImmutableList.of(COLONIST, FRIGATE, GALLEON, WAGON);

    private static final Map<String, UnitType> UNIT_TYPES_BY_NAME = UNIT_TYPES.stream()
            .collect(ImmutableMap.toImmutableMap(UnitType::name, Function.identity()));

    /**
     * Unit type builder.
     */
    private static final class UnitTypeBuilder {

        private String name;
        private List<TerrainType> moveableTerrains;
        private int speed;
        private int cargoCapacity;
        private Predicate<UnitType> attackableUnitTypeFilter;
        private boolean storable;
        private int europePrice;
        private GoodsType expertInProducing;
        private float expertProductionModifier;
        private boolean canPlowField = false;
        private boolean canBuildRoad = false;
        private boolean canCutTrees = false;
        private Integer requiredTools = null;
        private Integer requiredHammers = null;
        private BiFunction<Model, Colony, Boolean> canByBuildInColony = null;
        
        /**
         * Method that build final unit type and return it.
         *
         * @return Return final unit type definition.
         */
        UnitType build() {
            return new UnitType(name, moveableTerrains, speed, attackableUnitTypeFilter,
                    cargoCapacity, storable, europePrice, expertInProducing,
                    expertProductionModifier, canPlowField, canBuildRoad, canCutTrees,
                    requiredTools, requiredHammers, canByBuildInColony);
        }

        /**
         * Allows to set unit type's name.
         *
         * @param unitTypeName
         *            required unit types's name
         * @return return unit type builder object
         */
        UnitTypeBuilder setName(final String unitTypeName) {
            this.name = unitTypeName;
            return this;
        }

        /**
         * Allows to set list of terrain types on which unit could move.
         *
         * @param moveableTerrainList
         *            required list of terrain types
         * @return return unit type builder object
         */
        UnitTypeBuilder setMoveableTerrains(final List<TerrainType> moveableTerrainList) {
            this.moveableTerrains = moveableTerrainList;
            return this;
        }

        /**
         * Allows to set unit type speed.
         *
         * @param newSpeed
         *            required speed
         * @return return unit type builder object
         */
        UnitTypeBuilder setSpeed(final int newSpeed) {
            this.speed = newSpeed;
            return this;
        }

        /**
         * Allows to filter to find attackable unit types.
         *
         * @param filter
         *            required filter definition
         * @return return unit type builder object
         */
        UnitTypeBuilder setAttackableUnitTypeFilter(
                final Predicate<UnitType> filter) {
            this.attackableUnitTypeFilter = filter;
            return this;
        }

        /**
         * Allows to set cargo capacity.
         *
         * @param newCargoCapacity
         *            required cargo capacity.
         * @return return unit type builder object
         */
        UnitTypeBuilder setCargoCapacity(final int newCargoCapacity) {
            this.cargoCapacity = newCargoCapacity;
            return this;
        }

        /**
         * Allows to set if unit could be stored in cargo.
         *
         * @param defaultStorable
         *            required information if unit could be place to store.
         * @return return unit type builder object
         */
        UnitTypeBuilder setStorable(final boolean defaultStorable) {
            this.storable = defaultStorable;
            return this;
        }

        /**
         * Allows to set price in Europe.
         *
         * @param defaultEuropePrice
         *            required price in Europe
         * @return return unit type builder object
         */
        UnitTypeBuilder setEuropePrice(final int defaultEuropePrice) {
            this.europePrice = defaultEuropePrice;
            return this;
        }

        /**
         * Allows to set if unit type define expert unit and if there is any
         * production modifiers.
         *
         * @param newExpertInProducing
         *            Required if unit is expert in production
         * @param newExpertProductionModifier
         *            Required if there is some production modifiers.
         * @return return unit type builder object
         */
        UnitTypeBuilder setExpertise(final GoodsType newExpertInProducing,
                final float newExpertProductionModifier) {
            this.expertInProducing = newExpertInProducing;
            this.expertProductionModifier = newExpertProductionModifier;
            return this;
        }

        /**
         * @param canPlowField
         *            the canPlowField to set
         */
        UnitTypeBuilder setCanPlowField(final boolean canPlowField) {
            this.canPlowField = canPlowField;
            return this;
        }

        /**
         * @param canBuildRoad
         *            the canBuildRoad to set
         */
        UnitTypeBuilder setCanBuildRoad(final boolean canBuildRoad) {
            this.canBuildRoad = canBuildRoad;
            return this;
        }

        /**
         * @param canCutTrees
         *            the canCutTrees to set
         */
        UnitTypeBuilder setCanCutTrees(final boolean canCutTrees) {
            this.canCutTrees = canCutTrees;
            return this;
        }

        /**
         * @param requiredTools
         *            the requiredTools to set
         */
        UnitTypeBuilder setRequiredTools(final Integer requiredTools) {
            this.requiredTools = requiredTools;
            return this;
        }

        /**
         * @param requiredHammers
         *            the requiredHammers to set
         */
        UnitTypeBuilder setRequiredHammers(final Integer requiredHammers) {
            this.requiredHammers = requiredHammers;
            return this;
        }

        /**
         * @param canByBuildInColony the canByBuildInColony to set
         */
        UnitTypeBuilder setCanByBuildInColony(BiFunction<Model, Colony, Boolean> canByBuildInColony) {
            this.canByBuildInColony = canByBuildInColony;
            return this;
        }

    }

    /**
     * Unit type constructor. Should be called just from this class.
     *
     * @param name
     *            required name
     * @param moveableTerrains
     *            required list of terrain that unit could move on
     * @param speed
     *            required default speed
     * @param attackableUnitTypeFilter
     *            required function that could select attackable unit type
     * @param cargoCapacity
     *            required default cargo capacity
     * @param storable
     *            required if unit could be placed in cargo
     * @param europePrice
     *            required default price in Europe port
     * @param expertInProducing
     *            required unit's expertise
     * @param expertProductionModifier
     *            required production modifiers
     * @param canPlowField
     *            required if unit could plow field
     * @param canBuildRoad
     *            required if unit could build road
     * @param canCutTrees
     *            required if unit could cut down trees
     * @param requiredTools
     *            optional number of tools required to build unit
     * @param requiredHammers
     *            optional number of hammers required to build unit
     * @param canByBuildInColony
     *            optional function defining rules which have to be met to be
     *            able to build unit in colony. When it's <code>null</code> than
     *            it's never possible to build unit in colony.
     */
    UnitType(final String name, final List<TerrainType> moveableTerrains, final int speed,
            final Predicate<UnitType> attackableUnitTypeFilter, final int cargoCapacity,
            final boolean storable, final int europePrice, final GoodsType expertInProducing,
            final float expertProductionModifier, final boolean canPlowField,
            final boolean canBuildRoad, final boolean canCutTrees, final Integer requiredTools,
            final Integer requiredHammers, final BiFunction<Model, Colony, Boolean> canByBuildInColony) {
        this.name = Preconditions.checkNotNull(name);
        this.moveableTerrains = Preconditions.checkNotNull(moveableTerrains);
        this.speed = speed;
        this.attackableUnitTypeFilter = Preconditions.checkNotNull(attackableUnitTypeFilter);
        this.cargoCapacity = cargoCapacity;
        this.storable = storable;
        this.europePrice = europePrice;
        this.expertInProducing = expertInProducing;
        this.expertProductionModifier = expertProductionModifier;
        this.canPlowField = canPlowField;
        this.canBuildRoad = canBuildRoad;
        this.canCutTrees = canCutTrees;
        this.requiredTools = requiredTools;
        this.requiredHammers = requiredHammers;
        this.canByBuildInColony = canByBuildInColony;
    }

    /**
     * Static factory for unit type builder.
     *
     * @return Return unit type builder.
     */
    private static UnitTypeBuilder make() {
        return new UnitTypeBuilder();
    }

    /**
     * Unit type name.
     *
     * @return Return unit type name.
     */
    public String name() {
        return name;
    }

    /**
     * Same function as in enum type. It convert string type to class.
     *
     * @param unitTypeName
     *            required unit type as string
     * @return Return corresponding class
     * @throws IllegalArgumentException
     *             Exception is thrown when give string doesn't match any class.
     */
    public static UnitType valueOf(final String unitTypeName) {
        if (UNIT_TYPES_BY_NAME.get(unitTypeName) == null) {
            throw new IllegalArgumentException(
                    String.format("There is no such UnitType (%s)", unitTypeName));
        } else {
            return UNIT_TYPES_BY_NAME.get(unitTypeName);
        }
    }

    /**
     * Get info if this unit is ship.
     *
     * @return Return <code>true</code> if this unit type is some kind of ship
     *         otherwise return <code>false</code>.
     */
    public boolean isShip() {
        return this == UnitType.FRIGATE || this == UnitType.GALLEON;
    }

    /**
     * Get list of terrain types on which unit could move.
     *
     * @return List of unit type.
     */
    public List<TerrainType> getMoveableTerrains() {
        return moveableTerrains;
    }
    
    /**
     * How many food eat unit when is located in colony per turn.
     *
     * @return number of ate food
     */
    public int getAteFoodPerTurn() {
        return ATE_FOOD_PER_TURN;
    }

    /**
     * Get unit type speed. It's used as maximum number of action points per
     * turn.
     *
     * @return Unit type's speed.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Get info if unit could attack any unit.
     *
     * @return Return <code>true</code> if unit could attack any other unit
     *         otherwise return <code>false</code>.
     */
    public boolean canAttack() {
        return !getAttackableUnitType().isEmpty();
    }

    /**
     * Get list of unit types that could be attacked by this unit type. When
     * list is empty than there is no unit that could be attacked.
     *
     * @return List of unit type that could be attacked.
     */
    public List<UnitType> getAttackableUnitType() {
        return UNIT_TYPES.stream().filter(attackableUnitTypeFilter)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Get cargo capacity.
     *
     * @return Return cargo capacity.
     */
    public int getCargoCapacity() {
        return cargoCapacity;
    }

    /**
     * Return information whether unit could be stored in cargo of some other
     * unit.
     *
     * @return Return <code>true</code> if unit could be stored in cargo
     *         otherwise return <code>false</code>.
     */
    public boolean isStorable() {
        return storable;
    }

    /**
     * Return information whether unit could move at given terrain.
     *
     * @param terrain
     *            required terrain
     * @return Return <code>true</code> when unit type coud move at given
     *         terrain otherwise return <code>false</code>.
     */
    public boolean canMoveAtTerrain(final TerrainType terrain) {
        Preconditions.checkNotNull(terrain);
        return moveableTerrains.contains(terrain);
    }

    /**
     * Get info if unit could hold cargo.
     *
     * @return Return <code>true</code> when unit could hold cargo otherwise
     *         return <code>false</code>.
     */
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
     * @param goodsType
     *            required good type
     * @return goods type related production modifier
     */
    public float getProductionModifier(final GoodsType goodsType) {
        if (goodsType.equals(expertInProducing)) {
            return expertProductionModifier;
        } else {
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
        return MoreObjects.toStringHelper(this).add("name", name()).add("speed", speed)
                .add("canAttack", canAttack()).add("cargoCapacity", cargoCapacity)
                .add("storable", storable).toString();
    }

    /**
     * Default item price in Europe port.
     *
     * @return return price in Europe
     */
    public int getEuropePrice() {
        return europePrice;
    }

    /**
     * @return the canPlowField
     */
    public boolean isCanPlowField() {
        return canPlowField;
    }

    /**
     * @return the canBuildRoad
     */
    public boolean isCanBuildRoad() {
        return canBuildRoad;
    }

    /**
     * @return the canCutTrees
     */
    public boolean isCanCutTrees() {
        return canCutTrees;
    }

    /**
     * @return the requiredTools
     */
    public Integer getRequiredTools() {
        return requiredTools;
    }

    /**
     * @return the requiredHammers
     */
    public Integer getRequiredHammers() {
        return requiredHammers;
    }
    
    /**
     * Get information if unit could be build in colony lumber mill.
     *
     * @return return <code>true</code> when unit could be build in colony
     *         lumber mill otherwise return <code>false</code>
     */
    public boolean canBeBuildInColony() {
        return requiredTools != null && requiredHammers != null;
    }

    /**
     * @return the canByBuildInColony
     */
    public BiFunction<Model, Colony, Boolean> getCanByBuildInColony() {
        if (canByBuildInColony == null) {
            return (model, colony) -> false;
        } else {
            return canByBuildInColony;
        }
    }
 
}
