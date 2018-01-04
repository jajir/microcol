package org.microcol.gui.image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microcol.gui.MicroColException;
import org.microcol.model.GoodAmount;
import org.microcol.model.GoodType;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javafx.scene.image.Image;

/**
 * Provide image instances.
 */
public class ImageProvider {
	
	/**
	 * Allows to register image loaders. It's ordered list of loaders. 
	 */
	private final List<ImageLoader> STARTUP_IMAGE_LOADERS = Lists.newArrayList(new BackgroundImageLoader(),
			new GrassCoastImageLoader(), new IceCoastImageLoader());

	public final static String BACKGROUND_IMAGE_NAME = "backgroud.png";
	
	public static final String IMG_CURSOR_GOTO = "cursor-goto.png";

	public static final String IMG_ICON_STEPS_25x25 = "icon-steps-25x25.png";

	public static final String IMG_ICON_STEPS_TURN_25x25 = "icon-steps-turn-25x25.png";

	public static final String IMG_ICON_STEPS_FIGHT_25x25 = "icon-steps-fight-25x25.png";

	public static final String IMG_ICON_STEPS_ANCHOR_25x25 = "icon-steps-anchor-25x25.png";

	public static final String IMG_ICON_STEPS_ANCHOR_TURN_25x25 = "icon-steps-anchor-turn-25x25.png";

	public static final String IMG_ICON_STEPS_FIGHT_TURN_25x25 = "icon-steps-fight-turn-25x25.png";

	public static final String IMG_SPLASH_SCREEN = "splash-screen.png";

	public static final String IMG_TILE_OCEAN = "tile-ocean.png";

	public static final String IMG_TILE_TUNDRA = "tile-tundra.png";

	public static final String IMG_TILE_ARCTIC = "tile-arctic.png";

	public static final String IMG_TILE_HIGH_SEA = "tile-high-sea.png";

	public static final String IMG_TILE_GRASSLAND = "tile-grassland.png";

	public static final String IMG_TILE_SHIP_GALEON = "tile-ship-galeon.png";

	public static final String IMG_TILE_SHIP_FRIGATE = "tile-ship-frigate.png";

	public static final String IMG_UNIT_FREE_COLONIST = "tile-unit-free-colonist.png";

	public static final String IMG_TILE_MODE_MOVE = "tile-mode-move.png";

	public static final String IMG_CROSSED_SWORDS = "crossed-swords.png";

	public static final String IMG_GOOD_CORN = "good-corn.png";

	public static final String IMG_GOOD_BELL = "good-bell.png";

	public static final String IMG_GOOD_HAMMER = "good-hammer.png";

	public static final String IMG_GOOD_CROSS = "good-cross.png";

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

	public static final String IMG_TREE = "tree.png";

	public static final String IMG_TILE_FIELD = "tile-field.png";

	private static final String BASE_PACKAGE = "images";

	private final Map<String, Image> images;
	
	private final Map<TerrainType, Image> terrainMap;

	private final Map<UnitType, Image> unitImageMap = ImmutableMap.<UnitType, Image>builder()
			.put(UnitType.GALLEON, getRawImage(IMG_TILE_SHIP_GALEON))
			.put(UnitType.FRIGATE, getRawImage(IMG_TILE_SHIP_FRIGATE))
			.put(UnitType.COLONIST, getRawImage(IMG_UNIT_FREE_COLONIST)).build();

