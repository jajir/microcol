package org.microcol.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
			.build();

	public final static Terrain OCEAN = Terrain.make()
			.setName("OCEAN")
			.setCanHaveTree(false)
			.setSee(true)
			.setMoveCost(1)
			.setMoveCostWithTree(1)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.build();

	public final static Terrain TUNDRA = Terrain.make()
			.setName("TUNDRA")
			.setCanHaveTree(false)
			.setSee(false)
			.setMoveCost(2)
			.setMoveCostWithTree(2)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.build();

	public final static Terrain HIGH_SEA = Terrain.make()
			.setName("HIGH_SEA")
			.setCanHaveTree(false)
			.setSee(true)
			.setMoveCost(1)
			.setMoveCostWithTree(1)
			.setDefenseBonus(0)
			.setDefenseBonusWithTree(0)
			.build();
	
	public final static List<Terrain> TERRAINS = ImmutableList.of(GRASSLAND, OCEAN, TUNDRA, HIGH_SEA);
	
	private final static Map<String, Terrain> TERRAINS_BY_NAME = TERRAINS.stream()
			.collect(ImmutableMap.toImmutableMap(Terrain::name, Function.identity()));
	
	public final static List<Terrain> UNIT_CAN_SAIL_AT = TERRAINS.stream().filter(terrain -> terrain.isSee())
			.collect(ImmutableList.toImmutableList());
	
	private static class TerrainBuilder{
		
		private String name = null;
		
		private boolean canHaveTree;

		private boolean isSee;
		
		private int moveCost = NO_VALUE;
		
		private int moveCostWithTree = NO_VALUE;
		
		private int defenseBonus = NO_VALUE;
		
		private int defenseBonusWithTree = NO_VALUE;
		
		private Terrain build(){
			return new Terrain(name, canHaveTree, isSee, moveCost, moveCostWithTree, defenseBonus,
					defenseBonusWithTree);
		}

		private TerrainBuilder setName(String name) {
			this.name = name;
			return this;
		}

		private TerrainBuilder setCanHaveTree(boolean canHaveTree) {
			this.canHaveTree = canHaveTree;
			return this;
		}

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
	
	private Terrain(final String name, final boolean canHaveTree, final boolean isSee, final int moveCost,
			final int moveCostWithTree, final int defenseBonus, final int defenseBonusWithTree) {
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
	
}
