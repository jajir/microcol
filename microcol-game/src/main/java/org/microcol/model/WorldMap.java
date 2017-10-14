package org.microcol.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.microcol.model.store.GamePo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class WorldMap {
	
	private final int maxX;
	private final int maxY;
	
	private final ImmutableMap<Location, TerrainType> terrainMap;
	
	private final Set<Location> trees;
	
	public WorldMap(final GamePo gamePo){
		final WorldMapPo worldMapPo = gamePo.getMap();
		this.maxX = worldMapPo.getMaxX();
		this.maxY = worldMapPo.getMaxY();
		Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.", maxY);
		Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.", maxX);
		this.terrainMap = ImmutableMap.copyOf(worldMapPo.getTerrainMap());
		this.trees = worldMapPo.getTreeSet();
	}

	@Deprecated
	public WorldMap(final int maxX, final int maxY, Map<Location, TerrainType> terrainMap){
		Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.", maxY);
		
		this.maxX = maxX;
		this.maxY = maxY;
		this.terrainMap = ImmutableMap.copyOf(terrainMap);
		this.trees = new HashSet<>();
	}
	
	@Deprecated
	public WorldMap(final String fileName) {
		Preconditions.checkNotNull(fileName);

		int maxX = 0;
		int maxY = 0;
		this.trees = new HashSet<>();
		final Map<Location, TerrainType> terrainMap = new HashMap<>();

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(fileName), "UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null && !line.startsWith("-")) {
				maxX = Math.max(maxX, line.length() - 1);
				maxY++;
				for (int x = 0; x < line.length() - 1; x++) {
					final char tile = line.charAt(x);
					switch (tile) {
					case 'o':
						terrainMap.put(Location.of(x + 1, maxY), TerrainType.GRASSLAND);
						break;
					case 't':
						terrainMap.put(Location.of(x + 1, maxY), TerrainType.TUNDRA);
						break;
					case 'a':
						terrainMap.put(Location.of(x + 1, maxY), TerrainType.ARCTIC);
						break;
					case 'h':
						terrainMap.put(Location.of(x + 1, maxY), TerrainType.HIGH_SEA);
						break;
					case ' ':
						// Do nothing.
						break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported character (%s).", tile));
					}
				}
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException(String.format("Unable to load map from file (%s)", fileName), ex);
		}
		//XXX it's duplicated code
		Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.", maxY);

		this.maxX = maxX;
		this.maxY = maxY;
		this.terrainMap = ImmutableMap.copyOf(terrainMap);
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

	void save(final GamePo gamePo) {
		gamePo.getMap().setMaxX(maxX);
		gamePo.getMap().setMaxY(maxY);
		gamePo.getMap().setTerrainType(terrainMap);
		gamePo.getMap().setTrees(trees);
	}

}
