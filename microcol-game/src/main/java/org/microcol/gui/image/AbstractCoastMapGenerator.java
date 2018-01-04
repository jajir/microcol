package org.microcol.gui.image;

import java.util.HashMap;
import java.util.Map;

import org.microcol.model.ChainOfCommandOptionalStrategy;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javafx.scene.image.Image;

/**
 * Class create map with boundaries between sea and continent. Boundary have to
 * be smooth. Each tile have to be nicely connected.
 * <p>
 * On each tiles there are defined directions. Direction describes tiles around
 * computed tiles. Directions are:
 * </p>
 * 
 * <pre>
 * NW  N  NE
 *  \  |  /
 *   \ | /
 *    \|/
 *W ---*--- E
 *    /|\
 *   / | \
 *  /  |  \
 * SW  S  SE
 * </pre>
 * <p>
 * Boundary on tile have to start at some point and end somewhere. This
 * connection point are defined in a following way:
 * </p>
 * 
 * <pre>
 * 0     1            2    3
 *   +---+------------+---+
 *   |\  |            |  /|
 *   |                    |
 * b +--                --+ 4
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 * a +--                --+ 5
 *   |                    |
 *   |/  |            |  \|
 *   +---+------------+---+
 * 9     8            7     6
 * </pre>
 * <p>
 * Background file defining each terrain type simple "png" bitmap split in
 * column and rows. Size of each row and column is defined by game tile size.
 * Each row contains each terrain type:
 * </p>
 * 
 * <ul>
 * <li>well - it's one see tile in the middle of land.</li>
 * <li>U-shape
 * 
 * <pre>
 *   +---+------------+---+
 *   |   |            |   |
 *   |   |            |   |
 *   |   |            |   |
 *   |   |            |   |
 *   |   |            |   |
 *   |   |            |   |
 *   |   |            |   |
 *   |   \            /   |
 *   |    +----------+    |
 *   |                    |
 *   +--------------------+
 * </pre>
 * 
 * </li>
 * <li>L-shape
 * 
 * <pre>
 *   +----------------+---+
 *   |                |   |
 *   |                |   |
 *   |                |   |
 *   |                |   |
 *   |                |   |
 *   |                |   |
 *   |                |   |
 *   |                /   |
 *   +---------------+    |
 *   |                    |
 *   +--------------------+
 * </pre>
 * 
 * </li>
 * <li>I-shape
 * 
 * <pre>
 *   +--------------------+
 *   |\                   |
 *   | +------------------+
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   +--------------------+
 * </pre>
 * 
 * </li>
 * <li>II-shape
 * 
 * <pre>
 *   +--------------------+
 *   |\                   |
 *   | +------------------+
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   |                    |
 *   | +------------------+
 *   |/                   |
 *   +--------------------+
 * </pre>
 * 
 * This images could be made by combining of two I-shape images.</li>
 * 
 * </ul>
 * 
 */
public abstract class AbstractCoastMapGenerator {

	private final ImageProvider imageProvider;

	private Map<Location, Image> mapTiles = new HashMap<>();

	private WorldMap map;

