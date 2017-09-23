package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * It's countryside definition. It also specify what is on that land.
 */
public class Terrain {

	private final TerrainType terrainType;
	
	private boolean hasTrees;
	
	public Terrain(final TerrainType terrainType) {
		this.terrainType = Preconditions.checkNotNull(terrainType);
		hasTrees = false;
	}

	public TerrainType getTerrainType() {
		return terrainType;
	}

	public boolean isHasTrees() {
		return hasTrees;
	}

	public void setHasTrees(boolean hasTrees) {
		Preconditions.checkArgument(terrainType.isCanHaveTree() || !hasTrees,
				"this terrain type (%s) can't have trees.", terrainType);
		this.hasTrees = hasTrees;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Terrain.class)
				.add("terrain", terrainType.name())
				.add("hasTrees", hasTrees)
				.toString();
	}

}
