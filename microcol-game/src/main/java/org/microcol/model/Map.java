package org.microcol.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

// TODO JKA Documentation
// TODO JKA Tests
public class Map {
	private final int maxX;
	private final int maxY;

	private final Terrain[][] xxx;

	public Map(final int maxX, final int maxY) {
		// FIXME JKA >= 0
		Preconditions.checkArgument(maxX >= 1, "MaxX (%s) must be positive.", maxX);
		// FIXME JKA >= 0
		Preconditions.checkArgument(maxY >= 1, "MaxY (%s) must be positive.", maxY);

		this.maxX = maxX;
		this.maxY = maxY;

		this.xxx = new Terrain[maxX + 1][maxY + 1];
		this.xxx[3][2] = Terrain.CONTINENT;
	}

	public Map(final Terrain[][] xxx) {
		this.maxX = xxx.length;
		this.maxY = xxx[0].length;
		this.xxx = xxx;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public Terrain getTerrainAt(final Location location) {
		return xxx[location.getX()][location.getY()];
	}

	public boolean isValid(final Location location) {
		Preconditions.checkNotNull(location);

		return location.getX() >= 1
			&& location.getX() <= maxX
			&& location.getY() >= 1
			&& location.getY() <= maxY;
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
			.add("maxX", maxX)
			.add("maxY", maxY)
			.toString();
	}

	public String toString2() {
		StringBuilder builder = new StringBuilder();

		for (int x = 0; x < xxx.length + 2; x++) {
			builder.append("-");
		}
		builder.append("\n");

		for (int y = 0; y < xxx[0].length; y++) {
			builder.append("|");
			for (int x = 0; x < xxx.length; x++) {
				if (xxx[x][y] == Terrain.CONTINENT) {
					builder.append("o");
				} else {
					builder.append(" ");
				}
			}
			builder.append("|\n");
		}

		for (int x = 0; x < xxx.length + 2; x++) {
			builder.append("-");
		}
		builder.append("\n");

		return builder.toString();
	}

	public static Map load(final String fileName) {
		final InputStream in = Map.class.getResourceAsStream(fileName);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		final List<List<Terrain>> aaa = new ArrayList<List<Terrain>>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("-")) {
					break;
				}
				final List<Terrain> pole = new ArrayList<>();
				for (int i = 0; i < line.length(); i++) {
					if (line.charAt(i) == '|') {
						break;
					}
					if (line.charAt(i) == 'o') {
						pole.add(Terrain.CONTINENT);
					} else {
						pole.add(Terrain.OCEAN);
					}
				}
				//System.out.println(line);
				aaa.add(pole);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		Terrain[][] xxx = new Terrain[aaa.get(0).size()][aaa.size()];
		for (int y = 0; y < aaa.size(); y++) {
			for (int x = 0; x < aaa.get(y).size(); x++) {
				xxx[x][y] = aaa.get(y).get(x);
			}
		}

		return new Map(xxx);
	}

	public static void main(String[] args) {
//		Map map = new Map(20, 10);
		Map map = load("/maps/map-01.txt");
		System.out.println(map.toString2());
	}
}
