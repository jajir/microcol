package org.microcol.integration;

import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.MicroColException;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.model.ChainOfCommandOptionalStrategy;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;
import org.microcol.model.store.ModelProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Directions:
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
 * 
 * Connections to other tiles:
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
 * 
 * Background file defining each terrain type simple "png" bitmap split in
 * column and rows. Size of each row and column is defined by game tile size.
 * Each row contains each terrain type:
 * 
 * <pre>
 * well
 * U-shape
 * L-shape
 * I-shape
 * </pre>
 * 
 * fIXME describe shapes, describes meaning of direction for each
 * 
 * II-shape could be create by combining of I-shapes
 */
public class ImageTest {

	private final int TILE_MAX_X = 3;

	private final int TILE_MAX_Y = 4;

	private final Map<String, Image> images = new HashMap<>();

	private WorldMap map;

	private void init() {
		final Image img = ImageProvider.getRawImage("backgroud.png");
		for (int y = 0; y < TILE_MAX_Y; y++) {
			for (int x = 0; x < TILE_MAX_X; x++) {
				final String name = "type" + x + y;
				final PixelReader reader = img.getPixelReader();
				WritableImage tile = new WritableImage(reader, x * GamePanelView.TILE_WIDTH_IN_PX,
						y * GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX,
						GamePanelView.TILE_WIDTH_IN_PX);
				images.put(name, tile);
			}
		}

		images.put("well", images.get("type00"));

		// U-shape
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '3', getImg("type01"));
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '2', getImg("type11"));
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '3',
				getImg("u-shapeNorth-02").getImageReverseRows());
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '2', getImg("type21"));

		// L-shape
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', '9',
				getImg("type02"));
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', '9',
				getImg("type12"));
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', 'a',
				getImg("l-shapeSouthEast-29").getImageTranspose());
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', 'a',
				getImg("type22"));

		// I-shape
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '3', getImg("type03"));
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '4', getImg("type13"));
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '3',
				getImg("i-shapeNorth-04").getImageReverseRows());
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '4', getImg("type23"));

		// II-shape
		generateII_shape(Connector.of('0'), Connector.of('3'));
		generateII_shape(Connector.of('0'), Connector.of('4'));
		generateII_shape(Connector.of('b'), Connector.of('3'));
		generateII_shape(Connector.of('b'), Connector.of('4'));

	}

	private void generateII_shape(final Connector start, final Connector end) {
		final String name1 = "ii-shapeNorthSouth-" + start.get() + end.get();
		final String name2 = "ii-shapeEastWest-" + start.rotateRight().get() + end.rotateRight().get();
		final String imagName = "i-shapeNorth-" + start.get() + end.get();

		TileDef.of(name1, Connector.of('6'), Connector.of('9'), getImg(imagName).addImage(getImg("i-shapeSouth-69")))
				.storeTo(images).rotateRight(name2).storeTo(images);
		TileDef.of(name1, Connector.of('5'), Connector.of('9'), getImg(imagName).addImage(getImg("i-shapeSouth-59")))
				.storeTo(images).rotateRight(name2).storeTo(images);
		TileDef.of(name1, Connector.of('6'), Connector.of('a'), getImg(imagName).addImage(getImg("i-shapeSouth-6a")))
				.storeTo(images).rotateRight(name2).storeTo(images);
		TileDef.of(name1, Connector.of('5'), Connector.of('a'), getImg(imagName).addImage(getImg("i-shapeSouth-5a")))
				.storeTo(images).rotateRight(name2).storeTo(images);
	}

	private void addCycle(final String direction1, final String direction2, final String direction3,
			final String direction4, final char start, final char end, final ImageWrapper imageWrapper) {
		TileDef.of(direction1, Connector.of(start), Connector.of(end), imageWrapper).storeTo(images)
				.rotateRight(direction2).storeTo(images).rotateRight(direction3).storeTo(images).rotateRight(direction4)
				.storeTo(images);
	}

	ImageWrapper getImg(final String name) {
		return ImageWrapper.of(images.get(name));
	}

	@Test
	public void test_tryPaintMap() throws Exception {
		init();
		final Model model = new ModelProvider().buildComplexModel();
		assertNotNull(model);
		this.map = model.getMap();

		final WritableImage targetMap = new WritableImage(GamePanelView.TILE_WIDTH_IN_PX * map.getMaxX(),
				GamePanelView.TILE_WIDTH_IN_PX * map.getMaxY());

		for (int y = 1; y < map.getMaxY(); y++) {
			for (int x = 1; x < map.getMaxX(); x++) {
				final Location loc = Location.of(x, y);
				final String code = getTileCode(loc);
				final Image img = images.get(code);
				if (img != null) {
					paintAt(targetMap, x, y, img);
				}
				System.out.println("At " + loc + " is terrain image " + code);
			}
		}

		saveToFile(targetMap, "target/worldMap.png");
		System.out.println("Done");
	}

	private void paintAt(final WritableImage target, final int addX, int addY, final Image tile) {
		final PixelReader pixelReader = tile.getPixelReader();
		final PixelWriter pixelWriter = target.getPixelWriter();

		for (int y = 0; y < GamePanelView.TILE_WIDTH_IN_PX; y++) {
			for (int x = 0; x < GamePanelView.TILE_WIDTH_IN_PX; x++) {
				final Color color = pixelReader.getColor(x, y);
				pixelWriter.setColor(x + GamePanelView.TILE_WIDTH_IN_PX * addX,
						y + GamePanelView.TILE_WIDTH_IN_PX * addY, color);
			}
		}
	}

	private String getTileCode(final Location location) {
		Preconditions.checkArgument(map.isValid(location), "Invalid tile (%s)", location);
		final ChainOfCommandOptionalStrategy<Location, String> terrainImageResolvers = new ChainOfCommandOptionalStrategy<>(
				Lists.newArrayList(this::isItLand, this::isItWell, this::isItOpenSee, this::isItUShape,
						this::isItLShape, this::isItIShape, this::isItIIShape));
		return terrainImageResolvers.apply(location)
				.orElseThrow(() -> new MicroColException(String.format("Unable to resolve terrain at (%s)", location)));
	}

	private String isItWell(final Location loc) {
		preconditionItsSea(loc);
		final TerrainType ttNorth = getTerrainTypeAt(loc.add(Location.DIRECTION_NORTH));
		final TerrainType ttEast = getTerrainTypeAt(loc.add(Location.DIRECTION_EAST));
		final TerrainType ttSouth = getTerrainTypeAt(loc.add(Location.DIRECTION_SOUTH));
		final TerrainType ttWest = getTerrainTypeAt(loc.add(Location.DIRECTION_WEST));
		if (ttNorth.isLand() && ttEast.isLand() && ttSouth.isLand() && ttWest.isLand()) {
			return "well";
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
			return "openSea";
		} else {
			return null;
		}
	}

	private String isItLand(final Location loc) {
		final TerrainType tt = getTerrainTypeAt(loc);
		if (tt.isLand()) {
			return "land";
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

		if (ttNorth.isSee() && ttEast.isLand() && ttSouth.isLand() && ttWest.isLand()) {
			String code = "u-shapeNorth-";
			code += getNorthWestCorner_fromWest(ttNortWest);
			code += getNorthEastCorner_fromEast(ttNorthEast);
			return code;
		}

		if (ttNorth.isLand() && ttEast.isSee() && ttSouth.isLand() && ttWest.isLand()) {
			String code = "u-shapeEast-";
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			return code;
		}

		if (ttNorth.isLand() && ttEast.isLand() && ttSouth.isSee() && ttWest.isLand()) {
			String code = "u-shapeSouth-";
			code += getSouthEastCorner_fromEast(ttSouthEast);
			code += getSouthWestCorner_fromWest(ttSouthWest);
			return code;
		}

		if (ttNorth.isLand() && ttEast.isLand() && ttSouth.isLand() && ttWest.isSee()) {
			String code = "u-shapeWest-";
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

		if (ttNorth.isLand() && ttEast.isLand() && ttSouth.isSee() && ttWest.isSee()) {
			String code = "l-shapeNorthEast-";
			code += getNorthWestCorner_fromNorth(ttNortWest);
			code += getSouthEastCorner_fromEast(ttSouthEast);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isLand() && ttSouth.isLand() && ttWest.isSee()) {
			String code = "l-shapeSouthEast-";
			code += getNorthEastCorner_fromEast(ttNorthEast);
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isSee() && ttSouth.isLand() && ttWest.isLand()) {
			String code = "l-shapeSouthWest-";
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			code += getNorthWestCorner_fromWest(ttNortWest);
			return code;
		}

		if (ttNorth.isLand() && ttEast.isSee() && ttSouth.isSee() && ttWest.isLand()) {
			String code = "l-shapeNorthWest-";
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

		if (ttNorth.isLand() && ttEast.isSee() && ttSouth.isSee() && ttWest.isSee()) {
			String code = "i-shapeNorth-";
			code += getNorthWestCorner_fromNorth(ttNortWest);
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isLand() && ttSouth.isSee() && ttWest.isSee()) {
			String code = "i-shapeEast-";
			code += getNorthEastCorner_fromEast(ttNorthEast);
			code += getSouthEastCorner_fromEast(ttSouthEast);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isSee() && ttSouth.isLand() && ttWest.isSee()) {
			String code = "i-shapeSouth-";
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isSee() && ttSouth.isSee() && ttWest.isLand()) {
			String code = "i-shapeWest-";
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

		if (ttNorth.isLand() && ttEast.isSee() && ttSouth.isLand() && ttWest.isSee()) {
			String code = "ii-shapeNorthSouth-";
			// North
			code += getNorthWestCorner_fromNorth(ttNortWest);
			code += getNorthEastCorner_fromNorth(ttNorthEast);
			// South
			code += getSouthEastCorner_fromSouth(ttSouthEast);
			code += getSouthWestCorner_fromSouth(ttSouthWest);
			return code;
		}

		if (ttNorth.isSee() && ttEast.isLand() && ttSouth.isSee() && ttWest.isLand()) {
			String code = "ii-shapeEastWest-";
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

	public static void saveToFile(final Image image, String filePath) {
		final File outputFile = new File(filePath);
		final BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			ImageIO.write(bImage, "png", outputFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
