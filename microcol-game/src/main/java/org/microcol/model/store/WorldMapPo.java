package org.microcol.model.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

public class WorldMapPo {

	/**
	 * Hold tile definitions rows.
	 */
	private String[] tiles;

	/**
	 * Hold information where are trees.
	 */
	private String[] trees;

	private int maxX;

	private int maxY;

	public void setTerrainType(final Map<Location, TerrainType> terrainMap) {
		tiles = generateString(loc -> {
			final TerrainType terrainType = terrainMap.get(loc);
			if (terrainType == null) {
				return TerrainType.OCEAN.getCode();
			} else {
				return terrainType.getCode();
			}
		});
	}

	public void setTrees(final Set<Location> treesSet) {
		trees = generateString(loc -> {
			final boolean hasTree = treesSet.contains(loc);
			if (hasTree) {
				return "t";
			} else {
				return "-";
			}
		});
	}

	private String[] generateString(final Function<Location, String> charProducer) {
		final String[] out = new String[maxY];
		for (int y = 0; y < maxY; y++) {
			final StringBuilder buff = new StringBuilder();
			for (int x = 0; x < maxX; x++) {
				if (x > 0) {
					buff.append(",");
				}
				final Location loc = Location.of(x + 1, y + 1);
				buff.append(charProducer.apply(loc));
			}
			out[y] = buff.toString();
		}
		return out;
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

	private void iterate(final String[] rows, final BiConsumer<Location, String> consumer) {
		for (int y = 0; y < rows.length; y++) {
			final String row = rows[y];
			final String[] parts = row.split(",");
			for (int x = 0; x < parts.length; x++) {
				final String charCode = parts[x];
				final Location loc = Location.of(x + 1, y + 1);
				consumer.accept(loc, charCode);
			}
		}

	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
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

	public void setTrees(String[] trees) {
		this.trees = trees;
	}

}
