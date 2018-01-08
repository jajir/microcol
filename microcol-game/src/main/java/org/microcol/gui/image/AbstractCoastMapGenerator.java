package org.microcol.gui.image;

import java.util.HashMap;
import java.util.Map;

import org.microcol.model.ChainOfCommandOptionalStrategy;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;
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
 * <p>
 * Coast it crossing from one environment to another one. Example is sea and
 * green land, another example is arctic and sea. Coats is crossing from some
 * mass to void. So the sea is void and land is mass.
 * </p>
 * 
 */
public abstract class AbstractCoastMapGenerator {

	/**
	 * When it's returned it stop further searching for cost image and result in null image.
	 */
	private static final String NO_IMAGE = "no-image";

	private final ImageProvider imageProvider;

	private Map<Location, Image> mapTiles = new HashMap<>();

	private WorldMap map;

	protected AbstractCoastMapGenerator(final ImageProvider imageProvider) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
	}

	abstract String getPrefix();
	
	abstract boolean isVoid(final Location infoHolder);
	
	abstract boolean isMass(final Location infoHolder);
	
	abstract boolean skipp(final Location infoHolder);

	public void setMap(final WorldMap map) {
		this.map = Preconditions.checkNotNull(map);
		mapTiles.clear();
		
		for (int y = 1; y <= map.getMaxY(); y++) {
			for (int x = 1; x <= map.getMaxX(); x++) {
				final Location loc = Location.of(x, y);
				final String code = getTileCode(loc);
				if (code != null && !NO_IMAGE.equals(code)) {
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
				Lists.newArrayList(this::isItMass, this::isItWell, this::isItOpenVoid, this::isItUShape,
						this::isItLShape, this::isItIShape, this::isItIIShape));
		/*
		 * Null can be returned, for example when analyzing arctic borders at
		 * land border tile.
		 */
		return terrainImageResolvers.apply(location).orElse(null);
	}

	private String isItWell(final Location loc) {
		preconditionItsSea(loc);
		final Neighbors nei = new Neighbors(loc, this);
		
		if (isMass(nei.north().loc()) && isMass(nei.east().loc()) && isMass(nei.south().loc()) && isMass(nei.west().loc())) {
			return getPrefix() + "well";
		} else {
			return null;
		}
	}

	private String isItOpenVoid(final Location loc) {
		preconditionItsSea(loc);
		final Neighbors nei = new Neighbors(loc, this);
		
		if (isVoid(nei.north().loc()) && isVoid(nei.east().loc()) && isVoid(nei.south().loc()) && isVoid(nei.west().loc())) {
			//skip further processing, result to null image
			return NO_IMAGE;
		} else {
			return null;
		}
	}

	private String isItMass(final Location loc) {
		if (isMass(loc) || skipp(loc)) {
			//skip further processing, result to null image
			return NO_IMAGE;
		} else {
			return null;
		}
	}

	private String isItUShape(final Location loc) {
		preconditionItsSea(loc);
		final Neighbors nei = new Neighbors(loc, this);

		if (isVoid(nei.north().loc()) && isMass(nei.east().loc()) && isMass(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "u-shapeNorth-";
			code += getNorthWestCorner_fromWest(nei.northWest());
			code += getNorthEastCorner_fromEast(nei.northEast());
			return code;
		}

		if (isMass(nei.north().loc()) && isVoid(nei.east().loc()) && isMass(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "u-shapeEast-";
			code += getNorthEastCorner_fromNorth(nei.northEast());
			code += getSouthEastCorner_fromSouth(nei.southEast());
			return code;
		}

		if (isMass(nei.north().loc()) && isMass(nei.east().loc()) && isVoid(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "u-shapeSouth-";
			code += getSouthEastCorner_fromEast(nei.southEast());
			code += getSouthWestCorner_fromWest(nei.southWest());
			return code;
		}

		if (isMass(nei.north().loc()) && isMass(nei.east().loc()) && isMass(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "u-shapeWest-";
			code += getSouthWestCorner_fromSouth(nei.southWest());
			code += getNorthWestCorner_fromNorth(nei.northWest());
			return code;
		}

		return null;
	}

	private String isItLShape(final Location loc) {
		preconditionItsSea(loc);
		final Neighbors nei = new Neighbors(loc, this);
		
		if (isMass(nei.north().loc()) && isMass(nei.east().loc()) && isVoid(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "l-shapeNorthEast-";
			code += getNorthWestCorner_fromNorth(nei.northWest());
			code += getSouthEastCorner_fromEast(nei.southEast());
			return code;
		}

		if (isVoid(nei.north().loc()) && isMass(nei.east().loc()) && isMass(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "l-shapeSouthEast-";
			code += getNorthEastCorner_fromEast(nei.northEast());
			code += getSouthWestCorner_fromSouth(nei.southWest());
			return code;
		}

		if (isVoid(nei.north().loc()) && isVoid(nei.east().loc()) && isMass(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "l-shapeSouthWest-";
			code += getSouthEastCorner_fromSouth(nei.southEast());
			code += getNorthWestCorner_fromWest(nei.northWest());
			return code;
		}

		if (isMass(nei.north().loc()) && isVoid(nei.east().loc()) && isVoid(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "l-shapeNorthWest-";
			code += getSouthWestCorner_fromWest(nei.southWest());
			code += getNorthEastCorner_fromNorth(nei.northEast());
			return code;
		}

		return null;
	}

	private String isItIShape(final Location loc) {
		preconditionItsSea(loc);
		final Neighbors nei = new Neighbors(loc, this);

		if (isMass(nei.north().loc()) && isVoid(nei.east().loc()) && isVoid(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "i-shapeNorth-";
			code += getNorthWestCorner_fromNorth(nei.northWest());
			code += getNorthEastCorner_fromNorth(nei.northEast());
			return code;
		}

		if (isVoid(nei.north().loc()) && isMass(nei.east().loc()) && isVoid(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "i-shapeEast-";
			code += getNorthEastCorner_fromEast(nei.northEast());
			code += getSouthEastCorner_fromEast(nei.southEast());
			return code;
		}

		if (isVoid(nei.north().loc()) && isVoid(nei.east().loc()) && isMass(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "i-shapeSouth-";
			code += getSouthEastCorner_fromSouth(nei.southEast());
			code += getSouthWestCorner_fromSouth(nei.southWest());
			return code;
		}

		if (isVoid(nei.north().loc()) && isVoid(nei.east().loc()) && isVoid(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "i-shapeWest-";
			code += getSouthWestCorner_fromWest(nei.southWest());
			code += getNorthWestCorner_fromWest(nei.northWest());
			return code;
		}

		return null;
	}

	private String isItIIShape(final Location loc) {
		preconditionItsSea(loc);
		final Neighbors nei = new Neighbors(loc, this);

		if (isMass(nei.north().loc()) && isVoid(nei.east().loc()) && isMass(nei.south().loc()) && isVoid(nei.west().loc())) {
			String code = getPrefix() + "ii-shapeNorthSouth-";
			// North
			code += getNorthWestCorner_fromNorth(nei.northWest());
			code += getNorthEastCorner_fromNorth(nei.northEast());
			// South
			code += getSouthEastCorner_fromSouth(nei.southEast());
			code += getSouthWestCorner_fromSouth(nei.southWest());
			return code;
		}

		if (isVoid(nei.north().loc()) && isMass(nei.east().loc()) && isVoid(nei.south().loc()) && isMass(nei.west().loc())) {
			String code = getPrefix() + "ii-shapeEastWest-";
			// East
			code += getNorthEastCorner_fromEast(nei.northEast());
			code += getSouthEastCorner_fromEast(nei.southEast());
			// West
			code += getSouthWestCorner_fromWest(nei.southWest());
			code += getNorthWestCorner_fromWest(nei.northWest());
			return code;
		}

		return null;
	}

	private void preconditionItsSea(final Location loc) {
		Preconditions.checkArgument(isVoid(loc), "Invalid location '%s' it is not void", loc);
	}

	/**
	 * When location is out of map it will be moved back to map.
	 */
	protected TerrainType getTerrainTypeAt(final Location location) {
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

	private String getNorthEastCorner_fromNorth(final InfoHolder ttNorthEast) {
		return isVoid(ttNorthEast.loc()) ? "3" : "4";
	}

	private String getNorthEastCorner_fromEast(final InfoHolder ttNorthEast) {
		return isVoid(ttNorthEast.loc()) ? "3" : "2";
	}

	/**
	 * SouthEast
	 */

	private String getSouthEastCorner_fromEast(final InfoHolder ttSouthEast) {
		return isVoid(ttSouthEast.loc()) ? "6" : "7";
	}

	private String getSouthEastCorner_fromSouth(final InfoHolder ttSouthEast) {
		return isVoid(ttSouthEast.loc()) ? "6" : "5";
	}

	/**
	 * SouthWest
	 */

	private String getSouthWestCorner_fromSouth(final InfoHolder ttSouthWest) {
		return isVoid(ttSouthWest.loc()) ? "9" : "a";
	}

	private String getSouthWestCorner_fromWest(final InfoHolder ttSouthWest) {
		return isVoid(ttSouthWest.loc()) ? "9" : "8";
	}

	/**
	 * NorthWest
	 */

	private String getNorthWestCorner_fromWest(final InfoHolder ttNortWest) {
		return isVoid(ttNortWest.loc()) ? "0" : "1";
	}

	private String getNorthWestCorner_fromNorth(final InfoHolder ttNortWest) {
		return isVoid(ttNortWest.loc()) ? "0" : "b";
	}

	protected WorldMap getMap() {
		return map;
	}

}
