package org.microcol.gui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.microcol.model.Terrain;
import org.microcol.model.UnitType;

import com.google.common.collect.ImmutableMap;

import javafx.scene.image.Image;

/**
 * Provide image instances.
 * 
 * @author jan
 *
 */
public class ImageProvider {

	public static final String IMG_CURSOR_GOTO = "cursor-goto.png";

	public static final String IMG_ICON_STEPS_25x25 = "icon-steps-25x25.png";

	public static final String IMG_ICON_STEPS_TURN_25x25 = "icon-steps-turn-25x25.png";

	public static final String IMG_ICON_STEPS_FIGHT_25x25 = "icon-steps-fight-25x25.png";

	public static final String IMG_ICON_STEPS_ANCHOR_25x25 = "icon-steps-anchor-25x25.png";

	public static final String IMG_ICON_STEPS_ANCHOR_TURN_25x25 = "icon-steps-anchor-turn-25x25.png";

	public static final String IMG_ICON_STEPS_FIGHT_TURN_25x25 = "icon-steps-fight-turn-25x25.png";

	public static final String IMG_SPLASH_SCREEN = "splash-screen.png";

	private static final String IMG_TILE_OCEAN = "tile-ocean.png";

	private static final String IMG_TILE_LAND = "tile-land.png";

	private static final String IMG_TILE_SHIP_GALEON = "tile-ship-galeon.png";

	private static final String IMG_TILE_SHIP_FRIGATE = "tile-ship-frigate.png";

	private static final String IMG_UNIT_FREE_COLONIST = "tile-unit-free-colonist.png";

//	private static final String IMG_TILE_MODE_GOTO = "tile-mode-goto.png";

	public static final String IMG_TILE_MODE_MOVE = "tile-mode-move.png";

//	private static final String IMG_TILE_MODE_FORTIFY = "tile-mode-fortify.png";

//	private static final String IMG_TILE_MODE_PLOW = "tile-mode-plow.png";

//	private static final String IMG_TILE_MODE_ROAD = "tile-mode-road.png";

	public static final String IMG_CROSSED_SWORDS = "crossed-swords.png";

	public static final String IMG_GOOD_CORN = "good-corn.png";

	public static final String IMG_GOOD_SUGAR = "good-sugar.png";

	public static final String IMG_GOOD_TOBACCO = "good-tobacco.png";

	public static final String IMG_GOOD_COTTON = "good-cotton.png";

	public static final String IMG_GOOD_FUR = "good-fur.png";

	public static final String IMG_GOOD_LUMBER = "good-lumber.png";

	public static final String IMG_GOOD_ORE = "good-ore.png";

	public static final String IMG_GOOD_SILVER = "good-silver.png";

	public static final String IMG_GOOD_HORSE = "good-horse.png";

	public static final String IMG_GOOD_RUM = "good-rum.png";

	public static final String IMG_GOOD_CIGARS = "good-cigars.png";

	public static final String IMG_GOOD_SILK = "good-silk.png";

	public static final String IMG_GOOD_COAT = "good-coat.png";

	public static final String IMG_GOOD_GOODS = "good-goods.png";

	public static final String IMG_GOOD_TOOLS = "good-tools.png";

	public static final String IMG_GOOD_MUSKET = "good-musket.png";

	public static final String IMG_CRATE_OPEN = "crate-open.png";

	public static final String IMG_CRATE_CLOSED = "crate-closed.png";

	public static final String IMG_TILE_TOWN = "tile-town.png";

	public static final String IMG_TILE_FIELD = "tile-field.png";

	private static final String BASE_PACKAGE = "images";

	private final Map<String, Image> images;

	private final Map<Terrain, Image> terrainMap = ImmutableMap.<Terrain, Image>builder()
			.put(Terrain.CONTINENT, getRawImage(IMG_TILE_LAND)).put(Terrain.OCEAN, getRawImage(IMG_TILE_OCEAN)).build();

	private final Map<UnitType, Image> unitImageMap = ImmutableMap.<UnitType, Image>builder()
			.put(UnitType.GALLEON, getRawImage(IMG_TILE_SHIP_GALEON))
			.put(UnitType.FRIGATE, getRawImage(IMG_TILE_SHIP_FRIGATE))
			.put(UnitType.COLONIST, getRawImage(IMG_UNIT_FREE_COLONIST)).build();

	public ImageProvider() {
		images = new HashMap<>();
	}

	/**
	 * Load image for name.
	 * 
	 * @param name
	 *            required image name
	 * @return loaded image
	 */
	public Image getImage(final String name) {
		Image img = images.get(name);
		if (img == null) {
			img = ImageProvider.getRawImage(name);
			images.put(name, img);
		}
		return img;
	}

	/**
	 * Simplify loading image from resource. Path should look like: <code>
	 * org/microcol/images/unit-60x60.gif
	 * </code>. Class suppose that all images are in directory <i>images</i>.
	 * 
	 * @param rawPath
	 *            path at classpath where is stored image
	 * @return return {@link javafx.scene.image.Image} object
	 */
	public static Image getRawImage(final String rawPath) {
		final String path = BASE_PACKAGE + "/" + rawPath;
		ClassLoader cl = ImageProvider.class.getClassLoader();
		final InputStream in = cl.getResourceAsStream(path);
		if (in == null) {
			throw new MicroColException("Unable to load file '" + path + "'.");
		} else {
			return new Image(cl.getResourceAsStream(path));
		}
	}

	/**
	 * For specific terrain type find corresponding image.
	 * 
	 * @param terrain
	 *            required terrain type
	 * @return image representing terrain image
	 */
	public Image getTerrainImage(final Terrain terrain) {
		return terrainMap.get(terrain);
	}

	/**
	 * For specific ship type find corresponding image.
	 * 
	 * @param shipType
	 *            required ship type
	 * @return image representing ship image
	 */
	public Image getUnitImage(final UnitType shipType) {
		return unitImageMap.get(shipType);
	}

}
