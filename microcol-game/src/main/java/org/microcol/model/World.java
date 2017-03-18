package org.microcol.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class World {
	private final int maxX;
	private final int maxY;
	private final ImmutableMap<Location, Terrain> terrain;

	World(final int maxX, final int maxY) {
		Preconditions.checkArgument(maxX >= 1, "MaxX (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY >= 1, "MaxY (%s) must be positive.", maxY);

		this.maxX = maxX;
		this.maxY = maxY;
		this.terrain = ImmutableMap.of();
	}

	World(final String fileName) {
		Preconditions.checkNotNull(fileName);

		int maxX = 0;
		int maxY = 0;
		final Map<Location, Terrain> terrain = new HashMap<>();

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(World.class.getResourceAsStream(fileName)))) {
			String line = null;
			while ((line = reader.readLine()) != null && !line.startsWith("-")) {
				maxX = Math.max(maxX, line.length() - 1);
				maxY++;
				for (int x = 0; x < line.length() - 1; x++) {
					final char tile = line.charAt(x);
					switch (tile) {
						case 'o':
							terrain.put(Location.of(x + 1, maxY), Terrain.CONTINENT);
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
			throw new RuntimeException(ex);
		}

		Preconditions.checkArgument(maxX >= 1, "MaxX (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY >= 1, "MaxY (%s) must be positive.", maxY);

		this.maxX = maxX;
		this.maxY = maxY;
		this.terrain = ImmutableMap.copyOf(terrain);
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public Terrain getTerrainAt(final Location location) {
		Preconditions.checkNotNull(location);

		Terrain terrain = this.terrain.get(location);

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
			.add("maxX", getMaxX())
			.add("maxY", getMaxY())
			.toString();
	}

	String print() {
		StringBuilder builder = new StringBuilder();

		for (int x = 1; x <= maxX + 2; x++) {
			builder.append("-");
		}
		builder.append("\n");

		for (int y = 1; y <= maxY; y++) {
			builder.append("|");
			for (int x = 1; x <= maxX; x++) {
				switch (terrain.get(Location.of(x, y))) {
					case CONTINENT:
						builder.append("o");
						break;
					case OCEAN:
						builder.append(" ");
						break;
				}
			}
			builder.append("|\n");
		}

		for (int x = 1; x <= maxX + 2; x++) {
			builder.append("-");
		}

		return builder.toString();
	}

	public static void main(String[] args) {
//		World world = new World(15, 10);
		World world = new World("/maps/map-01.txt");
		System.out.println(world.print());
	}
}
