package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class Terrain {

	private final static int NO_VALUE = -1;
	
	public final static Terrain GRASSLAND = Terrain.make()
			.setName("GRASSLAND")
			.setCanHaveTree(true)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.CORN)
				.setWithTrees(3)
				.setWithoutTrees(4)
				.build()
			.setProduction(GoodType.SUGAR)
				.setWithoutTrees(1)
				.build()
			.setProduction(GoodType.TABACCO)
				.setWithTrees(1)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.COTTON)
				.setWithTrees(1)
				.setWithoutTrees(2)
				.build()
			.setProduction(GoodType.FUR)
				.setWithTrees(2)
				.build()
			.setProduction(GoodType.LUMBER)
				.setWithTrees(6)
				.build()
			.build();
	
	public final static Terrain PRAIRIE = Terrain.make()
			.setName("PRAIRIE")
			.setCanHaveTree(true)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.CORN)
				.setWithTrees(3)
				.setWithoutTrees(4)
				.build()
			.setProduction(GoodType.TABACCO)
				.setWithTrees(1)
				.setWithoutTrees(2)
				.build()
			.setProduction(GoodType.COTTON)
				.setWithTrees(1)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.FUR)
				.setWithTrees(2)
				.build()
			.setProduction(GoodType.LUMBER)
				.setWithTrees(4)
				.build()
			.build();
	
	public final static Terrain SAVANNAH = Terrain.make()
			.setName("SAVANNAH")
			.setCanHaveTree(true)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.CORN)
				.setWithTrees(3)
				.setWithoutTrees(4)
				.build()
			.setProduction(GoodType.SUGAR)
				.setWithoutTrees(1)
				.build()
			.setProduction(GoodType.TABACCO)
				.setWithTrees(1)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.COTTON)
				.setWithTrees(1)
				.setWithoutTrees(2)
				.build()
			.setProduction(GoodType.FUR)
				.setWithTrees(2)
				.build()
			.setProduction(GoodType.LUMBER)
				.setWithTrees(6)
				.build()
			.build();
	
	public final static Terrain SWAMP = Terrain.make()
			.setName("SWAMP")
			.setCanHaveTree(true)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.CORN)
				.setWithTrees(2)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.SUGAR)
				.setWithTrees(2)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.TABACCO)
				.setWithTrees(0)
				.setWithoutTrees(1)
				.build()
			.setProduction(GoodType.LUMBER)
				.setWithTrees(4)
				.build()
			.build();
	
	public final static Terrain DESERT = Terrain.make()
			.setName("DESERT")
			.setCanHaveTree(true)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.CORN)
				.setWithTrees(2)
				.setWithoutTrees(2)
				.build()
			.setProduction(GoodType.TABACCO)
				.setWithTrees(0)
				.setWithoutTrees(1)
				.build()
			.setProduction(GoodType.COTTON)
				.setWithTrees(1)
				.setWithoutTrees(1)
				.build()
			.setProduction(GoodType.FUR)
				.setWithTrees(2)
				.build()
			.setProduction(GoodType.LUMBER)
				.setWithTrees(2)
				.build()
			.setProduction(GoodType.ORE)
				.setWithTrees(1)
				.setWithoutTrees(2)
				.build()
			.build();

	public final static Terrain TUNDRA = Terrain.make()
			.setName("TUNDRA")
			.setCanHaveTree(false)
			.setSee(false)
			.setMoveCost(2)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.setProduction(GoodType.CORN)
				.setWithTrees(2)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.FUR)
				.setWithTrees(3)
				.build()
			.setProduction(GoodType.LUMBER)
				.setWithTrees(4)
				.build()
			.setProduction(GoodType.ORE)
				.setWithTrees(1)
				.setWithoutTrees(2)
				.build()
			.build();

	public final static Terrain ARTCIC = Terrain.make()
			.setName("ARTCIC")
			.setCanHaveTree(false)
			.setSee(false)
			.setMoveCost(2)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.build();
	
	public final static Terrain HILL = Terrain.make()
			.setName("HILL")
			.setCanHaveTree(false)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.CORN)
				.setWithoutTrees(2)
				.build()
			.setProduction(GoodType.ORE)
				.setWithoutTrees(4)
				.build()
			.build();
	
	public final static Terrain MOUNTAIN = Terrain.make()
			.setName("MOUNTAIN")
			.setCanHaveTree(false)
			.setSee(false)
			.setMoveCost(1)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(50)
			.setProduction(GoodType.ORE)
				.setWithoutTrees(3)
				.build()
			.setProduction(GoodType.SILVER)
				.setWithoutTrees(1)
				.build()
			.build();

	public final static Terrain HIGH_SEA = Terrain.make()
			.setName("HIGH_SEA")
			.setCanHaveTree(false)
			.setSee(true)
			.setMoveCost(1)
			.setMoveCostWithTree(1)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.setProduction(GoodType.CORN)
				.setWithoutTrees(3)
				.build()
			.build();

	public final static Terrain OCEAN = Terrain.make()
			.setName("OCEAN")
			.setCanHaveTree(false)
			.setSee(true)
			.setMoveCost(1)
			.setMoveCostWithTree(1)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.setProduction(GoodType.CORN)
				.setWithoutTrees(3)
				.build()
			.build();
	
	public final static List<Terrain> TERRAINS = ImmutableList.of(GRASSLAND, PRAIRIE, SAVANNAH, SWAMP, DESERT, TUNDRA,
			ARTCIC, HILL, MOUNTAIN, OCEAN, HIGH_SEA);
	
	private final static Map<String, Terrain> TERRAINS_BY_NAME = TERRAINS.stream()
			.collect(ImmutableMap.toImmutableMap(Terrain::name, Function.identity()));
	
	public final static List<Terrain> UNIT_CAN_SAIL_AT = TERRAINS.stream().filter(terrain -> terrain.isSee())
			.collect(ImmutableList.toImmutableList());
	
	private static class TerrainBuilder{
		
		private final List<Production> productions = new ArrayList<>();
		
		private String name = null;
		
		private boolean canHaveTree;

		private boolean isSee;
		
		private int moveCost = NO_VALUE;
		
		private int moveCostWithTree = NO_VALUE;
		
		private int defenseBonus = NO_VALUE;
		
		private int defenseBonusWithTree = NO_VALUE;
		
		private Terrain build(){
			return new Terrain(name, canHaveTree, isSee, moveCost, moveCostWithTree, defenseBonus,
					defenseBonusWithTree, productions);
		}

		private TerrainBuilder setName(String name) {
			this.name = name;
			return this;
		}

		private TerrainBuilder setCanHaveTree(boolean canHaveTree) {
			this.canHaveTree = canHaveTree;
			return this;
		}

		/**
		 * Set if it's see or ocean and if ships can sail it.
		 * 
		 * @param isSee
		 *            required value if it's <code>true</code> than ships can
		 *            sail on it and it's see
		 * @return builder object
		 */
		private TerrainBuilder setSee(boolean isSee) {
			this.isSee = isSee;
			return this;
		}

		private TerrainBuilder setMoveCost(int moveCost) {
			this.moveCost = moveCost;
			return this;
		}

		private TerrainBuilder setMoveCostWithTree(int moveCostWithTree) {
			this.moveCostWithTree = moveCostWithTree;
			return this;
		}

		private TerrainBuilder setDefenseBonus(int defenseBonus) {
			this.defenseBonus = defenseBonus;
			return this;
		}

		private TerrainBuilder setDefenseBonusWithTree(int defenseBonusWithTree) {
			this.defenseBonusWithTree = defenseBonusWithTree;
			return this;
		}

		private ProductionBuilder setProduction(final GoodType goodType) {
			return new ProductionBuilder(this, goodType);
		}
		
		private void addProduction(final Production production) {
			Preconditions.checkArgument(!productions.contains(production), "Production (%s) was already defined");
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

		GoodType getGoodType() {
			return goodType;
		}

		int getWithTrees() {
			return withTrees;
		}

		int getWithoutTrees() {
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
			return MoreObjects.toStringHelper(Production.class)
					.add("goodType", goodType)
					.add("withTrees", withTrees)
					.add("withoutTrees", withoutTrees)
					.toString();
		}
		
	}
	
	private static class ProductionBuilder {

		private final TerrainBuilder terrainBuilder;
		
		private final GoodType goodType;
		
		private int withTrees = 0;
		
		private int withoutTrees = 0;
		
		ProductionBuilder(final TerrainBuilder terrainBuilder, final GoodType goodType) {
			this.terrainBuilder = Preconditions.checkNotNull(terrainBuilder);
			this.goodType = Preconditions.checkNotNull(goodType);
		}

		private ProductionBuilder setWithTrees(final int withTrees) {
			this.withTrees = withTrees;
			return this;
		}

		private ProductionBuilder setWithoutTrees(final int withoutTrees) {
			this.withoutTrees = withoutTrees;
			return this;
		}
		
		private TerrainBuilder build() {
			terrainBuilder.addProduction(new Production(goodType, withTrees, withoutTrees));
			return terrainBuilder;
		}
		
	}
	
	private static TerrainBuilder make(){
		return new TerrainBuilder();
	}
	
	private final String name;
	
	private final boolean canHaveTree;

	private final boolean isSee;
	
	private final int moveCost;
	
	private final int moveCostWithTree;
	
	private final int defenseBonus;
	
	private final int defenseBonusWithTree;
	
	private final List<Production> productions;
	
	private Terrain(final String name, final boolean canHaveTree, final boolean isSee, final int moveCost,
			final int moveCostWithTree, final int defenseBonus, final int defenseBonusWithTree,
			final List<Production> productions) {
		Preconditions.checkArgument(moveCost != NO_VALUE, "move cost was not set.");
		Preconditions.checkArgument(moveCostWithTree != NO_VALUE, "move cost in trees was not set.");
		Preconditions.checkArgument(defenseBonus != NO_VALUE, "defense bonus was not set.");
		Preconditions.checkArgument(defenseBonusWithTree != NO_VALUE, "defense bonus in trees was not set.");
		this.name = Preconditions.checkNotNull(name, "name was not set");
		this.canHaveTree = canHaveTree;
		this.isSee = isSee;
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
		if (obj instanceof Terrain) {
			final Terrain other = (Terrain) obj;
			return name.equals(other.name);
		}
		return false;
	}

	public static Terrain valueOf(final String unitTypeName){
		if (TERRAINS_BY_NAME.get(unitTypeName) == null) {
			throw new IllegalArgumentException(String.format("There is no such UnitType (%s)", unitTypeName));
		} else {
			return TERRAINS_BY_NAME.get(unitTypeName);
		}
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Terrain.class).add("name", name).toString();
	}
	
	public String name() {
		return name;
	}

	public boolean isCanHaveTree() {
		return canHaveTree;
	}

	public boolean isSee() {
		return isSee;
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
	
}
