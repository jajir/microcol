package org.microcol.model.store;

import java.util.Map;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

public class WorldMapPo {

	/**
	 * Hold tile definitions rows.
	 */
	private String[] tiles;

	private int maxX;

	private int maxY;

	public void set(final Map<Location, TerrainType> terrainMap, final int maxX, final int maxY) {
		this.maxX = maxX;
		this.maxY = maxY;
		tiles = new String[maxY];
		for (int y = 0; y < maxY; y++) {
			final StringBuilder buff = new StringBuilder();
			for (int x = 0; x < maxX; x++) {
				if (x > 0) {
					buff.append(",");
				}
				Location loc = Location.of(x + 1, y + 1);
				final TerrainType terrainType = terrainMap.get(loc);
				if (terrainType == null) {
					buff.append(TerrainType.OCEAN.getCode());
				} else {
					buff.append(terrainType.getCode());
				}
			}
			tiles[y] = buff.toString();
		}

		terrainMap.forEach((location, terrain) -> {

		});

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

}