	protected AbstractCoastMapGenerator(final ImageProvider imageProvider) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
	}

	abstract String getPrefix();

	abstract boolean isItCoast(final TerrainType terrainType);

	public void setMap(final WorldMap map) {
		this.map = Preconditions.checkNotNull(map);

		for (int y = 1; y <= map.getMaxY(); y++) {
			for (int x = 1; x <= map.getMaxX(); x++) {
				final Location loc = Location.of(x, y);
				final String code = getTileCode(loc);
				if (code != null) {
					final Image img = imageProvider.getImage(code);
					mapTiles.put(loc, img);
				}
			}
		}

	}

	public Image getImageAt(final Location location) {
		return mapTiles.get(location);
	}

	private String getTileCode(final Location location) {
		Preconditions.checkArgument(map.isValid(location), "Invalid tile (%s)", location);
		final ChainOfCommandOptionalStrategy<Location, String> terrainImageResolvers = new ChainOfCommandOptionalStrategy<>(
				Lists.newArrayList(this::isItLand, this::isItWell, this::isItOpenSee, this::isItUShape,
						this::isItLShape, this::isItIShape, this::isItIIShape));
		// FIXME originally it always return something
		// return terrainImageResolvers.apply(location)
		// .orElseThrow(() -> new MicroColException(String.format("Unable to
		// resolve terrain at (%s)", location)));

		return terrainImageResolvers.apply(location).orElse(null);
	}

	private String isItWell(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		if (isItCoast(ttNorth) && isItCoast(ttEast) && isItCoast(ttSouth) && isItCoast(ttWest)) {
			return getPrefix() + "well";
		} else {
			return null;
		}
	}

	private String isItOpenSee(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		if (ttNorth.isSee() && ttEast.isSee() && ttSouth.isSee() && ttWest.isSee()) {
			final TerrainType tt = getTerrainTypeAt(loc);
			return terrainMap.get(tt);
		} else {
			return null;
		}
	}

	private Map<TerrainType, String> terrainMap = ImmutableMap.<TerrainType, String>builder()
			.put(TerrainType.GRASSLAND, ImageProvider.IMG_TILE_GRASSLAND)
			.put(TerrainType.OCEAN, ImageProvider.IMG_TILE_OCEAN).put(TerrainType.TUNDRA, ImageProvider.IMG_TILE_TUNDRA)
			.put(TerrainType.ARCTIC, ImageProvider.IMG_TILE_ARCTIC)
			.put(TerrainType.HIGH_SEA, ImageProvider.IMG_TILE_HIGH_SEA).build();

	private String isItLand(final Location loc) {
		final TerrainType tt = getTerrainTypeAt(loc);
		// FIXME -is it correct?
		if (tt.isLand()) {
			return terrainMap.get(tt);
		} else {
			return null;
		}
	}

	private String isItUShape(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttNorthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_EAST));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttSouthWest = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_WEST));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		final TerrainType ttNortWest = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_WEST));

		if (ttNorth.isSee() && isItCoast(ttEast) && isItCoast(ttSouth) && isItCoast(ttWest)) {
			String code = getPrefix() + "u-shapeNorth-";
			code += getNorthWestCorner_fromWest(ttNortWest);
			code += getNorthEastCorner_fromEast(ttNorthEast);
			return code;
		}

		if (isItCoast(ttNorth) && ttEast.isSee() && isItCoast(ttSouth) && isItCoast(ttWest)) {
			String code = getPrefix() + "u-shapeEast-";
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			return code;
		}

		if (isItCoast(ttNorth) && isItCoast(ttEast) && ttSouth.isSee() && isItCoast(ttWest)) {
			String code = getPrefix() + "u-shapeSouth-";
			code += getSouthEastCorner_fromEast(ttSouthEast);
			code += getSouthWestCorner_fromWest(ttSouthWest);
			return code;
		}

		if (isItCoast(ttNorth) && isItCoast(ttEast) && isItCoast(ttSouth) && ttWest.isSee()) {
			String code = getPrefix() + "u-shapeWest-";
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			code += getNorthWestCorner_fromNorth(ttNortWest);
			return code;
		}

		return null;
	}

	private String isItLShape(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttNorthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_EAST));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttSouthWest = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_WEST));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		final TerrainType ttNortWest = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_WEST));

		if (isItCoast(ttNorth) && isItCoast(ttEast) && ttSouth.isSee() && ttWest.isSee()) {
			String code = getPrefix() + "l-shapeNorthEast-";
			code += getNorthWestCorner_fromNorth(ttNortWest);
			code += getSouthEastCorner_fromEast(ttSouthEast);
			return code;
		}

		if (ttNorth.isSee() && isItCoast(ttEast) && isItCoast(ttSouth) && ttWest.isSee()) {
			String code = getPrefix() + "l-shapeSouthEast-";
			code += getNorthEastCorner_fromEast(ttNorthEast);
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isSee() && isItCoast(ttSouth) && isItCoast(ttWest)) {
			String code = getPrefix() + "l-shapeSouthWest-";
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			code += getNorthWestCorner_fromWest(ttNortWest);
			return code;
		}

		if (isItCoast(ttNorth) && ttEast.isSee() && ttSouth.isSee() && isItCoast(ttWest)) {
			String code = getPrefix() + "l-shapeNorthWest-";
			code += getSouthWestCorner_fromWest(ttSouthWest);
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			return code;
		}

		return null;
	}

	private String isItIShape(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttNorthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_EAST));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttSouthWest = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_WEST));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		final TerrainType ttNortWest = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_WEST));

		if (isItCoast(ttNorth) && ttEast.isSee() && ttSouth.isSee() && ttWest.isSee()) {
			String code = getPrefix() + "i-shapeNorth-";
			code += getNorthWestCorner_fromNorth(ttNortWest);
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			return code;
		}

		if (ttNorth.isSee() && isItCoast(ttEast) && ttSouth.isSee() && ttWest.isSee()) {
			String code = getPrefix() + "i-shapeEast-";
			code += getNorthEastCorner_fromEast(ttNorthEast);
			code += getSouthEastCorner_fromEast(ttSouthEast);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isSee() && isItCoast(ttSouth) && ttWest.isSee()) {
			String code = getPrefix() + "i-shapeSouth-";
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isSee() && ttSouth.isSee() && isItCoast(ttWest)) {
			String code = getPrefix() + "i-shapeWest-";
			code += getSouthWestCorner_fromWest(ttSouthWest);
			code += getNorthWestCorner_fromWest(ttNortWest);
			return code;
		}

		return null;
	}

	private String isItIIShape(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttNorthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_EAST));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouthEast = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttSouthWest = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH_WEST));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		final TerrainType ttNortWest = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH_WEST));

		if (isItCoast(ttNorth) && ttEast.isSee() && isItCoast(ttSouth) && ttWest.isSee()) {
			String code = getPrefix() + "ii-shapeNorthSouth-";
			// North
			code += getNorthWestCorner_fromNorth(ttNortWest);
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			// South
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			return code;
		}

		if (ttNorth.isSee() && isItCoast(ttEast) && ttSouth.isSee() && isItCoast(ttWest)) {
			String code = getPrefix() + "ii-shapeEastWest-";
			// East
			code += getNorthEastCorner_fromEast(ttNorthEast);
			code += getSouthEastCorner_fromEast(ttSouthEast);
			// West
			code += getSouthWestCorner_fromWest(ttSouthWest);
			code += getNorthWestCorner_fromWest(ttNortWest);
			return code;
		}

		return null;
	}

	private void preconditionItsSea(final Location loc) {
		final TerrainType tt = getTerrainTypeAt(loc);
		Preconditions.checkArgument(tt.isSee(), "Invalid location {} see is expected but it's {}", loc, tt);
	}

	/**
	 * When location is out of map it will be moved back to map.
	 */
	private TerrainType getTerrainTypeAt(final Location location) {
		Preconditions.checkNotNull(location);
		Location shifted = location;
		if (shifted.getX() < 1) {
			shifted = Location.of(1, shifted.getY());
		}
		if (shifted.getX() > map.getMaxX()) {
			shifted = Location.of(map.getMaxX(), shifted.getY());
		}
		if (shifted.getY() < 1) {
			shifted = Location.of(shifted.getX(), 1);
		}
		if (shifted.getY() > map.getMaxY()) {
			shifted = Location.of(shifted.getX(), map.getMaxY());
		}
		return map.getTerrainTypeAt(shifted);
	}

	/**
	 * NorthEast
	 */

	private String getNorthEastCorner_fromNorth(final TerrainType ttNorthEast) {
		return ttNorthEast.isSee() ? "3" : "4";
	}

	private String getNorthEastCorner_fromEast(final TerrainType ttNorthEast) {
		return ttNorthEast.isSee() ? "3" : "2";
	}

	/**
	 * SouthEast
	 */

	private String getSouthEastCorner_fromEast(final TerrainType ttSouthEast) {
		return ttSouthEast.isSee() ? "6" : "7";
	}

	private String getSouthEastCorner_fromSouth(final TerrainType ttSouthEast) {
		return ttSouthEast.isSee() ? "6" : "5";
	}

	/**
	 * SouthWest
	 */

	private String getSouthWestCorner_fromSouth(final TerrainType ttSouthWest) {
		return ttSouthWest.isSee() ? "9" : "a";
	}

	private String getSouthWestCorner_fromWest(final TerrainType ttSouthWest) {
		return ttSouthWest.isSee() ? "9" : "8";
	}

	/**
	 * NorthWest
	 */

	private String getNorthWestCorner_fromWest(final TerrainType ttNortWest) {
		return ttNortWest.isSee() ? "0" : "1";
	}

	private String getNorthWestCorner_fromNorth(final TerrainType ttNortWest) {
		return ttNortWest.isSee() ? "0" : "b";
	}

}
