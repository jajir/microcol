package org.microcol.model;

import org.microcol.model.TerrainType.Production;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * It's countryside definition. It also specify what is on that land.
 */
public class Terrain {
	
	private final Location location;

	private final TerrainType terrainType;
	
	private boolean hasTrees;
	
	public Terrain(final Location location, final TerrainType terrainType) {
		this.location = Preconditions.checkNotNull(location);
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
	
	public int canProduce(final Production production) {
		if (isHasTrees()) {
			return production.getWithTrees();
		} else {
			return production.getWithoutTrees();
		}
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Terrain.class)
				.add("terrain", terrainType.name())
				.add("hasTrees", hasTrees)
				.toString();
	}

	public Location getLocation() {
		return location;
	}

}
