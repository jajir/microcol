package org.microcol.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

// TODO JKA Documentation
// TODO JKA Tests
public class Map {
	private final Terrain[][] terrain;

	public Map(final int maxX, final int maxY) {
		Preconditions.checkArgument(maxX >= 1, "MaxX (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY >= 1, "MaxY (%s) must be positive.", maxY);

		terrain = new Terrain[maxX][maxY];
	}

	public Map(final Terrain[][] terrain) {
		Preconditions.checkNotNull(terrain);
		Preconditions.checkArgument(terrain.length >= 1, "MaxX (%s) must be positive.", terrain.length);
		Preconditions.checkArgument(terrain[0].length >= 1, "MaxY (%s) must be positive.", terrain[0].length);

		this.terrain = terrain;
	}

	public int getMaxX() {
		return terrain.length;
	}

	public int getMaxY() {
		return terrain[0].length;
	}

	public Terrain getTerrainAt(final Location location) {
		Preconditions.checkNotNull(location);

		return terrain[location.getX() - 1][location.getY() - 1];
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

		// TODO JKA Use streams
		for (Location location : path.getLocations()) {
			if (!isValid(location)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("maxX", getMaxX())
			.add("maxY", getMaxY())
			.toString();
	}

	public String print() {
		StringBuilder builder = new StringBuilder();

		for (int x = 0; x < terrain.length + 2; x++) {
			builder.append("-");
		}
		builder.append("\n");

		for (int y = 0; y < terrain[0].length; y++) {
			builder.append("|");
			for (int x = 0; x < terrain.length; x++) {
				if (terrain[x][y] == Terrain.CONTINENT) {
					builder.append("o");
				} else {
					builder.append(" ");
				}
			}
			builder.append("|\n");
		}

		for (int x = 0; x < terrain.length + 2; x++) {
			builder.append("-");
		}

		return builder.toString();
	}

	public static Map load(final String fileName) {
		final List<Terrain[]> rows = new ArrayList<>();

		try (final BufferedReader reader = new BufferedReader(
			new InputStreamReader(Map.class.getResourceAsStream(fileName)))) {
			String line = null;
			while ((line = reader.readLine()) != null && !line.startsWith("-")) {
				final Terrain[] row = new Terrain[line.length() - 1];
				for (int x = 0; x < line.length() - 1; x++) {
					row[x] = line.charAt(x) == 'o' ? Terrain.CONTINENT : Terrain.OCEAN; 
				}
				rows.add(row);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		final Terrain[][] terrain = new Terrain[rows.get(0).length][rows.size()];
		for (int y = 0; y < rows.size(); y++) {
			for (int x = 0; x < rows.get(y).length; x++) {
				terrain[x][y] = rows.get(y)[x];
			}
		}

		return new Map(terrain);
	}

	public static void main(String[] args) {
//		Map map = new Map(20, 10);
		Map map = load("/maps/map-01.txt");
		System.out.println(map.print());
	}
}
