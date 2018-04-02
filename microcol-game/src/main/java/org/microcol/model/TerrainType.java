package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class TerrainType {

    final static int NO_VALUE = -1;

    private final String name;

    /**
     * Code is used to identified terrain type in save file.
     */
    private final String code;

    private final boolean canHaveTree;

    private final boolean isSea;

    private final int moveCost;

    private final int moveCostWithTree;

    private final int defenseBonus;

    private final int defenseBonusWithTree;

    private final List<Production> productions;

    public final static TerrainType GRASSLAND = TerrainType.make().setName("GRASSLAND").setCode("g")
            .setCanHaveTree(true).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.CORN)
            .setWithTrees(3).setWithoutTrees(4).build().setProduction(GoodType.SUGAR)
            .setWithoutTrees(1).build().setProduction(GoodType.TOBACCO).setWithTrees(1)
            .setWithoutTrees(3).build().setProduction(GoodType.COTTON).setWithTrees(1)
            .setWithoutTrees(2).build().setProduction(GoodType.FUR).setWithTrees(2).build()
            .setProduction(GoodType.LUMBER).setWithTrees(6).build().build();

    public final static TerrainType PRAIRIE = TerrainType.make().setName("PRAIRIE").setCode("p")
            .setCanHaveTree(true).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.CORN)
            .setWithTrees(3).setWithoutTrees(4).build().setProduction(GoodType.TOBACCO)
            .setWithTrees(1).setWithoutTrees(2).build().setProduction(GoodType.COTTON)
            .setWithTrees(1).setWithoutTrees(3).build().setProduction(GoodType.FUR).setWithTrees(2)
            .build().setProduction(GoodType.LUMBER).setWithTrees(4).build().build();

    public final static TerrainType SAVANNAH = TerrainType.make().setName("SAVANNAH").setCode("s")
            .setCanHaveTree(true).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.CORN)
            .setWithTrees(3).setWithoutTrees(4).build().setProduction(GoodType.SUGAR)
            .setWithoutTrees(3).build().setProduction(GoodType.TOBACCO).setWithTrees(1)
            .setWithoutTrees(3).build().setProduction(GoodType.COTTON).setWithTrees(1)
            .setWithoutTrees(3).build().setProduction(GoodType.FUR).setWithTrees(2).build()
            .setProduction(GoodType.LUMBER).setWithTrees(6).build().build();

    public final static TerrainType SWAMP = TerrainType.make().setName("SWAMP").setCode("w")
            .setCanHaveTree(true).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.CORN)
            .setWithTrees(2).setWithoutTrees(3).build().setProduction(GoodType.SUGAR)
            .setWithTrees(2).setWithoutTrees(3).build().setProduction(GoodType.TOBACCO)
            .setWithTrees(0).setWithoutTrees(1).build().setProduction(GoodType.LUMBER)
            .setWithTrees(4).build().build();

    public final static TerrainType DESERT = TerrainType.make().setName("DESERT").setCode("d")
            .setCanHaveTree(true).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.CORN)
            .setWithTrees(2).setWithoutTrees(2).build().setProduction(GoodType.TOBACCO)
            .setWithTrees(0).setWithoutTrees(1).build().setProduction(GoodType.COTTON)
            .setWithTrees(1).setWithoutTrees(1).build().setProduction(GoodType.FUR).setWithTrees(2)
            .build().setProduction(GoodType.LUMBER).setWithTrees(2).build()
            .setProduction(GoodType.ORE).setWithTrees(1).setWithoutTrees(2).build().build();

    public final static TerrainType TUNDRA = TerrainType.make().setName("TUNDRA").setCode("t")
            .setCanHaveTree(false).setSea(false).setMoveCost(2).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(0).setProduction(GoodType.CORN)
            .setWithTrees(2).setWithoutTrees(3).build().setProduction(GoodType.FUR).setWithTrees(3)
            .build().setProduction(GoodType.LUMBER).setWithTrees(4).build()
            .setProduction(GoodType.ORE).setWithTrees(1).setWithoutTrees(2).build().build();

    public final static TerrainType ARCTIC = TerrainType.make().setName("ARCTIC").setCode("a")
            .setCanHaveTree(false).setSea(false).setMoveCost(2).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(0).build();

    public final static TerrainType HILL = TerrainType.make().setName("HILL").setCode("h")
            .setCanHaveTree(false).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.CORN)
            .setWithoutTrees(2).build().setProduction(GoodType.ORE).setWithoutTrees(4).build()
            .build();

    public final static TerrainType MOUNTAIN = TerrainType.make().setName("MOUNTAIN").setCode("m")
            .setCanHaveTree(false).setSea(false).setMoveCost(1).setMoveCostWithTree(2)
            .setDefenseBonus(0).setDefenseBonusWithTree(50).setProduction(GoodType.ORE)
            .setWithoutTrees(3).build().setProduction(GoodType.SILVER).setWithoutTrees(1).build()
            .build();

    public final static TerrainType HIGH_SEA = TerrainType.make().setName("HIGH_SEA").setCode("~")
            .setCanHaveTree(false).setSea(true).setMoveCost(1).setMoveCostWithTree(1)
            .setDefenseBonus(0).setDefenseBonusWithTree(0).setProduction(GoodType.CORN)
            .setWithoutTrees(3).build().build();

    public final static TerrainType OCEAN = TerrainType.make().setName("OCEAN").setCode("_")
            .setCanHaveTree(false).setSea(true).setMoveCost(1).setMoveCostWithTree(1)
            .setDefenseBonus(0).setDefenseBonusWithTree(0).setProduction(GoodType.CORN)
            .setWithoutTrees(3).build().build();

    public final static List<TerrainType> TERRAINS = ImmutableList.of(GRASSLAND, PRAIRIE, SAVANNAH,
            SWAMP, DESERT, TUNDRA, ARCTIC, HILL, MOUNTAIN, OCEAN, HIGH_SEA);

    private final static Map<String, TerrainType> TERRAINS_BY_NAME = TERRAINS.stream()
            .collect(ImmutableMap.toImmutableMap(TerrainType::name, Function.identity()));

    private final static Map<String, TerrainType> TERRAINS_BY_CODE = TERRAINS.stream()
            .collect(ImmutableMap.toImmutableMap(TerrainType::getCode, Function.identity()));

    public final static List<TerrainType> UNIT_CAN_SAIL_AT = TERRAINS.stream()
            .filter(terrain -> terrain.isSea()).collect(ImmutableList.toImmutableList());

    public final static List<TerrainType> UNIT_CAN_WALK_AT = TERRAINS.stream()
            .filter(terrain -> terrain.isLand()).collect(ImmutableList.toImmutableList());

    private static class TerrainTypeBuilder {

        private final List<Production> productions = new ArrayList<>();

        private String name = null;

        private String code;

        private boolean canHaveTree;

        private boolean isSea;

        private int moveCost = NO_VALUE;

        private int moveCostWithTree = NO_VALUE;

        private int defenseBonus = NO_VALUE;

        private int defenseBonusWithTree = NO_VALUE;

        TerrainType build() {
            return new TerrainType(name, code, canHaveTree, isSea, moveCost, moveCostWithTree,
                    defenseBonus, defenseBonusWithTree, productions);
        }

        TerrainTypeBuilder setName(String name) {
            this.name = name;
            return this;
        }

        TerrainTypeBuilder setCode(String code) {
            this.code = code;
            return this;
        }

        TerrainTypeBuilder setCanHaveTree(boolean canHaveTree) {
            this.canHaveTree = canHaveTree;
            return this;
        }

        /**
         * Set if it's see or ocean and if ships can sail it.
         * 
         * @param isSea
         *            required value if it's <code>true</code> than ships can
         *            sail on it and it's see
         * @return builder object
         */
        TerrainTypeBuilder setSea(boolean isSea) {
            this.isSea = isSea;
            return this;
        }

        TerrainTypeBuilder setMoveCost(int moveCost) {
            this.moveCost = moveCost;
            return this;
        }

        TerrainTypeBuilder setMoveCostWithTree(int moveCostWithTree) {
            this.moveCostWithTree = moveCostWithTree;
            return this;
        }

        TerrainTypeBuilder setDefenseBonus(int defenseBonus) {
            this.defenseBonus = defenseBonus;
            return this;
        }

        TerrainTypeBuilder setDefenseBonusWithTree(int defenseBonusWithTree) {
            this.defenseBonusWithTree = defenseBonusWithTree;
            return this;
        }

        ProductionBuilder setProduction(final GoodType goodType) {
            return new ProductionBuilder(this, goodType);
        }

        void addProduction(final Production production) {
            Preconditions.checkArgument(!productions.contains(production),
                    "Production (%s) was already defined");
            productions.add(Preconditions.checkNotNull(production));
        }

    }

    public static class Production {

        private final GoodType goodType;

        private final int withTrees;

        private final int withoutTrees;

        Production(final GoodType goodType, final int withTrees, final int withoutTrees) {
            this.goodType = Preconditions.checkNotNull(goodType);
            this.withTrees = withTrees;
            this.withoutTrees = withoutTrees;
        }

        public GoodType getGoodType() {
            return goodType;
        }

        public int getWithTrees() {
            return withTrees;
        }

        public int getWithoutTrees() {
            return withoutTrees;
        }

        @Override
        public int hashCode() {
            return goodType.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj instanceof Production) {
                final Production other = (Production) obj;
                return goodType.equals(other.goodType);
            }
            return false;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Production.class).add("goodType", goodType)
                    .add("withTrees", withTrees).add("withoutTrees", withoutTrees).toString();
        }

    }

    private static class ProductionBuilder {

        private final TerrainTypeBuilder terrainBuilder;

        private final GoodType goodType;

        private int withTrees = 0;

        private int withoutTrees = 0;

        ProductionBuilder(final TerrainTypeBuilder terrainBuilder, final GoodType goodType) {
            this.terrainBuilder = Preconditions.checkNotNull(terrainBuilder);
            this.goodType = Preconditions.checkNotNull(goodType);
        }

        ProductionBuilder setWithTrees(final int withTrees) {
            this.withTrees = withTrees;
            return this;
        }

        ProductionBuilder setWithoutTrees(final int withoutTrees) {
            this.withoutTrees = withoutTrees;
            return this;
        }

        TerrainTypeBuilder build() {
            terrainBuilder.addProduction(new Production(goodType, withTrees, withoutTrees));
            return terrainBuilder;
        }

    }

    private static TerrainTypeBuilder make() {
        return new TerrainTypeBuilder();
    }

    TerrainType(final String name, final String code, final boolean canHaveTree,
            final boolean isSee, final int moveCost, final int moveCostWithTree,
            final int defenseBonus, final int defenseBonusWithTree,
            final List<Production> productions) {
        Preconditions.checkArgument(moveCost != NO_VALUE, "move cost was not set.");
        Preconditions.checkArgument(moveCostWithTree != NO_VALUE,
                "move cost in trees was not set.");
        Preconditions.checkArgument(defenseBonus != NO_VALUE, "defense bonus was not set.");
        Preconditions.checkArgument(defenseBonusWithTree != NO_VALUE,
                "defense bonus in trees was not set.");
        this.name = Preconditions.checkNotNull(name, "name was not set");
        this.code = Preconditions.checkNotNull(code, "code was not set");
        this.canHaveTree = canHaveTree;
        this.isSea = isSee;
        this.moveCost = moveCost;
        this.moveCostWithTree = moveCostWithTree;
        this.defenseBonus = defenseBonus;
        this.defenseBonusWithTree = defenseBonusWithTree;
        this.productions = ImmutableList.copyOf(Preconditions.checkNotNull(productions));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof TerrainType) {
            final TerrainType other = (TerrainType) obj;
            return name.equals(other.name);
        }
        return false;
    }

    public static TerrainType valueOf(final String unitTypeName) {
        if (TERRAINS_BY_NAME.get(unitTypeName) == null) {
            throw new IllegalArgumentException(
                    String.format("There is no such UnitType (%s)", unitTypeName));
        } else {
            return TERRAINS_BY_NAME.get(unitTypeName);
        }
    }

    public static TerrainType valueOfCode(final String code) {
        Preconditions.checkNotNull(code, "Terrain code can't be null.");
        final TerrainType out = TERRAINS_BY_CODE.get(code.trim());
        if (out == null) {
            throw new IllegalArgumentException(
                    String.format("There is no terrain type for code '%s'", code));
        }
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(TerrainType.class).add("name", name).toString();
    }

    public String name() {
        return name;
    }

    public boolean isCanHaveTree() {
        return canHaveTree;
    }

    public boolean isSea() {
        return isSea;
    }

    public boolean isLand() {
        return !isSea;
    }

    public int getMoveCost() {
        return moveCost;
    }

    public int getMoveCostWithTree() {
        return moveCostWithTree;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public int getDefenseBonusWithTree() {
        return defenseBonusWithTree;
    }

    public List<Production> getProductions() {
        return productions;
    }

    public boolean canProduce(final GoodType goodType) {
        return productions.stream().map(Production::getGoodType)
                .filter(goodType1 -> goodType1.equals(goodType)).findAny().isPresent();
    }

    public Optional<Production> getProductionForGoodType(final GoodType goodType) {
        return productions.stream().filter(production -> production.getGoodType().equals(goodType))
                .findAny();
    }

    public String getCode() {
        return code;
    }

}
