package org.microcol.model.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

public class WorldMapPo extends AbstractMapStore {

	/**
	 * Hold tile definitions rows.
	 */
	private String[] tiles;

	/**
	 * Hold information where are trees.
	 */
	private String[] trees;

	private VisibilityPo visibility;

	private int maxX;

	private int maxY;
	
	private Integer seed;

	public void setTerrainType(final Map<Location, TerrainType> terrainMap) {
		tiles = generateString(loc -> {
			final TerrainType terrainType = terrainMap.get(loc);
			if (terrainType == null) {
				return TerrainType.OCEAN.getCode();
			} else {
				return terrainType.getCode();
			}
		}, maxX, maxY);
	}

	public void setTrees(final Set<Location> treesSet) {
		trees = generateString(loc -> {
			final boolean hasTree = treesSet.contains(loc);
			if (hasTree) {
				return "t";
			} else {
				return "-";
			}
		}, maxX, maxY);
	}

	public void setVisibles(final Set<Location> visibleSet) {
		trees = generateString(loc -> {
			final boolean isVisible = visibleSet.contains(loc);
			if (isVisible) {
				return "-";
			} else {
				return "#";
			}
		}, maxX, maxY);
	}

	public Map<Location, TerrainType> getTerrainMap() {
		final Map<Location, TerrainType> out = new HashMap<>();
		iterate(tiles, (location, charCode) -> {
			final TerrainType terrainType = TerrainType.valueOfCode(charCode);
			if (!terrainType.equals(TerrainType.OCEAN)) {
				out.put(location, terrainType);
			}
		});
		return out;
	}

	public Set<Location> getTreeSet() {
		final Set<Location> out = new HashSet<>();
		if (trees != null) {
			iterate(trees, (location, charCode) -> {
				if (charCode.equals("t")) {
					out.add(location);
				}
			});
		}
		return out;
	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(final int maxX) {
		this.maxX = maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	public String[] getTiles() {
		return tiles;
	}

	public String[] getTrees() {
		return trees;
	}

	public void setTrees(final String[] trees) {
		this.trees = trees;
	}

	/**
	 * @return the seed
	 */
	public Integer getSeed() {
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(Integer seed) {
		this.seed = seed;
	}

	/**
	 * @return the visibility
	 */
	public VisibilityPo getVisibility() {
		return visibility;
	}

	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(VisibilityPo visibility) {
		this.visibility = visibility;
	}

}
