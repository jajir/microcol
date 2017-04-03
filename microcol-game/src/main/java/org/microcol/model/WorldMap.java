package org.microcol.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class WorldMap {
	private final int maxX;
	private final int maxY;
	private final ImmutableMap<Location, Terrain> terrainMap;

	WorldMap(final String fileName) {
		Preconditions.checkNotNull(fileName);

		int maxX = 0;
		int maxY = 0;
		final Map<Location, Terrain> terrainMap = new HashMap<>();

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(fileName)))) {
			String line = null;
			while ((line = reader.readLine()) != null && !line.startsWith("-")) {
				maxX = Math.max(maxX, line.length() - 1);
				maxY++;
				for (int x = 0; x < line.length() - 1; x++) {
					final char tile = line.charAt(x);
					switch (tile) {
						case 'o':
							terrainMap.put(Location.of(x + 1, maxY), Terrain.CONTINENT);
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

	public Terrain getTerrainAt(final Location location) {
		Preconditions.checkArgument(isValid(location), "Location (%s) is not part of this map.", location);

		Terrain terrain = terrainMap.get(location);

		return terrain != null ? terrain : Terrain.OCEAN;
	}

	public boolean isValid(final Location location) {
		Preconditions.checkNotNull(location);

		return location.getX() >= 1
			&& location.getX() <= getMaxX()
			&& location.getY() >= 1
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
			.add("landmass", terrainMap.keySet().size())
			.toString();
	}
}
