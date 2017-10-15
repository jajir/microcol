package org.microcol.model;

import java.util.Set;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class WorldMap {
	
	private final int maxX;
	private final int maxY;
	
	private final ImmutableMap<Location, TerrainType> terrainMap;
	
	private final Set<Location> trees;
	
	public WorldMap(final ModelPo gamePo){
		final WorldMapPo worldMapPo = gamePo.getMap();
		this.maxX = worldMapPo.getMaxX();
		this.maxY = worldMapPo.getMaxY();
		Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.", maxY);
		Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.", maxX);
		this.terrainMap = ImmutableMap.copyOf(worldMapPo.getTerrainMap());
		this.trees = worldMapPo.getTreeSet();
	}
	
	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public TerrainType getTerrainTypeAt(final Location location) {
		Preconditions.checkArgument(isValid(location), "Location (%s) is not part of this map.", location);

		TerrainType terrain = terrainMap.get(location);

		return terrain != null ? terrain : TerrainType.OCEAN;
	}
	
	/**
	 * Provide info about trees on specific location.
	 * 
	 * @param location
	 *            required location
	 * @return return <code>true</code> if location contain tree otherwise
	 *         return <code>false</code>
	 */
	public boolean isTreeAt(final Location location){
		Preconditions.checkArgument(isValid(location), "Location (%s) is not part of this map.", location);
		
		return trees.contains(location);
	}
	
	public Terrain getTerrainAt(final Location location){
		Preconditions.checkArgument(isValid(location), "Location (%s) is not part of this map.", location);
		final Terrain out = new Terrain(location, getTerrainTypeAt(location));
		out.setHasTrees(trees.contains(location));
		return out;
	}

	public boolean isValid(final Location location) {
		Preconditions.checkNotNull(location);

		return location.getX() >= 1 && location.getX() <= getMaxX() && location.getY() >= 1
				&& location.getY() <= getMaxY();
	}

	public boolean isValid(final Path path) {
		Preconditions.checkNotNull(path);

		return !path.getLocations().stream().anyMatch(location -> !isValid(location));
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("maxX", maxX)
				.add("maxY", maxY)
				.add("landmass", terrainMap.keySet().size()).toString();
	}

	void save(final ModelPo gamePo) {
		gamePo.getMap().setMaxX(maxX);
		gamePo.getMap().setMaxY(maxY);
		gamePo.getMap().setTerrainType(terrainMap);
		gamePo.getMap().setTrees(trees);
	}

}
