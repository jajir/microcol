package org.microcol.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class WorldMap {
	@Deprecated
	private final String fileName;
	private final int maxX;
	private final int maxY;
	private final ImmutableMap<Location, TerrainType> terrainMap;
	
	public WorldMap(final int maxX, final int maxY, Map<Location, TerrainType> terrainMap){
		Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.", maxY);
		
		this.fileName = null;
		this.maxX = maxX;
		this.maxY = maxY;
		this.terrainMap = ImmutableMap.copyOf(terrainMap);
	}

	public WorldMap(final String fileName) {
		Preconditions.checkNotNull(fileName);

		this.fileName = fileName;

		int maxX = 0;
		int maxY = 0;
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

	public String getFileName() {
		return fileName;
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
		return MoreObjects.toStringHelper(this).add("fileName", fileName).add("maxX", maxX).add("maxY", maxY)
				.add("landmass", terrainMap.keySet().size()).toString();
	}

	void save(final String name, final JsonGenerator generator) {
		generator.writeStartObject(name).write("fileName", fileName).writeEnd();
	}

	static WorldMap load(final JsonParser parser) {
		parser.next(); // START_OBJECT
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_STRING
		final String fileName = parser.getString();
		parser.next(); // END_OBJECT

		return new WorldMap(fileName);
	}
}