	private final Map<GoodType, Image> goodTypeImageMap = ImmutableMap.<GoodType, Image>builder()
			.put(GoodType.CORN, getRawImage(IMG_GOOD_CORN))
			.put(GoodType.BELL, getRawImage(IMG_GOOD_BELL))
			.put(GoodType.HAMMERS, getRawImage(IMG_GOOD_HAMMER))
			.put(GoodType.CROSS, getRawImage(IMG_GOOD_CROSS))
			.put(GoodType.SUGAR, getRawImage(IMG_GOOD_SUGAR))
			.put(GoodType.TOBACCO, getRawImage(IMG_GOOD_TOBACCO))
			.put(GoodType.COTTON, getRawImage(IMG_GOOD_COTTON))
			.put(GoodType.FUR, getRawImage(IMG_GOOD_FUR))
			.put(GoodType.LUMBER, getRawImage(IMG_GOOD_LUMBER))
			.put(GoodType.ORE, getRawImage(IMG_GOOD_ORE))
			.put(GoodType.SILVER, getRawImage(IMG_GOOD_SILVER))
			.put(GoodType.HORSE, getRawImage(IMG_GOOD_HORSE))
			.put(GoodType.RUM, getRawImage(IMG_GOOD_RUM))
			.put(GoodType.CIGARS, getRawImage(IMG_GOOD_CIGARS))
			.put(GoodType.SILK, getRawImage(IMG_GOOD_SILK))
			.put(GoodType.COAT, getRawImage(IMG_GOOD_COAT))
			.put(GoodType.GOODS, getRawImage(IMG_GOOD_GOODS))
			.put(GoodType.TOOLS, getRawImage(IMG_GOOD_TOOLS))
			.put(GoodType.MUSKET, getRawImage(IMG_GOOD_MUSKET))
			.build();

	public ImageProvider() {
		images = new HashMap<>();
		STARTUP_IMAGE_LOADERS.forEach(loader -> loader.preload(this));
		terrainMap = ImmutableMap.<TerrainType, Image>builder()
				.put(TerrainType.GRASSLAND, getImage(IMG_TILE_GRASSLAND))
				.put(TerrainType.OCEAN, getImage(IMG_TILE_OCEAN))
				.put(TerrainType.TUNDRA, getImage(IMG_TILE_TUNDRA))
				.put(TerrainType.ARCTIC, getImage(IMG_TILE_ARCTIC))
				.put(TerrainType.HIGH_SEA, getImage(IMG_TILE_HIGH_SEA))
				.build();
	}
	
	public List<String> getTileNames(){
		return ImmutableList.copyOf(images.keySet());
	}

	/**
	 * Load image for name. If image is not in cache it try to load it from
	 * image store.
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
	 * Allow to register new type of image.
	 * 
	 * @param name
	 *            required unique image name
	 * @param image
	 *            required image object
	 */
	public void registerImage(final String name, final Image image){
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(image);
		if (images.containsKey(name)) {
			throw new IllegalArgumentException(String.format("Image with name '%s' was already registerd", name));
		} else {
			images.put(name, image);
		}
	}

	/**
	 * For specific terrain type find corresponding image.
	 * 
	 * @param terrain
	 *            required terrain type
	 * @return image representing terrain image
	 */
	public Image getTerrainImage(final TerrainType terrain) {
		return terrainMap.get(terrain);
	}

	/**
	 * For specific unit type find corresponding image.
	 * 
	 * @param unitType
	 *            required unit type
	 * @return image representing ship image
	 */
	public Image getUnitImage(final UnitType unitType) {
		return unitImageMap.get(unitType);
	}

	/**
	 * For specific unit find corresponding image.
	 * 
	 * @param unit
	 *            required unit
	 * @return image representing ship image
	 */
	public Image getUnitImage(final Unit unit) {
		return unitImageMap.get(unit.getType());
	}

	/**
	 * For specific good type find corresponding image.
	 * 
	 * @param goodType
	 *            required good type
	 * @return image representing good type
	 */
	public Image getGoodTypeImage(final GoodType goodType) {
		return goodTypeImageMap.get(goodType);
	}

	/**
	 * For specific good amount find corresponding image.
	 * 
	 * @param goodAmount
	 *            required good amount
	 * @return image representing good type
	 */
	public Image getGoodTypeImage(final GoodAmount goodAmount) {
		return goodTypeImageMap.get(goodAmount.getGoodType());
	}

}